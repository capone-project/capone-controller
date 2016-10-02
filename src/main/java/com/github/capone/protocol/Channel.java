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

import com.github.capone.protocol.crypto.*;
import com.google.protobuf.nano.MessageNano;
import nano.Encryption;
import org.abstractj.kalium.Sodium;
import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.crypto.Random;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public abstract class Channel {

    private SymmetricKey.Nonce localNonce;
    private SymmetricKey.Nonce remoteNonce;
    private SymmetricKey key;

    public void enableEncryption(SigningKey signKeys, VerifyKey remoteKey)
            throws IOException, VerifyKey.SignatureException, SymmetricKey.InvalidKeyException {
        final PrivateKey emphKeys = PrivateKey.fromRandom();

        ByteBuffer sessionBuffer = ByteBuffer.wrap(new Random().randomBytes(4));
        int sessionid = sessionBuffer.getInt();

        Encryption.InitiatorKey initiatorKey = new Encryption.InitiatorKey();
        initiatorKey.sessionid = sessionid;
        initiatorKey.signPk = signKeys.getVerifyKey().toBytes();
        initiatorKey.ephmPk = emphKeys.getPublicKey().toBytes();
        try {
            writeProtobuf(initiatorKey);
        } catch (SymmetricKey.EncryptionException e) {
            /* no encryption in use yet, ignore */
        }

        Encryption.ResponderKey responderKey = new Encryption.ResponderKey();
        try {
            readProtobuf(responderKey);
        } catch (SymmetricKey.DecryptionException e) {
            /* no encryption in use yet, ignore */
        }

        if (responderKey.sessionid != sessionid) {
            throw new RuntimeException();
        }

        if (responderKey.signPk.length != VerifyKey.BYTES
                    || responderKey.ephmPk.length != PublicKey.BYTES) {
            throw new RuntimeException();
        }

        ByteBuffer signBuffer = ByteBuffer.allocate(PublicKey.BYTES * 4 + 4);
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
        try {
            writeProtobuf(acknowledgeKey);
        } catch (SymmetricKey.EncryptionException e) {
            /* no encryption in use yet, ignore */
        }

        byte[] scalarmult = new byte[SodiumConstants.SCALAR_BYTES];
        Sodium.crypto_scalarmult_curve25519(scalarmult, emphKeys.toBytes(),
                                            responderKey.ephmPk);

        int bufferLength = scalarmult.length + PublicKey.BYTES * 2;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        buffer.put(scalarmult);
        buffer.put(emphKeys.getPublicKey().toBytes());
        buffer.put(responderKey.ephmPk);

        byte[] symmetricKey = new byte[SymmetricKey.BYTES];
        Sodium.crypto_generichash_blake2b(symmetricKey, symmetricKey.length, buffer.array(),
                                          buffer.array().length, new byte[0], 0);

        enableEncryption(SymmetricKey.fromBytes(symmetricKey));
    }

    public void enableEncryption(SymmetricKey key) {
        this.key = key;
        this.localNonce = new SymmetricKey.Nonce();
        this.remoteNonce = new SymmetricKey.Nonce();
        this.remoteNonce.increment();
    }

    public boolean isEncrypted() {
        return this.key != null;
    }

    public void write(byte[] msg) throws IOException, SymmetricKey.EncryptionException {
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);

        byte[] pkg;
        ByteBuffer plain;
        if (isEncrypted()) {
            plain = ByteBuffer.allocate(512 - SymmetricKey.MACBYTES);
        } else {
            plain = ByteBuffer.allocate(512);
        }

        plain.order(ByteOrder.BIG_ENDIAN).putInt(msg.length);

        while (plain.position() > 0 || msgBuffer.hasRemaining()) {
            int len;
            if (isEncrypted()) {
                len = Math.min(plain.capacity() - plain.position(), msgBuffer.remaining());
            } else {
                len = Math.min(plain.capacity() - plain.position() - SymmetricKey.MACBYTES,
                               msgBuffer.remaining());
            }

            plain.put(msgBuffer.array(), msgBuffer.position(), len);
            msgBuffer.position(msgBuffer.position() + len);
            Arrays.fill(plain.array(), plain.position(), plain.capacity(), (byte) 0);

            if (isEncrypted()) {
                pkg = key.encrypt(localNonce, plain.array());
                localNonce.increment();
                localNonce.increment();
            } else {
                pkg = plain.array();
            }

            write(pkg, pkg.length);
            plain.clear();
        }
    }

    public byte[] read() throws IOException, SymmetricKey.DecryptionException {
        ByteBuffer message = null;
        ByteBuffer pkg = ByteBuffer.allocate(512);

        while (message == null || message.position() < message.capacity()) {
            if (read(pkg.array(), pkg.capacity()) < 0) {
                return null;
            }

            ByteBuffer plain;
            if (isEncrypted()) {
                plain = ByteBuffer.wrap(key.decrypt(remoteNonce, pkg.array()));
                remoteNonce.increment();
                remoteNonce.increment();
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

    public void writeProtobuf(MessageNano msg)
            throws IOException, SymmetricKey.EncryptionException {
        write(MessageNano.toByteArray(msg));
    }

    public <Message extends MessageNano> Message readProtobuf(Message msg)
            throws IOException, SymmetricKey.DecryptionException {
        byte[] bytes = read();
        if (bytes == null) {
            throw new IOException("Channel received invalid protobuf");
        }
        return Message.mergeFrom(msg, bytes);
    }

    protected abstract void write(byte[] msg, int len) throws IOException;

    protected abstract int read(byte[] msg, int len) throws IOException;

    public abstract void connect() throws IOException;

    public abstract void close() throws IOException;

}
