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

package com.github.capone.protocol.crypto;

import org.abstractj.kalium.Sodium;
import org.abstractj.kalium.SodiumConstants;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Hex;

public class SymmetricKey {

    public static final int BYTES = 32;
    public static final int MACBYTES = 16;
    public static final int NONCEBYTES = 24;
    public static final int SUBKEYBYTES = 32;

    public static class InvalidKeyException extends Exception {
    }

    public static class EncryptionException extends Exception {
    }

    public static class DecryptionException extends Exception {
    }

    public static class Nonce {
        private final byte[] nonce;

        public Nonce() {
            this.nonce = new byte[NONCEBYTES];
        }

        public void increment() {
            byte c = 1;

            for (int i = 0; i < nonce.length; i++) {
                c += nonce[i];
                nonce[i] = c;
                c >>= 8;
            }
        }

    }

    private final KeyParameter key;

    private SymmetricKey(KeyParameter parameter) {
        this.key = parameter;
    }

    public static SymmetricKey fromBytes(byte[] key) throws InvalidKeyException {
        if (key.length != BYTES) {
            throw new InvalidKeyException();
        }

        return new SymmetricKey(new KeyParameter(key));
    }

    public static SymmetricKey fromScalarMultiplication(PrivateKey sk, PublicKey pk)
            throws InvalidKeyException {
        return fromScalarMultiplication(sk, pk, true);
    }

    protected static SymmetricKey fromScalarMultiplication(PrivateKey sk, PublicKey pk,
                                                           boolean localKeyFirst)
            throws InvalidKeyException {
        byte[] scalarmult = new byte[SodiumConstants.SCALAR_BYTES];
        Sodium.crypto_scalarmult_curve25519(scalarmult, sk.toBytes(), pk.toBytes());

        Digest digest = new Digest();
        digest.update(scalarmult);
        if (localKeyFirst) {
            digest.update(sk.getPublicKey().toBytes());
            digest.update(pk.toBytes());
        } else {
            digest.update(pk.toBytes());
            digest.update(sk.getPublicKey().toBytes());
        }

        return SymmetricKey.fromBytes(digest.digest());
    }

    public String toString() {
        return Hex.toHexString(key.getKey());
    }

    public byte[] encrypt(Nonce nonce, byte[] message) throws EncryptionException {
        try {
            byte[] subkey = new byte[SUBKEYBYTES];
            byte[] encrypted = new byte[message.length + MACBYTES];

            StreamCipher cipher = new XSalsa20Engine();
            cipher.init(true, new ParametersWithIV(key, nonce.nonce));
            cipher.processBytes(subkey, 0, subkey.length, subkey, 0);
            cipher.processBytes(message, 0, message.length, encrypted, MACBYTES);

            Mac auth = new Poly1305();
            auth.init(new KeyParameter(subkey));
            auth.update(encrypted, MACBYTES, message.length);
            auth.doFinal(encrypted, 0);

            return encrypted;
        } catch (Exception e) {
            throw new EncryptionException();
        }
    }

    public byte[] decrypt(Nonce nonce, byte[] encrypted) throws DecryptionException {
        byte[] subkey = new byte[SUBKEYBYTES];
        byte[] mac = new byte[MACBYTES];
        byte[] plain = new byte[encrypted.length - MACBYTES];

        if (encrypted.length < MACBYTES) {
            throw new DecryptionException();
        }

        StreamCipher cipher = new XSalsa20Engine();
        cipher.init(true, new ParametersWithIV(key, nonce.nonce));
        cipher.processBytes(subkey, 0, subkey.length, subkey, 0);

        Mac auth = new Poly1305();
        auth.init(new KeyParameter(subkey));
        auth.update(encrypted, MACBYTES, encrypted.length - MACBYTES);
        auth.doFinal(mac, 0);

        boolean validMac = true;
        for (int i = 0; i < MACBYTES; i++) {
            validMac &= mac[i] == encrypted[i];
        }
        if (!validMac) {
            throw new DecryptionException();
        }

        cipher.processBytes(encrypted, MACBYTES, encrypted.length - MACBYTES, plain, 0);

        return plain;
    }

}
