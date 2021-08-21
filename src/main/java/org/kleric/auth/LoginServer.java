package org.kleric.auth;

import com.google.gson.Gson;
import io.javalin.Javalin;
import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.Account;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.util.Util;
import net.swordie.ms.world.Channel;
import org.kleric.proximity.DiscordConnector;
import org.mindrot.jbcrypt.BCrypt;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class LoginServer {

    private static final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    private static LoginServer INSTANCE;

    public static LoginServer getInstance() {
        if (INSTANCE == null) {
            synchronized (LoginServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoginServer();
                }
            }
        }
        return INSTANCE;
    }

    private final HashMap<String, String> tokenMap = new HashMap<>();

    private LoginServer() {

    }

    public Account loginWithToken(String token) {
        synchronized (tokenMap) {
            if (tokenMap.containsKey(token)) {
                String username = tokenMap.get(token);
                if (username == null) return null;
                return Account.getFromDBByName(username);
            }
        }
        return null;
    }

    private Javalin restApp;

    public void init() {
        restApp = Javalin.create().start(7000);
        restApp.post("/login", ctx -> {
            Gson gson = new Gson();
            LoginRequest body = gson.fromJson(ctx.body(), LoginRequest.class);

            String token = attemptLogin(body.user, body.pw);

            LoginResult result = new LoginResult();
            if (token != null) {
                result.success = token;
            } else {
                result.error = "Username or password is wrong";
            }
            ctx.result(gson.toJson(result));
        });
        restApp.post("/bind", ctx -> {
            Gson gson = new Gson();
            LoginRequest body = gson.fromJson(ctx.body(), LoginRequest.class);
            System.out.println(ctx.body());
            System.out.println(body.discordId);
            if (body.discordId == null) {
                ctx.result("fail");
                return;
            }
            Account authed = checkLogin(body.user, body.pw);
            if (authed != null) {
                DiscordConnector.getInstance().saveDiscordId(authed, body.discordId);
                System.out.println("Logged in???");
                ctx.result("ok");
                return;
            }
            System.out.println("NOpe");
            ctx.result("fail");
        });
        restApp.get("/online", ctx -> {
            StringBuilder builder = new StringBuilder("");
            List<Channel> channels = Server.getInstance().getWorlds().get(0).getChannels();
            for (Channel c : channels) {
                builder.append("Channel ").append(c.getChannelId()).append("\n");
                Collection<Char> onlineChars = c.getChars().values();
                for (Char cr : onlineChars) {
                    builder.append(Char.makeMapleReadable(cr.getName()));
                    builder.append("\n");
                }
            }
           ctx.result(builder.toString());
        });
        restApp.get("/sunset", ctx -> {
            List<Char> characters = (List<Char>) DatabaseManager.getObjListFromDB(Char.class);

            characters.removeIf(next -> next.bestTimeSunset == null);
            characters.sort(Comparator.comparingLong(o -> o.bestTimeSunset));

            StringBuilder sb = new StringBuilder();
            NumberFormat formatter = new DecimalFormat("#0.000");
            for (int i = 0; i < 20 && i < characters.size(); i++) {
                sb.append(i + 1);
                sb.append(". ");
                Char c = characters.get(i);
                sb.append(c.getName());
                long time = c.bestTimeSunset;
                sb.append(" | ");
                sb.append(formatter.format(time / 1000.0));
                sb.append(" seconds");
                sb.append("\r\n");
            }
            ctx.result(sb.toString());
        });
        restApp.get("/night", ctx -> {
            List<Char> characters = (List<Char>) DatabaseManager.getObjListFromDB(Char.class);

            characters.removeIf(next -> next.bestTime == null);
            characters.sort(Comparator.comparingLong(o -> o.bestTime));

            StringBuilder sb = new StringBuilder();
            NumberFormat formatter = new DecimalFormat("#0.000");
            for (int i = 0; i < 20 && i < characters.size(); i++) {
                sb.append(i + 1);
                sb.append(". ");
                Char c = characters.get(i);
                sb.append(c.getName());
                long time = c.bestTime;
                sb.append(" | ");
                sb.append(formatter.format(time / 1000.0));
                sb.append(" seconds");
                sb.append("\r\n");
            }
            ctx.result(sb.toString());
        });
    }

    private static class LoginRequest {
        String pw;
        String user;
        Long discordId;
    }

    private static class LoginResult {
        String success;
        String error;
    }

    public Account checkLogin(String user, String password) {
        Account acct = Account.getFromDBByName(user);
        if (acct == null) return null;
        String dbPassword = acct.getPassword();
        boolean hashed = Util.isStringBCrypt(dbPassword);
        boolean success;
        if (hashed) {
            try {
                success = BCrypt.checkpw(password, dbPassword);
            } catch (IllegalArgumentException e) { // if password hashing went wrong
                success = false;
            }
        } else {
            success = password.equals(dbPassword);
        }

        if (success) {
            if (!hashed) {
                acct.setPassword(BCrypt.hashpw(acct.getPassword(), BCrypt.gensalt(ServerConstants.BCRYPT_ITERATIONS)));
                // if a user has an assigned pic, hash it
                // TODO: hash
                /*if (acct.getPic() != null && acct.getPic().length() >= 6 && !Util.isStringBCrypt(acct.getPic())) {
                    acct.setPic(BCrypt.hashpw(acct.getPic(), BCrypt.gensalt(ServerConstants.BCRYPT_ITERATIONS)));
                }*/
            }
            DatabaseManager.saveToDB(acct);

        }

        if (success) return acct;
        return null;
    }

    private String attemptLogin(String user, String password) {
        Account acct = Account.getFromDBByName(user);
        if (acct == null) return null;
        String dbPassword = acct.getPassword();
        boolean hashed = Util.isStringBCrypt(dbPassword);
        boolean success;
        if (hashed) {
            try {
                success = BCrypt.checkpw(password, dbPassword);
            } catch (IllegalArgumentException e) { // if password hashing went wrong
                success = false;
            }
        } else {
            success = password.equals(dbPassword);
        }

        if (success) {
            if (!hashed) {
                acct.setPassword(BCrypt.hashpw(acct.getPassword(), BCrypt.gensalt(ServerConstants.BCRYPT_ITERATIONS)));
                // if a user has an assigned pic, hash it
                // TODO: hash
                /*if (acct.getPic() != null && acct.getPic().length() >= 6 && !Util.isStringBCrypt(acct.getPic())) {
                    acct.setPic(BCrypt.hashpw(acct.getPic(), BCrypt.gensalt(ServerConstants.BCRYPT_ITERATIONS)));
                }*/
            }
            DatabaseManager.saveToDB(acct);

        }

        if (success) {
            String token = generateToken();
            synchronized (tokenMap) {
                while (tokenMap.containsKey(token)) {
                    token = generateToken();
                }
                tokenMap.put(token, user);
            }
            return token;
        }
        return null;
    }

    protected String generateToken() {
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 16) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}
