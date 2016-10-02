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

public class PrivateKey {

    private final org.abstractj.kalium.keys.PrivateKey sk;
    private final PublicKey pk;

    private PrivateKey(org.abstractj.kalium.keys.KeyPair keyPair) {
        this.sk = keyPair.getPrivateKey();
        this.pk = new PublicKey(keyPair.getPublicKey());
    }

    public static PrivateKey fromRandom() {
        return new PrivateKey(new org.abstractj.kalium.keys.KeyPair());
    }

    public PublicKey getPublicKey() {
        return pk;
    }

    public byte[] toBytes() {
        return sk.toBytes();
    }

}
