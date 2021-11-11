package org.kleric.proximity;

import com.google.gson.Gson;
import net.swordie.ms.client.character.Char;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DiscordLobbyManager {

    private static final String BOT_TOKEN = "<insert yours>";
    private static final String APP_ID = "<insert yours>";

    public static final int RACE_LOBBY_CAPACITY = 32;

    public static void deleteLobby(String secret) {
        String[] parts = secret.split(":");
        if (parts.length != 2) {
            return;
        }
        String id = parts[0];
        try {
            URL url = new URL("https://discord.com/api/v6/lobbies/" + id);
            URLConnection con = url.openConnection();

            HttpsURLConnection http = (HttpsURLConnection) con;
            http.setRequestMethod("DELETE"); // PUT is another valid option
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Authorization", "Bot " + BOT_TOKEN);
            http.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getOrCreateLobby(int capacity) {
        if (capacity < 6) {
            capacity = 6;
        }
        try {
            URL url = new URL("https://discord.com/api/v6/lobbies");
            URLConnection con = url.openConnection();

            HttpsURLConnection http = (HttpsURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Authorization", "Bot " + BOT_TOKEN);
            byte[] out = ("{\"application_id\":\"" + APP_ID + "\",\"capacity\":\"" + capacity + "\"}").getBytes(StandardCharsets.UTF_8);
            http.setDoOutput(true);
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            Gson gson = new Gson();
            Lobby lobby = gson.fromJson(content.toString(), Lobby.class);
            return lobby.id + ":" + lobby.secret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
