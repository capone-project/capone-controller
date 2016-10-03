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

        Encryption.EncryptionInitiationMessage initiatorKey = new Encryption.EncryptionInitiationMessage();
        initiatorKey.identity = signKeys.getVerifyKey().toMessage();
        initiatorKey.ephemeral = emphKeys.getPublicKey().toMessage();
        try {
            writeProtobuf(initiatorKey);
        } catch (SymmetricKey.EncryptionException e) {
            /* no encryption in use yet, ignore */
        }

        PublicKey remoteEmphKey;
        VerifyKey remoteSignKey;
        byte[] signature;
        try {
            Encryption.EncryptionAcknowledgementMessage msg =
                    new Encryption.EncryptionAcknowledgementMessage();
            readProtobuf(msg);

            remoteEmphKey = PublicKey.fromMessage(msg.ephemeral);
            remoteSignKey = VerifyKey.fromMessage(msg.identity);
            signature = msg.signature;
        } catch (SymmetricKey.DecryptionException e) {
            throw new RuntimeException();
        } catch (PublicKey.InvalidKeyException | VerifyKey.InvalidKeyException e) {
            throw new SymmetricKey.InvalidKeyException();
        }

        ByteBuffer signBuffer = ByteBuffer.allocate(PublicKey.BYTES * 4 + 4);
        signBuffer.put(remoteSignKey.toBytes());
        signBuffer.put(remoteEmphKey.toBytes());
        signBuffer.put(emphKeys.getPublicKey().toBytes());
        signBuffer.put(signKeys.getVerifyKey().toBytes());
        remoteKey.verify(signBuffer.array(), signature);

        signBuffer.clear();
        signBuffer.put(signKeys.getVerifyKey().toBytes());
        signBuffer.put(emphKeys.getPublicKey().toBytes());
        signBuffer.put(remoteEmphKey.toBytes());
        signBuffer.put(remoteSignKey.toBytes());

        Encryption.EncryptionAcknowledgementMessage ack =
                new Encryption.EncryptionAcknowledgementMessage();
        ack.ephemeral = emphKeys.getPublicKey().toMessage();
        ack.identity = signKeys.getVerifyKey().toMessage();
        ack.signature = signKeys.sign(signBuffer.array());
        try {
            writeProtobuf(ack);
        } catch (SymmetricKey.EncryptionException e) {
            /* no encryption in use yet, ignore */
        }

        enableEncryption(SymmetricKey.fromScalarMultiplication(emphKeys, remoteEmphKey));
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
