/*
 * Copyright (C) 2016 Patrick Steinhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package im.pks.sd.protocol;

import com.google.protobuf.nano.MessageNano;
import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PublicKey;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Channel {

    private KeyPair localKeys;
    private PublicKey remoteKey;
    private byte[] localNonce;
    private byte[] remoteNonce;

    public Channel() {
        this.localKeys = null;
        this.remoteKey = null;
    }

    public void setEncrypted(KeyPair localKeys, PublicKey remoteKey) {
        this.localKeys = localKeys;
        this.remoteKey = remoteKey;
    }

    public boolean isEncrypted() {
        return this.localKeys != null && this.remoteKey != null;
    }

    public void write(byte[] msg) throws IOException {
        byte[] data;
        ByteBuffer len = ByteBuffer.wrap(new byte[4]);

        if (isEncrypted()) {
            data = new byte[msg.length + NaCl.Sodium.HMACSHA512256_BYTES];
            if (NaCl.sodium().crypto_secretbox_xsalsa20poly1305(data, msg, msg.length, localNonce, localKeys.getPrivateKey().toBytes()) < 0) {
                throw new RuntimeException();
            }
        } else {
            data = msg;
        }

        len.order(ByteOrder.BIG_ENDIAN).putLong(data.length);
        write(len.array(), len.array().length);
        write(data, data.length);
    }

    public byte[] read() throws IOException {
        ByteBuffer lenBuf = ByteBuffer.wrap(new byte[4]);
        read(lenBuf.array(), lenBuf.array().length);

        long len = lenBuf.order(ByteOrder.nativeOrder()).getLong();
        if (len > Integer.MAX_VALUE || len < 0) {
            return null;
        }

        byte[] buf = new byte[(int) len];
        read(buf, (int) len);

        if (isEncrypted()) {
            if (len - NaCl.Sodium.HMACSHA512256_BYTES < 0) {
                throw new RuntimeException();
            }

            byte[] msg = new byte[(int) (len - NaCl.Sodium.HMACSHA512256_BYTES)];
            if (NaCl.sodium().crypto_secretbox_xsalsa20poly1305_open(msg, buf, len, remoteNonce, remoteKey.toBytes()) < 0) {
                throw new RuntimeException();
            }
        }

        return buf;
    }

    public void writeProtobuf(MessageNano msg) throws IOException {
        write(MessageNano.toByteArray(msg));
    }

    public <Message extends MessageNano> Message readProtobuf(Message msg)
            throws IOException {
        return Message.mergeFrom(msg, read());
    }

    protected abstract void write(byte[] msg, int len) throws IOException;
    protected abstract void read(byte[] msg, int len) throws IOException;
}
