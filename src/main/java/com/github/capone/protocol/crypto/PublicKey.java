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

import nano.Core;
import org.abstractj.kalium.SodiumConstants;

public class PublicKey {

    public static final int BYTES = SodiumConstants.PUBLICKEY_BYTES;

    public static class InvalidKeyException extends Exception {
    }

    private final org.abstractj.kalium.keys.PublicKey key;

    protected PublicKey(org.abstractj.kalium.keys.PublicKey key) {
        this.key = key;
    }

    public static PublicKey fromBytes(byte[] key) throws InvalidKeyException {
        try {
            return new PublicKey(new org.abstractj.kalium.keys.PublicKey(key));
        } catch (Exception e) {
            throw new InvalidKeyException();
        }
    }

    public static PublicKey fromMessage(Core.PublicKeyMessage msg) throws InvalidKeyException {
        try {
            return new PublicKey(new org.abstractj.kalium.keys.PublicKey(msg.data));
        } catch (Exception e) {
            throw new InvalidKeyException();
        }
    }

    public byte[] toBytes() {
        return key.toBytes();
    }

    public Core.PublicKeyMessage toMessage() {
        Core.PublicKeyMessage msg = new Core.PublicKeyMessage();
        msg.data = toBytes();
        return msg;
    }

}
