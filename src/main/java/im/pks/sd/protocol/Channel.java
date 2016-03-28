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
import nano.Encryption;
import org.abstractj.kalium.Sodium;
import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.crypto.SecretBox;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Channel {

    private byte[] localNonce;
    private byte[] remoteNonce;
    private SecretBox key;

    public void enableEncryption(SigningKey localKeys, VerifyKey remoteKey) throws IOException, VerifyKey.SignatureException {
        final KeyPair keys = new KeyPair();

        Encryption.SessionKeyMessage sessionKey = new Encryption.SessionKeyMessage();
        sessionKey.signPk = localKeys.getVerifyKey().toBytes();
        sessionKey.encryptPk = keys.getPublicKey().toBytes();
        sessionKey.signature = localKeys.sign(sessionKey.encryptPk);
        writeProtobuf(sessionKey);

        Encryption.SessionKeyMessage remoteSessionKey = new Encryption.SessionKeyMessage();
        readProtobuf(remoteSessionKey);

        remoteKey.verify(remoteSessionKey.encryptPk, remoteSessionKey.signature);

        byte[] scalarmult = new byte[SodiumConstants.SCALAR_BYTES];
        Sodium.crypto_scalarmult_curve25519(scalarmult, keys.getPrivateKey().toBytes(),
                remoteSessionKey.encryptPk);

        int bufferLength = scalarmult.length + keys.getPublicKey().toBytes().length +
                remoteSessionKey.encryptPk.length;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        buffer.put(scalarmult);
        buffer.put(keys.getPublicKey().toBytes());
        buffer.put(remoteSessionKey.encryptPk);

        byte[] symmetricKey = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_KEYBYTES];
        Sodium.crypto_generichash_blake2b(symmetricKey, symmetricKey.length, buffer.array(),
                buffer.array().length, new byte[0], 0);

        enableEncryption(new SecretBox(symmetricKey));
    }

    public void enableEncryption(SecretBox key) throws IOException, VerifyKey.SignatureException {
        this.key = key;
        this.localNonce = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_NONCEBYTES];
        this.remoteNonce = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_NONCEBYTES];
        incrementNonce(this.remoteNonce);
    }

    public boolean isEncrypted() {
        return this.key != null;
    }

    public void write(byte[] msg) throws IOException {
        ByteBuffer len = ByteBuffer.allocate(4);

        if (isEncrypted()) {
            msg = key.encrypt(localNonce, msg);
            incrementNonce(localNonce);
            incrementNonce(localNonce);
        }

        len.order(ByteOrder.BIG_ENDIAN).putInt(msg.length);
        write(len.array(), len.array().length);
        write(msg, msg.length);
    }

    public byte[] read() throws IOException {
        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        read(lenBuf.array(), lenBuf.array().length);

        int len = lenBuf.order(ByteOrder.BIG_ENDIAN).getInt();
        if (len < 0) {
            return null;
        }

        byte[] buf = new byte[len];
        read(buf, len);

        if (isEncrypted()) {
            buf = key.decrypt(remoteNonce, buf);
            incrementNonce(remoteNonce);
            incrementNonce(remoteNonce);
        }

        return buf;
    }

    public void writeProtobuf(MessageNano msg) throws IOException {
        write(MessageNano.toByteArray(msg));
    }

    public <Message extends MessageNano> Message readProtobuf(Message msg)
            throws IOException {
        byte[] bytes = read();
        return Message.mergeFrom(msg, bytes);
    }

    private void incrementNonce(byte[] buf) {
        byte c = 1;

        for (int i = 0; i < buf.length; i++) {
            c += buf[i];
            buf[i] = c;
            c >>= 8;
        }
    }

    protected abstract void write(byte[] msg, int len) throws IOException;

    protected abstract void read(byte[] msg, int len) throws IOException;

    public abstract void connect() throws IOException;

    public abstract void close() throws IOException;

}
