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

package im.pks.sd.entities;

import nano.Discovery;
import org.abstractj.kalium.keys.PublicKey;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServerTo implements Serializable {
    public String publicKey;
    public String address;
    public List<ServiceTo> services;

    public static ServerTo fromAnnounce(String address, Discovery.AnnounceMessage announce) {
        ServerTo server = new ServerTo();
        server.publicKey = new PublicKey(announce.signKey).toString();
        server.address = address;
        server.services = new ArrayList<>();

        for (Discovery.AnnounceMessage.Service announcedService : announce.services) {
            ServiceTo service = new ServiceTo();
            service.name = announcedService.name;
            service.category = announcedService.category;
            service.port = Integer.valueOf(announcedService.port);
            server.services.add(service);
        }
        return server;
    }

    @Override
    public String toString() {
        return publicKey;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
