package org.kleric.auth;

import com.google.gson.Gson;
import io.javalin.Javalin;
import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.Account;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.flag.GhostManager;
import net.swordie.ms.loaders.StringData;
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
                    builder.append(" - ");
                    builder.append(StringData.getMapStringById(cr.getFieldID()));
                    builder.append("\n");
                }
            }
           ctx.result(builder.toString());
        });
        restApp.get("/sunset", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getSunsetRecords());

            StringBuilder sb = new StringBuilder();
            NumberFormat formatter = new DecimalFormat("#0.000");
            for (int i = 0; i < 20 && i < ranking.size(); i++) {
                sb.append(i + 1);
                sb.append(". ");
                Rank r = ranking.get(i);
                sb.append(r.name);
                long time = r.record.time;
                sb.append(" | ");
                sb.append(formatter.format(time / 1000.0));
                sb.append(" seconds");
                sb.append("\r\n");
            }
            ctx.result(sb.toString());
        });
        restApp.get("/night", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getNightRecords());
            ctx.result(formatRankingWithPowerups(ranking));
        });
        restApp.get("/morning", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getMorningRecords());
            ctx.result(formatRankingWithPowerups(ranking));
        });
        restApp.get("/new_night", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getNewNightRecords());
            ctx.result(formatRankingWithPowerups(ranking));
        });
        restApp.get("/new_sunset", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getNewSunsetRecords());
            ctx.result(formatRankingWithPowerups(ranking));
        });
        restApp.get("/new_morning", ctx -> {
            List<Rank> ranking = getRanking(GhostManager.getInstance().getNewMorningRecords());
            ctx.result(formatRankingWithPowerups(ranking));
        });
    }

    private String formatRankingWithPowerups(List<Rank> ranking) {
        StringBuilder sb = new StringBuilder();
        NumberFormat formatter = new DecimalFormat("#0.000");
        for (int i = 0; i < 20 && i < ranking.size(); i++) {
            sb.append(i + 1);
            sb.append(". ");
            Rank r = ranking.get(i);
            sb.append(r.name);
            long time = r.record.time;
            sb.append(" | ");
            sb.append(formatter.format(time / 1000.0));
            sb.append(" seconds");
            String powerups = getPowerupString(r.record);
            if (powerups != null) {
                sb.append(" | ");
                sb.append(powerups);
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    private String getPowerupString(GhostManager.Record record) {
        String powerup = "";
        if (record.sjumps == Integer.MAX_VALUE || record.dashes == Integer.MAX_VALUE) {
            return null;
        }
        powerup += record.sjumps;
        powerup += "S";
        powerup += record.dashes;
        powerup += "D";
        return powerup;
    }

    private List<Rank> getRanking(HashMap<String, GhostManager.Record> recordMap) {
        List<Rank> ranking = new ArrayList<>();
        for (String name : recordMap.keySet()) {
            Rank rank = new Rank();
            rank.name = name;
            rank.record = recordMap.get(name);
            ranking.add(rank);
        }
        ranking.sort(Comparator.comparingLong(r -> r.record.time));
        return ranking;
    }

    static class Rank {
        String name;
        GhostManager.Record record;
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
