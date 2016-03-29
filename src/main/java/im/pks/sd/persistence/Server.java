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

package im.pks.sd.persistence;

import com.orm.SugarRecord;
import im.pks.sd.entities.ServerTo;

import java.util.List;

public class Server extends SugarRecord {

    private String name;
    private String publicKey;
    private String address;

    public Server() {
    }

    public Server(ServerTo server) {
        this.publicKey = server.publicKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static Server findByNaturalKey(String publicKey, String address) {
        List<Server> favorites = find(Server.class, "public_key = ? and address = ?",
                                      publicKey, address);

        if (favorites.isEmpty()) {
            return null;
        } else if (favorites.size() > 1) {
            throw new RuntimeException();
        }

        return favorites.get(0);
    }

}
