/*
    This file is part of Desu: MapleStory v62 Server Emulator
    Copyright (C) 2014  Zygon <watchmystarz@hotmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.swordie.ms.connection.netty;

import net.swordie.ms.connection.InPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.swordie.ms.connection.crypto.AESCipher;
import net.swordie.ms.connection.crypto.CIGCipher;
import org.apache.log4j.LogManager;

import java.util.List;

/**
 * Implementation of a Netty decoder pattern so that decryption of MapleStory
 * packets is possible. Follows steps using the special MapleAES as well as
 * ShandaCrypto (which became non-used after v149.2 in GMS).
 *
 * @author Zygon
 */
public class ProximityPacketDecoder extends ByteToMessageDecoder {
    private static final org.apache.log4j.Logger log = LogManager.getRootLogger();

    private int storedLength = -1;

    @Override
    protected void decode(ChannelHandlerContext chc, ByteBuf in, List<Object> out) {
        if (storedLength == -1) {
            if (in.readableBytes() < 2) {
                return;
            }
            int uDataLen = in.readShortLE();
            if (uDataLen > 0x50000) {
                log.error("Recv packet length overflow.");
                return;
            }
            storedLength = uDataLen;
        }
        if (in.readableBytes() >= storedLength) {
            byte[] dec = new byte[storedLength];
            in.readBytes(dec);

            storedLength = -1;
            InPacket inPacket = new InPacket(dec);
            out.add(inPacket);
        }
    }
}
