package net.swordie.ms.connection.packet;

import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.handlers.header.OutHeader;
import net.swordie.ms.life.FieldAttackObj;

/**
 * @author Sjonnie
 * Created on 8/19/2018.
 */
public class FieldAttackObjPool {

    public static OutPacket objCreate(FieldAttackObj fieldAttackObj) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_CREATE);

        outPacket.encode(fieldAttackObj);
        return outPacket;
    }

    public static OutPacket objRemoveByKey(int objectID) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_REMOVE_BY_KEY);

        outPacket.encodeInt(objectID);

        return outPacket;
    }

    public static OutPacket objInfo(FieldAttackObj fieldAttackObj) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_INFO);

        outPacket.encode(fieldAttackObj);
        outPacket.encodeInt(-1);
        return outPacket;
    }

    public static OutPacket onPushAct(int objectId, int act, boolean flip) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_PUSH_ACT);

        outPacket.encodeInt(objectId);
        outPacket.encodeInt(act);
        outPacket.encodeByte(flip);
                return outPacket;
    }

    public static OutPacket setAttack(int objectId, int attackIdx) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_SET_ATTACK);

        outPacket.encodeInt(objectId);
        outPacket.encodeInt(attackIdx);

        return outPacket;
    }

    public static OutPacket resultGetOff(int objectId, boolean unk) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_RESULT_GET_OFF);
        outPacket.encodeInt(objectId);
        outPacket.encodeByte(unk);
        return outPacket;
    }

    public static OutPacket resultBoard(int objectId, boolean success, int sender, int owner) {
        OutPacket outPacket = new OutPacket(OutHeader.FIELD_ATTACK_RESULT_BOARD);
        outPacket.encodeInt(objectId);
        outPacket.encodeByte(success);
        outPacket.encodeInt(sender);
        outPacket.encodeInt(owner);
        return outPacket;
    }

    public static OutPacket rewardBoard(int objectId) {
        OutPacket outPacket = new OutPacket(OutHeader.BONUS_REWARD_BOARD_FAO);
        outPacket.encodeInt(objectId);
        return outPacket;
    }
}
