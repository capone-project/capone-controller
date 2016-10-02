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

package com.github.capone.protocol;

import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.crypto.VerifyKey;
import com.google.protobuf.nano.MessageNano;
import nano.Encryption;
import org.abstractj.kalium.Sodium;
import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.crypto.SecretBox;
import org.abstractj.kalium.keys.KeyPair;

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

        ByteBuffer sessionBuffer = ByteBuffer.wrap(new Random().randomBytes(4));
        int sessionid = sessionBuffer.getInt();

        Encryption.InitiatorKey initiatorKey = new Encryption.InitiatorKey();
        initiatorKey.sessionid = sessionid;
        initiatorKey.signPk = signKeys.getVerifyKey().toBytes();
        initiatorKey.ephmPk = emphKeys.getPublicKey().toBytes();
        writeProtobuf(initiatorKey);

        Encryption.ResponderKey responderKey = new Encryption.ResponderKey();
        readProtobuf(responderKey);

        if (responderKey.sessionid != sessionid) {
            throw new RuntimeException();
        }

        if (responderKey.signPk.length != VerifyKey.BYTES
                    || responderKey.ephmPk.length != SodiumConstants.PUBLICKEY_BYTES) {
            throw new RuntimeException();
        }

        ByteBuffer signBuffer = ByteBuffer.allocate(SodiumConstants.PUBLICKEY_BYTES * 4 + 4);
        signBuffer.put(responderKey.signPk);
        signBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(sessionid);
        signBuffer.put(responderKey.ephmPk);
        signBuffer.put(emphKeys.getPublicKey().toBytes());
        signBuffer.put(signKeys.getVerifyKey().toBytes());
        remoteKey.verify(signBuffer.array(), responderKey.signature);

        signBuffer.clear();
        signBuffer.put(signKeys.getVerifyKey().toBytes());
        signBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(sessionid);
        signBuffer.put(emphKeys.getPublicKey().toBytes());
        signBuffer.put(responderKey.ephmPk);
        signBuffer.put(responderKey.signPk);

        Encryption.AcknowledgeKey acknowledgeKey = new Encryption.AcknowledgeKey();
        acknowledgeKey.sessionid = sessionid;
        acknowledgeKey.signPk = signKeys.getVerifyKey().toBytes();
        acknowledgeKey.signature = signKeys.sign(signBuffer.array());
        writeProtobuf(acknowledgeKey);

        byte[] scalarmult = new byte[SodiumConstants.SCALAR_BYTES];
        Sodium.crypto_scalarmult_curve25519(scalarmult, emphKeys.getPrivateKey().toBytes(),
                                            responderKey.ephmPk);

        int bufferLength = scalarmult.length + SodiumConstants.PUBLICKEY_BYTES * 2;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        buffer.put(scalarmult);
        buffer.put(emphKeys.getPublicKey().toBytes());
        buffer.put(responderKey.ephmPk);

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
            if (read(pkg.array(), pkg.capacity()) < 0) {
                return null;
            }

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
        if (bytes == null) {
            throw new IOException("Channel received invalid protobuf");
        }
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

    protected abstract int read(byte[] msg, int len) throws IOException;

    public abstract void connect() throws IOException;

    public abstract void close() throws IOException;

}
