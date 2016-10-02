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

import com.github.capone.protocol.crypto.VerifyKey;
import com.orm.SugarRecord;
import com.github.capone.protocol.entities.Server;

import java.util.List;

public class ServerRecord extends SugarRecord {

    private String name;
    private String publicKey;
    private String address;

    public ServerRecord() {
    }

    public ServerRecord(Server server) {
        this.publicKey = server.signatureKey.toString();
        this.address = server.address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VerifyKey getPublicKey() {
        try {
            return VerifyKey.fromString(publicKey);
        } catch (VerifyKey.InvalidKeyException e) {
            throw new RuntimeException();
        }
    }

    public void setPublicKey(VerifyKey publicKey) {
        this.publicKey = publicKey.toString();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static ServerRecord findByTo(Server server) {
        List<ServerRecord> favorites = find(ServerRecord.class, "public_key = ? and address = ?",
                                            server.signatureKey.toString(), server.address);

        if (favorites.isEmpty()) {
            return null;
        } else if (favorites.size() > 1) {
            throw new RuntimeException();
        }

        return favorites.get(0);
    }

}
