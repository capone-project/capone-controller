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

public class SigningKey {

    public static final int BYTES = SodiumConstants.SECRETKEY_BYTES;

    public static class InvalidSeedException extends Exception {
    }

    private final org.abstractj.kalium.keys.SigningKey key;

    private SigningKey(org.abstractj.kalium.keys.SigningKey key) {
        this.key = key;
    }

    public static SigningKey fromSeed(String seed) throws InvalidSeedException {
        try {
            return new SigningKey(new org.abstractj.kalium.keys.SigningKey(seed, Encoder.HEX));
        } catch (Exception e) {
            throw new InvalidSeedException();
        }
    }

    public static SigningKey fromRandom() {
        return new SigningKey(new org.abstractj.kalium.keys.SigningKey());
    }

    public VerifyKey getVerifyKey() {
        return new VerifyKey(key.getVerifyKey());
    }

    public byte[] sign(byte[] message) {
        return key.sign(message);
    }

    @Override
    public String toString() {
        return key.toString();
    }

}
