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
import org.abstractj.kalium.encoders.Encoder;

public class VerifyKey {

    public static final int BYTES = SodiumConstants.PUBLICKEY_BYTES;

    public static class SignatureException extends Exception {
    }

    public static class InvalidKeyException extends Exception {
    }

    private final org.abstractj.kalium.keys.VerifyKey key;

    protected VerifyKey(org.abstractj.kalium.keys.VerifyKey key) {
        this.key = key;
    }

    public static VerifyKey fromBytes(byte[] key) throws InvalidKeyException {
        try {
            return new VerifyKey(new org.abstractj.kalium.keys.VerifyKey(key));
        } catch (Exception e) {
            throw new InvalidKeyException();
        }
    }

    public static VerifyKey fromString(String key) throws InvalidKeyException {
        try {
            return new VerifyKey(new org.abstractj.kalium.keys.VerifyKey(key, Encoder.HEX));
        } catch (Exception e) {
            throw new InvalidKeyException();
        }
    }

    public byte[] toBytes() {
        return key.toBytes();
    }

    @Override
    public String toString() {
        return key.toString();
    }

    public void verify(byte[] message, byte[] signature) throws SignatureException {
        if (signature.length != SodiumConstants.SIGNATURE_BYTES)
            throw new SignatureException();

        try {
            if (!key.verify(message, signature))
                throw new SignatureException();
        } catch (org.abstractj.kalium.keys.VerifyKey.SignatureException e) {
            throw new SignatureException();
        }
    }

}
