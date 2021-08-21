package net.swordie.ms.connection.packet;

import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.handlers.header.OutHeader;

public class ActionBarPacket {

    public static int HEAVY_STANDER = 2023295;
    public static int HIGH_JUMP = 2023296;
    public static int SLIPSTREAM = 2023297;
    public static int CANNON = 2023298;



    public static OutPacket removeActionBar(int id) {
        OutPacket out = new OutPacket(OutHeader.ACTION_BAR_RESULT);
        out.encodeInt(6);
        out.encodeInt(id);
        return out;
    }

    public static OutPacket showActionBar(int id) {
        OutPacket out = new OutPacket(OutHeader.ACTION_BAR_RESULT);
        out.encodeInt(5);
        out.encodeInt(id);
        return out;
    }

    public static OutPacket enableAll(int id) {
        OutPacket out = new OutPacket(OutHeader.ACTION_BAR_RESULT);
        out.encodeInt(13);
        out.encodeInt(id);
        return out;
    }

    public static OutPacket updateActionBar(int actionBarId, int entryId, int usable, int maxUsable) {
        OutPacket out = new OutPacket(OutHeader.ACTION_BAR_RESULT);
        out.encodeInt(11);
        out.encodeInt(actionBarId);
        out.encodeInt(entryId);
        out.encodeInt(maxUsable - Math.min(usable, maxUsable));
        out.encodeInt(0);
        out.encodeInt(0);
        return out;
    }
}
