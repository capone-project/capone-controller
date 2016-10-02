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
import nano.Capone;

public class ServiceDescriptionTo implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public ServiceDescriptionTo createFromParcel(Parcel in) {
            return new ServiceDescriptionTo(in);
        }

        @Override
        public ServiceDescriptionTo[] newArray(int size) {
            return new ServiceDescriptionTo[size];
        }
    };

    public final String name;
    public final int port;
    public final String location;
    public final String category;
    public final String type;
    public final String version;

    public ServiceDescriptionTo(Capone.ServiceQueryResult result) {
        this.name = result.result.name;
        this.port = result.result.port;
        this.location = result.result.location;
        this.category = result.result.category;
        this.type = result.result.type;
        this.version = result.result.version;
    }

    private ServiceDescriptionTo(Parcel in) {
        name = in.readString();
        port = in.readInt();
        location = in.readString();
        category = in.readString();
        type = in.readString();
        version = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(port);
        dest.writeString(location);
        dest.writeString(category);
        dest.writeString(type);
        dest.writeString(version);
    }

}
