/*
2 hours ago
submitgit commented on pull request git/git#219
@submitgit

    @pranitbauva1997 sent this commit (7e4ba36...a2fa85d) as a patch to the mailing list with submitGit - here on Gmane, MARC

2 hours ago
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

import java.util.ArrayList;

public class ServiceDescriptionTo implements Parcelable {

    public static class Parameter implements Parcelable {
        public static final Creator<Parameter> CREATOR = new Creator<Parameter>() {
            @Override
            public Parameter createFromParcel(Parcel in) {
                return new Parameter(in);
            }

            @Override
            public Parameter[] newArray(int size) {
                return new Parameter[size];
            }
        };

        public String name;
        public String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private Parameter(Parcel in) {
            name = in.readString();
            value = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(value);
        }
    }

    public static final Creator<ServiceDescriptionTo> CREATOR = new Creator<ServiceDescriptionTo>() {
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
    public final ArrayList<Parameter> parameters;

    public ServiceDescriptionTo(ServerTo server, ServiceTo service, String type, String location,
                                String version, ArrayList<Parameter> parameters) {
        this.server = server;
        this.service = service;
        this.type = type;
        this.location = location;
        this.version = version;
        this.parameters = parameters;
    }

    private ServiceDescriptionTo(Parcel in) {
        server = in.readParcelable(ServerTo.class.getClassLoader());
        service = in.readParcelable(ServiceTo.class.getClassLoader());
        type = in.readString();
        location = in.readString();
        version = in.readString();
        parameters = in.createTypedArrayList(Parameter.CREATOR);
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
        dest.writeTypedList(parameters);
    }

}
