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

public class ServiceTo implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public ServiceTo createFromParcel(Parcel in) {
            return new ServiceTo(in);
        }

        @Override
        public ServiceTo[] newArray(int size) {
            return new ServiceTo[size];
        }
    };

    public final String name;
    public final String category;
    public final int port;

    public ServiceTo() {
        name = null;
        category = null;
        port = 0;
    }

    public ServiceTo(Discovery.DiscoverResult.Service service) {
        this.name = service.name;
        this.category = service.category;
        this.port = service.port;
    }

    private ServiceTo(Parcel in) {
        name = in.readString();
        category = in.readString();
        port = in.readInt();
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
        dest.writeString(category);
        dest.writeInt(port);
    }
}
