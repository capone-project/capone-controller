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

import org.abstractj.kalium.SodiumConstants;
import org.abstractj.kalium.crypto.SecretBox;

public class SymmetricKey {

    public static final int BYTES = SodiumConstants.XSALSA20_POLY1305_SECRETBOX_KEYBYTES;
    public static final int MACBYTES = SodiumConstants.BOXZERO_BYTES;

    public static class InvalidKeyException extends Exception {
    }

    public static class EncryptionException extends Exception {
    }

    public static class DecryptionException extends Exception {
    }

    public static class Nonce {
        private final byte[] nonce;

        public Nonce() {
            this.nonce = new byte[SodiumConstants.NONCE_BYTES];
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

    private final SecretBox key;

    private SymmetricKey(SecretBox key) {
        this.key = key;
    }

    public static SymmetricKey fromBytes(byte[] key) throws InvalidKeyException {
        try {
            return new SymmetricKey(new SecretBox(key));
        } catch (Exception e) {
            throw new InvalidKeyException();
        }
    }

    public String toString() {
        return key.toString();
    }

    public byte[] encrypt(Nonce nonce, byte[] message) throws EncryptionException {
        try {
            return key.encrypt(nonce.nonce, message);
        } catch (Exception e) {
            throw new EncryptionException();
        }
    }

    public byte[] decrypt(Nonce nonce, byte[] message) throws DecryptionException {
        try {
            return key.decrypt(nonce.nonce, message);
        } catch (Exception e) {
            throw new DecryptionException();
        }
    }

}
