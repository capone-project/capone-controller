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

import android.os.Parcel;
import android.os.Parcelable;

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

    public final ServerTo server;
    public final ServiceTo service;
    public final String type;
    public final String location;
    public final String version;

    public ServiceDescriptionTo(ServerTo server, ServiceTo service, String type, String location,
                                String version) {
        this.server = server;
        this.service = service;
        this.type = type;
        this.location = location;
        this.version = version;
    }

    private ServiceDescriptionTo(Parcel in) {
        server = in.readParcelable(ServerTo.class.getClassLoader());
        service = in.readParcelable(ServiceTo.class.getClassLoader());
        type = in.readString();
        location = in.readString();
        version = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(server, flags);
        dest.writeParcelable(service, flags);
        dest.writeString(type);
        dest.writeString(location);
        dest.writeString(version);
    }

}
