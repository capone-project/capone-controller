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

package com.github.capone.protocol.entities;

import android.os.Parcel;
import android.os.Parcelable;
import nano.Discovery;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Server implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Server createFromParcel(Parcel in) {
            return new Server(in);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };

    public String name;
    public String address;
    public Identity signatureKey;
    public List<Service> services;

    public Server() {
    }

    private Server(Parcel in) {
        name = in.readString();
        address = in.readString();
        signatureKey = in.readParcelable(Identity.class.getClassLoader());
        services = in.createTypedArrayList(Service.CREATOR);
    }

    @Override
    public String toString() {
        return signatureKey.toString();
    }

    public static Server fromAnnounce(String address, Discovery.DiscoverResult announce) {
        Server server = new Server();
        server.name = announce.name;
        server.address = address;
        server.signatureKey = new Identity(announce.identity);
        server.services = new ArrayList<>();

        for (Discovery.DiscoverResult.Service announcedService : announce.services) {
            server.services.add(new Service(announcedService));
        }

        return server;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeParcelable(signatureKey, flags);
        dest.writeTypedList(services);
    }

}
