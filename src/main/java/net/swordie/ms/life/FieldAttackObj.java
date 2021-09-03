package net.swordie.ms.life;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.Encodable;
import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.connection.packet.FieldAttackObjPool;
import net.swordie.ms.util.Position;

/**
 * @author Sjonnie
 * Created on 8/19/2018.
 */
public class FieldAttackObj extends Life implements Encodable {

    private int ownerID;
    private int reserveID;

    public int shots;

    public FieldAttackObj(int templateId) {
        super(templateId);
    }

    public FieldAttackObj(int templateId, int ownerID, Position position, boolean flip) {
        super(templateId);
        this.ownerID = ownerID;
        setPosition(position);
        setFlip(flip);
        shots = 3;
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(getObjectId());
        outPacket.encodeInt(getTemplateId());
        outPacket.encodeInt(getOwnerID());
        outPacket.encodeInt(getReserveID());
        outPacket.encodeByte(isFlip());
        outPacket.encodePositionInt(getPosition());
        outPacket.encodeByte(isFlip());
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public int getReserveID() {
        return reserveID;
    }

    public void setReserveID(int reserveID) {
        this.reserveID = reserveID;
    }

    @Override
    public void broadcastSpawnPacket(Char onlyChar) {
        onlyChar.write(FieldAttackObjPool.objCreate(this));
    }

    @Override
    public void broadcastLeavePacket() {
        if (ownerID != 0) {
            Char owner = getField().getCharByID(ownerID);
            if (owner != null) {
                owner.write(FieldAttackObjPool.resultGetOff(getObjectId(), true));
                owner.dispose();
            }
            ownerID = 0;
        }
        getField().broadcastPacket(FieldAttackObjPool.objRemoveByKey(this.getObjectId()));
    }
}
