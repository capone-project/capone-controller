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

import org.bouncycastle.jcajce.provider.digest.Blake2b;

public class Digest {

    private final Blake2b.Blake2b256 blake;

    public Digest() {
        this.blake = new Blake2b.Blake2b256();
    }

    public Digest update(byte[] data) {
        blake.update(data);
        return this;
    }

    public byte[] digest() {
        return blake.digest();
    }

}
