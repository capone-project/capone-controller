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
import java.util.Arrays;

public abstract class Channel {

    private byte[] localNonce;
    private byte[] remoteNonce;
    private SecretBox key;

    public void enableEncryption(SigningKey signKeys, VerifyKey remoteKey)
            throws IOException, VerifyKey.SignatureException {
        final KeyPair emphKeys = new KeyPair();
        ByteBuffer signBuffer;

        signBuffer = ByteBuffer.allocate(SodiumConstants.PUBLICKEY_BYTES * 2);
        signBuffer.put(signKeys.getVerifyKey().toBytes());
        signBuffer.put(emphKeys.getPublicKey().toBytes());

        Encryption.SignedKey signedKey = new Encryption.SignedKey();
        signedKey.signPk = signKeys.getVerifyKey().toBytes();
        signedKey.encryptPk = emphKeys.getPublicKey().toBytes();
        signedKey.signature = signKeys.sign(signBuffer.array());
        writeProtobuf(signedKey);

        Encryption.SignedKeys signedRemoteKeys = new Encryption.SignedKeys();
        readProtobuf(signedRemoteKeys);

        if (signedRemoteKeys.signature.length != SodiumConstants.SIGNATURE_BYTES
                    || signedRemoteKeys.senderPk.length != SodiumConstants.PUBLICKEY_BYTES
                    || signedRemoteKeys.receiverPk.length != SodiumConstants.PUBLICKEY_BYTES) {
            throw new RuntimeException();
        }

        if (!Arrays.equals(signedRemoteKeys.receiverPk, emphKeys.getPublicKey().toBytes())) {
            throw new RuntimeException();
        }

        signBuffer.clear();
        signBuffer.put(signedRemoteKeys.senderPk);
        signBuffer.put(signedRemoteKeys.receiverPk);
        remoteKey.verify(signBuffer.array(), signedRemoteKeys.signature);

        signBuffer.clear();
        signBuffer.put(emphKeys.getPublicKey().toBytes());
        signBuffer.put(signedRemoteKeys.senderPk);
        Encryption.SignedKeys signedKeys = new Encryption.SignedKeys();
        signedKeys.senderPk = emphKeys.getPublicKey().toBytes();
        signedKeys.receiverPk = signedRemoteKeys.senderPk;
        signedKeys.signature = signKeys.sign(signBuffer.array());
        writeProtobuf(signedKeys);

        byte[] scalarmult = new byte[SodiumConstants.SCALAR_BYTES];
        Sodium.crypto_scalarmult_curve25519(scalarmult, emphKeys.getPrivateKey().toBytes(),
                                            signedRemoteKeys.senderPk);

        int bufferLength = scalarmult.length + SodiumConstants.PUBLICKEY_BYTES * 2;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        buffer.put(scalarmult);
        buffer.put(emphKeys.getPublicKey().toBytes());
        buffer.put(signedRemoteKeys.senderPk);

        byte[] symmetricKey = new byte[SodiumConstants.XSALSA20_POLY1305_SECRETBOX_KEYBYTES];
        Sodium.crypto_generichash_blake2b(symmetricKey, symmetricKey.length, buffer.array(),
                                          buffer.array().length, new byte[0], 0);

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
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);

        byte[] pkg;
        ByteBuffer plain;
        if (isEncrypted()) {
            plain = ByteBuffer.allocate(512 - SodiumConstants.BOXZERO_BYTES);
        } else {
            plain = ByteBuffer.allocate(512);
        }

        plain.order(ByteOrder.BIG_ENDIAN).putInt(msg.length);

        while (plain.position() > 0 || msgBuffer.hasRemaining()) {
            int len;
            if (isEncrypted()) {
                len = Math.min(plain.capacity() - plain.position(), msgBuffer.remaining());
            } else {
                len = Math.min(plain.capacity() - plain.position() - SodiumConstants.BOXZERO_BYTES,
                               msgBuffer.remaining());
            }

            plain.put(msgBuffer.array(), msgBuffer.position(), len);
            msgBuffer.position(msgBuffer.position() + len);
            Arrays.fill(plain.array(), plain.position(), plain.capacity(), (byte) 0);

            if (isEncrypted()) {
                pkg = key.encrypt(localNonce, plain.array());
                incrementNonce(localNonce);
                incrementNonce(localNonce);
            } else {
                pkg = plain.array();
            }

            write(pkg, pkg.length);
            plain.clear();
        }
    }

    public byte[] read() throws IOException {
        ByteBuffer message = null;
        ByteBuffer pkg = ByteBuffer.allocate(512);

        while (message == null || message.position() < message.capacity()) {
            read(pkg.array(), pkg.capacity());

            ByteBuffer plain;
            if (isEncrypted()) {
                plain = ByteBuffer.wrap(key.decrypt(remoteNonce, pkg.array()));
                incrementNonce(remoteNonce);
                incrementNonce(remoteNonce);
            } else {
                plain = ByteBuffer.wrap(pkg.array());
            }

            if (message == null) {
                message = ByteBuffer.allocate(plain.order(ByteOrder.BIG_ENDIAN).getInt());
            }

            message.put(plain.array(), plain.position(),
                        Math.min(message.remaining(), plain.remaining()));

            pkg.clear();
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
