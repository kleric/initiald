package net.swordie.ms.constants;

import net.swordie.ms.util.Position;

import java.awt.*;

public class FlagConstants {

    // Tree
    public static final int NIGHT_SPAWN_1_X = -1856;
    public static final int NIGHT_SPAWN_1_Y = 2558;

    // First Pit
    public static final int NIGHT_SPAWN_2_X = -1248;
    public static final int NIGHT_SPAWN_2_Y = 2618;

    // Second Pit
    public static final int NIGHT_SPAWN_3_X = -900;
    public static final int NIGHT_SPAWN_3_Y = 2618;

    // Item Sign
    public static final int NIGHT_SPAWN_4_X = 827;
    public static final int NIGHT_SPAWN_4_Y = 2050;

    // Platform next to item sign
    public static final int NIGHT_SPAWN_5_X = 1016;
    public static final int NIGHT_SPAWN_5_Y = 2092;

    // Mid Ropes
    public static final int NIGHT_SPAWN_6_X = 2116;
    public static final int NIGHT_SPAWN_6_Y = 2018;

    // Top Right Ropes
    public static final int NIGHT_SPAWN_7_X = 2415;
    public static final int NIGHT_SPAWN_7_Y = 1358;

    // Top Left Ropes
    public static final int NIGHT_SPAWN_8_X = 2169;
    public static final int NIGHT_SPAWN_8_Y = 1358;

    // Sjump Top
    public static final int NIGHT_SPAWN_9_X = -667;
    public static final int NIGHT_SPAWN_9_Y = 1058;

    // Maze Drop Middle
    public static final int NIGHT_SPAWN_10_X = -369;
    public static final int NIGHT_SPAWN_10_Y = 1718;

    // Maze Bottom
    public static final int NIGHT_SPAWN_11_X = -282;
    public static final int NIGHT_SPAWN_11_Y = 1898;

    //
    public static final int NIGHT_SPAWN_12_X = -2000;
    public static final int NIGHT_SPAWN_12_Y = 1778;

    public static final int STAR = 2633626;

    public static final int[] POWERUPS = {2023295, 2023296, 2023297, 2023298};
    //public static final int[] POWERUPS = { 2023296, 2023297};

    public static final int POWERUP_START_TIME = 34_000;
    public static final int POWERUP_SPAWN_TIME = 10_000;

    public static final int SKILL_A = 80001415;
    public static final int SKILL_F = 80001418;
    public static final int SKILL_G = 80001425;
    public static final int SKILL_H = 80001426;

    public static final int SKILL_CANNON = 95001004;
    public static final int CANNON_SPAWN_CYCLE = 35_000;
    public static final int CANNON_START_TIME = 20_000;
    public static final int CANNON_LIFE_TIME = 15_000;

    public static final int SKILL_CD = 40_000;

    public static final int MAP_SUNSET_EXIT = 932200004;

    public static final int MAP_NIGHT = 932200300;
    public static final int MAP_SUNSET = 932200200;
    public static final int MAP_DAY = 932200100;
    public static final int MAP_NEW_MORNING = 942000500;
    public static final int MAP_NEW_SUNSET = 942001500;
    public static final int MAP_NEW_NIGHT = 942002500;

    public static final int MAP_DAY_LOBBY = 932200001;
    public static final int MAP_SUNSET_LOBBY = 932200003;
    public static final int MAP_NIGHT_LOBBY = 932200005;
    public static final int MAP_NEW_MORNING_LOBBY = 942000000;
    public static final int MAP_NEW_SUNSET_LOBBY = 942001000;
    public static final int MAP_NEW_NIGHT_LOBBY = 942002000;

    public static final long MAP_EVENT_AREA = 820000000;

    public static final boolean SPAWN_GHOST = true;
    public static final String CAMERA_NAME = "Camera";

    public static final Position N_STAR_1 = new Position(-105, 2678);
    public static final Position N_STAR_2 = new Position(-1075, 2332);
    public static final Position N_STAR_3 = new Position(-2060, 1364);
    public static final Position N_STAR_4 = new Position(-2063, 1092);
    public static final Position N_STAR_5 = new Position(807, 1358);
    public static final Position N_STAR_6 = new Position(2064, 1298);

    public static final Position S_STAR_1 = new Position(-785, 1480);
    public static final Position S_STAR_2 = new Position(275, 1538);
    public static final Position S_STAR_3 = new Position(2013, 1128);
    public static final Position S_STAR_4 = new Position(1352, 1122);
    public static final Position S_STAR_5 = new Position(724, 1001);

    public static final Position M_STAR_1 = new Position(97, 2300);
    public static final Position M_STAR_2 = new Position(1893, 1238);
    public static final Position M_STAR_3 = new Position(811, 1178);
    public static final Position M_STAR_4 = new Position(420, 1000);
    public static final Position M_STAR_5 = new Position(-582, 1058);
    public static final Position M_STAR_6 = new Position(-2028, 1248);

    public static boolean isRaceLobby(int mapId) {
        switch (mapId) {
            case MAP_DAY_LOBBY:
            case MAP_SUNSET_LOBBY:
            case MAP_NIGHT_LOBBY:
                return true;
        }
        return false;
    }
}
