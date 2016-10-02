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
import com.github.capone.protocol.crypto.VerifyKey;
import nano.Core;

public class Identity implements Parcelable {

    public static final Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Identity createFromParcel(Parcel in) {
            return new Identity(in);
        }

        @Override
        public Identity[] newArray(int size) {
            return new Identity[size];
        }
    };

    public final VerifyKey key;

    protected Identity(VerifyKey key) {
        this.key = key;
    }

    protected Identity(Parcel in) {
        byte[] bytes = new byte[VerifyKey.BYTES];

        in.readByteArray(bytes);
        try {
            this.key = VerifyKey.fromBytes(bytes);
        } catch (VerifyKey.InvalidKeyException e) {
            throw new RuntimeException();
        }
    }

    public static Identity fromMessage(Core.IdentityMessage message)
            throws VerifyKey.InvalidKeyException {
        return new Identity(VerifyKey.fromBytes(message.data));
    }

    public Core.IdentityMessage toMessage() {
        Core.IdentityMessage msg = new Core.IdentityMessage();
        msg.data = key.toBytes();
        return msg;
    }

    public String toString() {
        return key.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(key.toBytes());
    }

}
