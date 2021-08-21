package net.swordie.ms.connection.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import net.swordie.ms.connection.InPacket;
import net.swordie.ms.connection.OutPacket;
import org.kleric.proximity.DiscordConnector;
import org.kleric.proximity.DiscordLobbyManager;

public class ProximityServerHandler extends SimpleChannelInboundHandler<InPacket> { // (1)

    private long discordId;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InPacket inPacket) throws Exception {
        short op = inPacket.decodeShort();
        switch (op) {
            case 0: {
                long id = inPacket.decodeLong();
                discordId = id;
                DiscordConnector.getInstance().onConnect(id, channelHandlerContext);
                break;
            }
            case 1: { // spectate
                int charId = inPacket.decodeInt();
                DiscordConnector.getInstance().requestFollow(discordId, charId);

                break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (discordId != 0) {
            DiscordConnector.getInstance().onDisconnect(discordId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
