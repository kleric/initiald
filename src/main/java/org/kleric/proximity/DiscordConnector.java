package org.kleric.proximity;

import io.netty.channel.ChannelHandlerContext;
import net.swordie.ms.Server;
import net.swordie.ms.client.Account;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.OutPacket;
import net.swordie.ms.connection.netty.ChannelHandler;
import net.swordie.ms.connection.packet.User;
import net.swordie.ms.util.Position;
import net.swordie.ms.world.Channel;
import net.swordie.ms.world.field.Field;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordConnector {

    private DiscordConnector() {
    }

    private static DiscordConnector instance;

    public final HashMap<Integer, Char> accountCharMap = new HashMap<>();
    private final Map<Integer, Long> accountIdMap = new HashMap<>();
    private final Map<Long, Integer> discordAccountMap = new HashMap<>();

    public static DiscordConnector getInstance() {
        if (instance == null) {
            synchronized (DiscordConnector.class) {
                if (instance == null) {
                    instance = new DiscordConnector();
                }
            }
        }
        return instance;
    }

    public void saveDiscordId(Account account, long discordId) {
        accountIdMap.put(account.getId(), discordId);
        discordAccountMap.put(discordId, account.getId());
    }

    public Long getDiscordId(int accId) {
        return accountIdMap.get(accId);
    }

    public ChannelHandlerContext getContext(Char chr) {
        Long discordId = getDiscordId(chr.getAccId());
        if (discordId == null) return null;
        return getContext(discordId);
    }

    public void updateAccountCharMap(Char c) {
        accountCharMap.put(c.getAccId(), c);
    }

    public void onAddedToLobby(Char c, String token) {
        Long discordId = getDiscordId(c.getAccId());
        if (discordId == null) {
            return;
        }
        ChannelHandlerContext context = getContext(discordId);
        if (context == null) {
            return;
        }

        OutPacket joinLobby = new OutPacket(1);
        joinLobby.encodeString(token);
        context.channel().writeAndFlush(joinLobby);
    }

    public void onRemovedFromLobby(Char c) {
        accountCharMap.remove(c.getAccId());
        Long discordId = getDiscordId(c.getAccId());
        if (discordId == null) {
            return;
        }
        ChannelHandlerContext context = getContext(discordId);
        if (context != null) {
            OutPacket joinLobby = new OutPacket(3);
            context.channel().writeAndFlush(joinLobby);
        }
    }

    private final HashMap<Long, ChannelHandlerContext> idMap = new HashMap<>();

    public void onConnect(long discordId, ChannelHandlerContext context) {
        idMap.put(discordId, context);
        Integer accId = discordAccountMap.get(discordId);
        if (accId != null) {
            Char c = accountCharMap.get(accId);
            if (c != null) {
                Field f = c.getField();
                if (f != null) {
                    String voiceLobby = f.getVoiceLobby();
                    if (voiceLobby != null) {
                        OutPacket joinLobby = new OutPacket(1);
                        joinLobby.encodeString(voiceLobby);
                        context.channel().writeAndFlush(joinLobby);
                    }
                }
                if (c.isCamera()) {
                    c.refreshCameraField();
                }
            }
        }
    }

    public void requestFollow(long discordId, int targetCharId) {
        Integer accId = discordAccountMap.get(discordId);
        if (accId != null) {
            Char cr = accountCharMap.get(accId);
            /*List<Channel> channels = Server.getInstance().getWorlds().get(0).getChannels();
            for (Channel c : channels) {
                Collection<Char> onlineChars = c.getChars().values();
                for (Char cr : onlineChars) {
                    if (cr.getAccId() == accId) {
                        Char driver = cr.getField().getCharByID(targetCharId);
                        if (driver != null) {
                            cr.write(User.followCharacter(cr.getId(), driver.getId(), false, new Position()));
                            System.out.println("Following..?");
                        } else {
                            cr.write(User.followCharacter(cr.getId(), 0, false, new Position()));
                            System.out.println("Null Driver");
                        }
                    }
                }
            }*/
            if (cr != null) {
                Char driver = cr.getField().getCharByID(targetCharId);
                if (driver != null) {
                    cr.write(User.followCharacter(cr.getId(), driver.getId(), false, new Position()));
                } else {
                    cr.write(User.followCharacter(cr.getId(), 0, false, new Position()));
                }
            }
        } else {
            System.out.println("Null acct");
        }
    }

    public void onDisconnect(long id) {
        idMap.remove(id);
    }

    public ChannelHandlerContext getContext(long discordId) {
        return idMap.get(discordId);
    }

}
