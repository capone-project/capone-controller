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

package com.github.capone.entities;

import android.os.Parcel;
import android.os.Parcelable;
import nano.Discovery;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class ServerTo implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public ServerTo createFromParcel(Parcel in) {
            return new ServerTo(in);
        }

        @Override
        public ServerTo[] newArray(int size) {
            return new ServerTo[size];
        }
    };

    public String name;
    public String address;
    public SignatureKeyTo signatureKey;
    public List<ServiceTo> services;

    public ServerTo() {
    }

    private ServerTo(Parcel in) {
        name = in.readString();
        address = in.readString();
        signatureKey = in.readParcelable(SignatureKeyTo.class.getClassLoader());
        services = in.createTypedArrayList(ServiceTo.CREATOR);
    }

    @Override
    public String toString() {
        return signatureKey.toString();
    }

    public static ServerTo fromAnnounce(String address, Discovery.DiscoverResult announce) {
        ServerTo server = new ServerTo();
        server.name = announce.name;
        server.address = address;
        server.signatureKey = new SignatureKeyTo(announce.signKey);
        server.services = new ArrayList<>();

        for (Discovery.DiscoverResult.Service announcedService : announce.services) {
            server.services.add(new ServiceTo(announcedService));
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
