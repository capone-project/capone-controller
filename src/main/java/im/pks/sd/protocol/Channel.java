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
import java.nio.BufferOverflowException;
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

        byte[] ephemeralKeySignature = localKeys.sign(symmetricKey);
        Encryption.EphemeralKeySignatureMessage ephemeralSignatureMessage =
                new Encryption.EphemeralKeySignatureMessage();
        ephemeralSignatureMessage.signature = ephemeralKeySignature;
        writeProtobuf(ephemeralSignatureMessage);

        Encryption.EphemeralKeySignatureMessage remoteEphemeralSignatureMessage =
                new Encryption.EphemeralKeySignatureMessage();
        readProtobuf(remoteEphemeralSignatureMessage);
        remoteKey.verify(symmetricKey, remoteEphemeralSignatureMessage.signature);

        enableEncryption(new SecretBox(symmetricKey));
    }

    public void enableEncryption(SecretBox key) {
        this.key = key;
        this.localNonce = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_NONCEBYTES];
        this.remoteNonce = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_NONCEBYTES];
        incrementNonce(this.remoteNonce);
    }

    public boolean isEncrypted() {
        return this.key != null;
    }

    public void write(byte[] msg) throws IOException {
        if (isEncrypted()) {
            writeEncrypted(msg);
        } else {
            writeUnencrypted(msg);
        }
    }

    private void writeEncrypted(byte[] msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        ByteBuffer plain = ByteBuffer.allocate(512 - SodiumConstants.BOXZERO_BYTES);
        plain.order(ByteOrder.BIG_ENDIAN).putInt(msg.length);

        while (buffer.hasRemaining()) {
            try {
                plain.put(buffer);
            } catch (BufferOverflowException e) {
                /* ignore */
            }

            byte[] cipher = key.encrypt(localNonce, plain.array());
            incrementNonce(localNonce);
            incrementNonce(localNonce);
            write(cipher, cipher.length);
        }
    }

    private void writeUnencrypted(byte[] msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg);

        ByteBuffer packet = ByteBuffer.allocate(512);
        packet.order(ByteOrder.BIG_ENDIAN).putInt(msg.length);

        while (buffer.hasRemaining()) {
            buffer.get(packet.array(), packet.position(), Math.min(packet.remaining(),
                                                                   buffer.remaining()));

            write(packet.array(), packet.capacity());
            packet.clear();
        }
    }

    public byte[] read() throws IOException {
        if (isEncrypted()) {
            return readEncrypted();
        } else {
            return readUnencrypted();
        }
    }

    private byte[] readEncrypted() throws IOException {
        ByteBuffer cipher = ByteBuffer.allocate(512);
        read(cipher.array(), cipher.capacity());

        ByteBuffer decrypted = ByteBuffer.wrap(key.decrypt(remoteNonce, cipher.array()));
        incrementNonce(remoteNonce);
        incrementNonce(remoteNonce);

        int len = decrypted.order(ByteOrder.BIG_ENDIAN).getInt();
        ByteBuffer plain = ByteBuffer.allocate(len);
        plain.put(decrypted.array(), decrypted.position(),
                  Math.min(decrypted.remaining(), plain.remaining()));

        while (plain.position() < plain.capacity()) {
            cipher.clear();
            read(cipher.array(), cipher.capacity());

            plain.put(key.decrypt(remoteNonce, cipher.array()),
                      0, Math.min(decrypted.capacity(), plain.remaining()));
            incrementNonce(remoteNonce);
            incrementNonce(remoteNonce);
        }

        return plain.array();
    }

    private byte[] readUnencrypted() throws IOException {
        ByteBuffer pkg = ByteBuffer.allocate(512);
        read(pkg.array(), pkg.capacity());

        int len = pkg.order(ByteOrder.BIG_ENDIAN).getInt();
        ByteBuffer message = ByteBuffer.allocate(len);
        message.put(pkg.array(), pkg.position(), len);

        while (message.position() < message.capacity()) {
            pkg.clear();
            read(pkg.array(), pkg.capacity());

            try {
                message.put(pkg);
            } catch (BufferOverflowException e) {
                /* ignore */
            }
        }

        return message.array();
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
