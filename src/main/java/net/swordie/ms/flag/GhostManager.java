package net.swordie.ms.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.swordie.ms.constants.FlagConstants;
import net.swordie.ms.life.movement.Movement;
import net.swordie.ms.life.movement.MovementInterfaceAdapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class GhostManager {

    private String lastSunset;
    private String lastNight;
    private String lastNewSunset;
    private String lastNewNight;
    private String lastMorning;

    private Ranking nightGhosts;
    private Ranking sunsetGhosts;
    private Ranking newNightGhosts;
    private Ranking newSunsetGhosts;
    private Ranking morningGhosts;

    private static final String GHOST_NIGHT = "night.json";
    private static final String GHOST_SUNSET = "sunset.json";

    private static final String GHOSTS_MORNING = "morning_ghosts.json";

    private static final String GHOSTS_NIGHT = "night_ghosts.json";
    private static final String GHOSTS_SUNSET = "sunset_ghosts.json";

    private static final String GHOSTS_NEW_NIGHT = "new_night_ghosts.json";
    private static final String GHOSTS_NEW_SUNSET = "new_sunset_ghosts.json";

    private Gson gson;

    private static class Ranking {
        List<Ghost> ghostRanking = new ArrayList<>();

        public void topTen() {
            Collections.sort(ghostRanking);
            HashSet<Integer> ids = new HashSet<>();
            ListIterator<Ghost> ghostListIterator = ghostRanking.listIterator();
            while (ghostListIterator.hasNext()) {
                Ghost g = ghostListIterator.next();
                if (ids.contains(g.id)) {
                    ghostListIterator.remove();
                } else {
                    ids.add(g.id);
                }
            }
            ghostRanking = ghostRanking.subList(0, Math.min(3, ghostRanking.size()));
        }
    }

    private GhostManager() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Movement.class, new MovementInterfaceAdapter());
        gson = builder.create();
        try {
            JsonReader reader = new JsonReader(new FileReader(GHOSTS_NIGHT));
            nightGhosts = gson.fromJson(reader, Ranking.class);
        } catch (FileNotFoundException e) {
            nightGhosts = new Ranking();
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(GHOSTS_SUNSET));
            sunsetGhosts = gson.fromJson(reader, Ranking.class);
        } catch (FileNotFoundException e) {
            sunsetGhosts = new Ranking();
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(GHOSTS_NEW_NIGHT));
            newNightGhosts = gson.fromJson(reader, Ranking.class);
        } catch (FileNotFoundException e) {
            newNightGhosts = new Ranking();
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(GHOSTS_NEW_SUNSET));
            newSunsetGhosts = gson.fromJson(reader, Ranking.class);
        } catch (FileNotFoundException e) {
            newSunsetGhosts = new Ranking();
        }
        try {
            JsonReader reader = new JsonReader(new FileReader(GHOSTS_MORNING));
            morningGhosts = gson.fromJson(reader, Ranking.class);
        } catch (FileNotFoundException e) {
            morningGhosts = new Ranking();
        }

        clean();
    }

    private void clean() {
        nightGhosts.ghostRanking.removeIf(g -> g.time < 130_000);
        saveGhosts();
    }

    private static GhostManager INSTANCE;

    public static GhostManager getInstance() {
        if (INSTANCE == null) {
            synchronized (GhostManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GhostManager();
                }
            }
        }
        return INSTANCE;
    }

    public Ghost getGhost(int fieldId) {
        if (fieldId == FlagConstants.MAP_SUNSET) {
            if (sunsetGhosts.ghostRanking.isEmpty()) return null;
            return sunsetGhosts.ghostRanking.get(0);
        } else if (fieldId == FlagConstants.MAP_NIGHT) {
            if (nightGhosts.ghostRanking.isEmpty()) return null;
            return nightGhosts.ghostRanking.get(0);
        } else if (fieldId == FlagConstants.MAP_NEW_SUNSET) {
            if (newSunsetGhosts.ghostRanking.isEmpty()) return null;
            return newSunsetGhosts.ghostRanking.get(0);
        } else if (fieldId == FlagConstants.MAP_NEW_NIGHT) {
            if (newNightGhosts.ghostRanking.isEmpty()) return null;
            return newNightGhosts.ghostRanking.get(0);
        } else if (fieldId == FlagConstants.MAP_DAY) {
            if (morningGhosts.ghostRanking.isEmpty()) return null;
            return morningGhosts.ghostRanking.get(0);
        }
        return null;
    }

    public List<Ghost> getGhosts(int fieldId) {
        if (fieldId == FlagConstants.MAP_SUNSET) {
            if (sunsetGhosts.ghostRanking.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(sunsetGhosts.ghostRanking);
        } else if (fieldId == FlagConstants.MAP_NIGHT){
            if (nightGhosts.ghostRanking.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(nightGhosts.ghostRanking);
        } else if (fieldId == FlagConstants.MAP_NEW_NIGHT) {
            if (newNightGhosts.ghostRanking.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(newNightGhosts.ghostRanking);
        } else if (fieldId == FlagConstants.MAP_NEW_SUNSET) {
            if (newSunsetGhosts.ghostRanking.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(newSunsetGhosts.ghostRanking);
        } else if (fieldId == FlagConstants.MAP_DAY) {
            if (morningGhosts.ghostRanking.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(morningGhosts.ghostRanking);
        }
        return new ArrayList<>();
    }

    private void saveGhosts() {
        sunsetGhosts.topTen();
        String sg = gson.toJson(sunsetGhosts);
        if (!sg.equals(lastSunset)) {
            try {
                PrintWriter out = new PrintWriter(GHOSTS_SUNSET);
                out.write(sg);
                out.close();
                lastSunset = sg;
            } catch (FileNotFoundException e) {
            }
        }
        morningGhosts.topTen();
        sg = gson.toJson(morningGhosts);
        if (!sg.equals(lastMorning)) {
            try {
                PrintWriter out = new PrintWriter(GHOSTS_MORNING);
                out.write(sg);
                out.close();
                lastMorning = sg;
            } catch (FileNotFoundException e) {
            }
        }
        nightGhosts.topTen();
        sg = gson.toJson(nightGhosts);
        if (!sg.equals(lastNight)) {
            try {
                PrintWriter out = new PrintWriter(GHOSTS_NIGHT);
                out.write(sg);
                out.close();
                lastNight = sg;
            } catch (FileNotFoundException e) {
            }
        }
        newSunsetGhosts.topTen();
        sg = gson.toJson(newSunsetGhosts);
        if (!sg.equals(lastNewSunset)) {
            try {
                PrintWriter out = new PrintWriter(GHOSTS_NEW_SUNSET);
                out.write(sg);
                out.close();
                lastNewNight = sg;
            } catch (FileNotFoundException e) {
            }
        }
        newNightGhosts.topTen();
        sg = gson.toJson(newNightGhosts);
        if (!sg.equals(lastNewNight)) {
            try {
                PrintWriter out = new PrintWriter(GHOSTS_NEW_NIGHT);
                out.write(sg);
                out.close();
                lastNewNight = sg;
            } catch (FileNotFoundException e) {
            }
        }
    }

    public void updateGhost(long fieldId, Ghost ghost) {
        if (fieldId == FlagConstants.MAP_SUNSET) {
            sunsetGhosts.ghostRanking.add(ghost);
            saveGhosts();
        } else if (fieldId == FlagConstants.MAP_NIGHT){
            nightGhosts.ghostRanking.add(ghost);
            saveGhosts();
        } else if (fieldId == FlagConstants.MAP_DAY) {
            morningGhosts.ghostRanking.add(ghost);
            saveGhosts();
        } else if (fieldId == 942001500){
            newSunsetGhosts.ghostRanking.add(ghost);
            saveGhosts();
        } else if (fieldId == 942002500){
            newNightGhosts.ghostRanking.add(ghost);
            saveGhosts();
        }
    }
}
