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

package com.github.capone.persistence;

import com.github.capone.protocol.crypto.SigningKey;
import com.orm.SugarRecord;

import java.util.List;

public class SigningKeyRecord extends SugarRecord {

    private String keySeed;

    public SigningKeyRecord() {
    }

    public SigningKeyRecord(SigningKey key) {
        this.keySeed = key.toString();
    }

    public SigningKey getKey() throws SigningKey.InvalidSeedException {
        return SigningKey.fromSeed(keySeed);
    }

    public void setKey(SigningKey key) {
        this.keySeed = key.toString();
    }

    public static SigningKey getSigningKey() {
        try {
            List<SigningKeyRecord> identities = SigningKeyRecord.listAll(SigningKeyRecord.class);
            if (identities.size() == 1) {
                return identities.get(0).getKey();
            } else if (identities.size() == 0) {
                SigningKeyRecord identity = new SigningKeyRecord(SigningKey.fromRandom());
                identity.save();
                return identity.getKey();
            } else {
                throw new RuntimeException();
            }
        } catch (SigningKey.InvalidSeedException e) {
            throw new RuntimeException();
        }
    }

}
