package net.swordie.ms.client.character.commands;

import net.swordie.ms.Server;
import net.swordie.ms.client.Account;
import net.swordie.ms.client.character.BroadcastMsg;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.avatar.AvatarLook;
import net.swordie.ms.client.character.items.Equip;
import net.swordie.ms.client.character.items.Item;
import net.swordie.ms.client.character.quest.Quest;
import net.swordie.ms.client.character.skills.Option;
import net.swordie.ms.client.character.skills.Skill;
import net.swordie.ms.client.character.skills.StolenSkill;
import net.swordie.ms.client.character.skills.info.ForceAtomInfo;
import net.swordie.ms.client.character.skills.info.SkillInfo;
import net.swordie.ms.client.character.skills.temp.CharacterTemporaryStat;
import net.swordie.ms.client.character.skills.temp.TemporaryStatBase;
import net.swordie.ms.client.character.skills.temp.TemporaryStatManager;
import net.swordie.ms.client.jobs.adventurer.Archer;
import net.swordie.ms.client.jobs.adventurer.Magician;
import net.swordie.ms.client.jobs.adventurer.Thief;
import net.swordie.ms.client.jobs.nova.Kaiser;
import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.connection.packet.*;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.constants.JobConstants.JobEnum;
import net.swordie.ms.enums.*;
import net.swordie.ms.handlers.header.OutHeader;
import net.swordie.ms.life.Life;
import net.swordie.ms.life.mob.Mob;
import net.swordie.ms.life.mob.MobStat;
import net.swordie.ms.life.mob.MobTemporaryStat;
import net.swordie.ms.life.npc.Npc;
import net.swordie.ms.loaders.*;
import net.swordie.ms.loaders.containerclasses.SkillStringInfo;
import net.swordie.ms.util.FileTime;
import net.swordie.ms.util.Position;
import net.swordie.ms.util.Rect;
import net.swordie.ms.util.Util;
import net.swordie.ms.util.container.Tuple;
import net.swordie.ms.util.tools.StringUtil;
import net.swordie.ms.world.World;
import net.swordie.ms.world.field.Field;
import net.swordie.ms.world.field.FieldInstanceType;
import net.swordie.ms.world.field.Portal;
import net.swordie.ms.world.field.fieldeffect.FieldEffect;
import org.apache.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

import static net.swordie.ms.client.character.skills.SkillStat.*;
import static net.swordie.ms.client.character.skills.temp.CharacterTemporaryStat.*;
import static net.swordie.ms.enums.ChatType.*;
import static net.swordie.ms.enums.InventoryOperation.ADD;
import static net.swordie.ms.enums.PrivateStatusIDFlag.*;


/**
 * Created on 12/22/2017.
 */
public class PlayerCommands {


    static final org.apache.log4j.Logger log = LogManager.getRootLogger();

    @Command(names = {"hair"}, requiredType = NONE)
    public static class hair extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            AvatarLook hair1 = chr.getAvatarData().getAvatarLook();
            int hair = Integer.parseInt(args[1]);
            hair1.setHair(hair);
            chr.setStatAndSendPacket(Stat.hair, hair);
            // chr.changeChannelAndWarp(chr.getClient().getChannelInstance().getChannelId(), chr.getFieldID());
        }
    }

    @Command(names = {"cc"}, requiredType = NONE)
    public static class cc extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            int channel = Integer.parseInt(args[1]);
            if (channel > 0 && channel <= 3) {
                chr.changeChannel((byte) channel);
            } else {
                chr.chatMessage(Mob, String.format("Invalid channel %d", channel));
            }
        }
    }

    @Command(names = {"face"}, requiredType = NONE)
    public static class face extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            AvatarLook face1 = chr.getAvatarData().getAvatarLook();
            int face = Integer.parseInt(args[1]);
            face1.setFace(face);
            chr.setStatAndSendPacket(Stat.face, face);
            //     chr.changeChannelAndWarp(chr.getClient().getChannelInstance().getChannelId(), chr.getFieldID());
        }
    }

    @Command(names = {"getitem"}, requiredType = NONE)
    public static class GetItem extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            if (Util.isNumber(args[1])) {

                int id = Integer.parseInt(args[1]);
                Equip equip = ItemData.getEquipDeepCopyFromID(id, true);
                if (equip == null) {
                    Item item = ItemData.getItemDeepCopy(id, true);
                    if (item == null) {
                        chr.chatMessage(Mob, String.format("Could not find an item with id %d", id));
                        return;
                    }
                    short quant = 1;
                    if (args.length > 2) {
                        quant = Short.parseShort(args[2]);
                    }
                    item.setQuantity(quant);
                    chr.addItemToInventory(item);
                } else {
                    chr.addItemToInventory(InvType.EQUIP, equip, false);
                }
            } else {
                StringBuilder query = new StringBuilder();
                int size = args.length;
                short quant = 1;
                if (Util.isNumber(args[size - 1])) {
                    size--;
                    quant = Short.parseShort(args[size]);
                }
                for (int i = 1; i < size; i++) {
                    query.append(args[i].toLowerCase()).append(" ");
                }
                query = new StringBuilder(query.substring(0, query.length() - 1));
                Map<Integer, String> map = StringData.getItemStringByName(query.toString());
                if (map.size() == 0) {
                    chr.chatMessage(Mob, "No items found for query " + query);
                }
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    int id = entry.getKey();
                    Item item = ItemData.getEquipDeepCopyFromID(id, true);
                    if (item != null) {
                        Equip equip = (Equip) item;
                        if (equip.getItemId() < 1000000) {
                            continue;
                        }
                        chr.addItemToInventory(equip);
                        chr.getClient().write(WvsContext.inventoryOperation(true, false,
                                ADD, (short) equip.getBagIndex(), (byte) -1, 0, equip));
                        return;
                    }
                    item = ItemData.getItemDeepCopy(id);
                    if (item == null) {
                        continue;
                    }
                    item.setQuantity(quant);
                    chr.addItemToInventory(item);
                    chr.getClient().write(WvsContext.inventoryOperation(true, false,
                            ADD, (short) item.getBagIndex(), (byte) -1, 0, item));
                    return;
                }
            }
        }
    }

    @Command(names ={"cubes"}, requiredType = NONE)
    public static class GetCubes extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            giveItem(chr, 2049764, (short) 1);
            giveItem(chr, 5062010, (short) 50);
        }

        private static void giveItem(Char chr, int id, short quantity) {
            Equip equip = ItemData.getEquipDeepCopyFromID(id, true);
            if (equip == null) {
                Item item = ItemData.getItemDeepCopy(id, true);
                if (item == null) {
                    chr.chatMessage(Mob, String.format("Could not find an item with id %d", id));
                    return;
                }
                item.setQuantity(quantity);
                chr.addItemToInventory(item);
            } else {
                chr.addItemToInventory(InvType.EQUIP, equip, false);
            }
        }
    }

    @Command(names = {"save"}, requiredType = NONE)
    public static class SaveLocation extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            chr.setOldPosition(chr.getPosition());
        }
    }

    @Command(names = {"load"}, requiredType = NONE)
    public static class LoadLocation extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            if (!chr.getField().isRace()) {
                chr.write(CField.teleport(chr.getOldPosition(), chr));
            }
        }
    }

    @Command(names = {"ghost"}, requiredType = NONE)
    public static class AdjustGhost extends PlayerCommand {
        public static void execute(Char chr, String [] args) {
            if (Util.isNumber(args[1])) {
                int ghost = 0;
                try {
                    ghost = Integer.parseInt(args[1]);
                }catch (Exception e) {
                }
                chr.ghostSetting = ghost;
            } else {
                chr.ghostSetting = 0;
            }
        }
    }

    @Command(names = {"online"}, requiredType = NONE)
    public static class ListOnline extends PlayerCommand {
        public static void execute(Char chr, String[] args) {

            StringBuilder builder = new StringBuilder("Players Online: ");
            Collection<Char> onlineChars = chr.getClient().getChannelInstance().getChars().values();
            builder.append(onlineChars.size());
            for (Char cr : onlineChars) {
                if (builder.length() > 150) {
                    builder.setLength(builder.length() - 2);
                    chr.chatMessage(Notice2, builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(Char.makeMapleReadable(cr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            chr.chatMessage(Notice2, builder.toString());
        }
    }

    @Command(names = {"oz48"}, requiredType = NONE)
    public static class Oz48 extends PlayerCommand {
        public static void execute(Char chr, String [] args) {
            Field toField = chr.getOrCreateFieldByCurrentInstanceType(992048000);
            if (toField != null) {
                chr.warp(toField);
            }
        }
    }

    @Command(names = {"oz"}, requiredType = NONE)
    public static class Oz extends PlayerCommand {
        public static void execute(Char chr, String [] args) {
            if (Util.isNumber(args[1])) {
                int id = Integer.parseInt(args[1]);
                if (id < 1 || id > 50) {
                    chr.chatMessage(Mob, String.format("Invalid oz floor %d", id));
                    return;
                }
                int fieldId = 992_000_000;
                fieldId += id * 1000;
                Field toField = chr.getOrCreateFieldByCurrentInstanceType(fieldId);
                if (toField != null) {
                    chr.warp(toField);
                }
            }
        }
    }

    @Command(names ={"snowshoes"}, requiredType = NONE)
    public static class GetSnowshoes extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            giveItem(chr, 1072239); // yellow
        }

        private static void giveItem(Char chr, int id) {
            Equip equip = ItemData.getEquipDeepCopyFromID(id, true);
            if (equip == null) {
                Item item = ItemData.getItemDeepCopy(id, true);
                if (item == null) {
                    chr.chatMessage(Mob, String.format("Could not find an item with id %d", id));
                    return;
                }
                short quant = 1;
                item.setQuantity(quant);
                chr.addItemToInventory(item);
            } else {
                chr.addItemToInventory(InvType.EQUIP, equip, false);
            }
        }
    }

    @Command(names= {"race"}, requiredType = NONE)
    public static class JoinRace extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            int targetField = 932200005; // night
            if (args.length == 2) {
                if (Util.isNumber(args[1])) {
                    int id = Integer.parseInt(args[1]);
                    if (id == 2) {
                        targetField = 932200003; // sunset
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to sunset");
                    } else if (id == 3) {
                        targetField = 932200001; // day
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to daylight");
                    } else if (id == 4) {
                        targetField = 942001000;
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to new sunset");
                    } else if (id == 5) {
                        targetField = 942000000;
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to new night");
                    }
                } else if ("day".equalsIgnoreCase(args[1])) {
                    targetField = 932200001;
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to daylight");
                } else if ("sunset".equalsIgnoreCase(args[1])) {
                    targetField = 932200003;
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to sunset");
                } else {
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to night");
                }
            }

            Field toField = chr.getClient().getChannelInstance().getField(targetField);
            if (toField != null) {
                chr.warp(toField);
                if (chr.getLevel() < 50) {
                    int hp = 5000;
                    int lv = 50;
                    chr.setStatAndSendPacket(Stat.hp, hp);
                    chr.setStatAndSendPacket(Stat.mhp, hp);
                    chr.setStatAndSendPacket(Stat.mp, hp);
                    chr.setStatAndSendPacket(Stat.mmp, hp);
                    chr.setStatAndSendPacket(Stat.level, (short) lv);
                }
            }
        }
    }

    @Command(names= {"practice"}, requiredType = NONE)
    public static class JoinFlag extends PlayerCommand {
        public static void execute(Char chr, String[] args) {
            int targetField = 932200300; // night
            if (args.length == 2) {
                if (Util.isNumber(args[1])) {
                    int id = Integer.parseInt(args[1]);
                    if (id == 2) {
                        targetField = 932200200; // sunset
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to sunset");
                    } else if (id == 3) {
                        targetField = 932200100; // day
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to daylight");
                    } else if (id == 4) {
                        targetField = 942001500;
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to new sunset");
                    } else if (id == 5) {
                        targetField = 942002500;
                        chr.chatMessage(ChatType.SystemNotice, "Sending you to new night");
                    }
                } else if ("day".equalsIgnoreCase(args[1])) {
                    targetField = 932200100;
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to daylight");
                } else if ("sunset".equalsIgnoreCase(args[1])) {
                    targetField = 932200200;
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to sunset");
                } else {
                    chr.chatMessage(ChatType.SystemNotice, "Sending you to night");
                }
            }

            Field toField = chr.getOrCreateFieldByCurrentInstanceType(targetField);
            if (toField != null) {
                chr.warp(toField);
                if (chr.getLevel() < 50) {
                    int hp = 5000;
                    int lv = 50;
                    chr.setStatAndSendPacket(Stat.hp, hp);
                    chr.setStatAndSendPacket(Stat.mhp, hp);
                    chr.setStatAndSendPacket(Stat.mp, hp);
                    chr.setStatAndSendPacket(Stat.mmp, hp);
                    chr.setStatAndSendPacket(Stat.level, (short) lv);
                }
            }
        }
    }

}
