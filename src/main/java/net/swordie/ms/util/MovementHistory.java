package net.swordie.ms.util;

import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.life.movement.MovementInfo;

public class MovementHistory {
    public long timestamp;
    public MovementInfo movementInfo;

    public MovementHistory(long time, MovementInfo movementInfo) {
        this.timestamp = time;
        this.movementInfo = movementInfo;
    }
}
