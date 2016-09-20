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
import nano.Core;
import org.abstractj.kalium.keys.VerifyKey;

import static org.abstractj.kalium.SodiumConstants.PUBLICKEY_BYTES;

public class SignatureKeyTo implements Parcelable {

    public static final Creator CREATOR = new Parcelable.Creator() {
        @Override
        public SignatureKeyTo createFromParcel(Parcel in) {
            return new SignatureKeyTo(in);
        }

        @Override
        public SignatureKeyTo[] newArray(int size) {
            return new SignatureKeyTo[size];
        }
    };

    public final VerifyKey key;

    public SignatureKeyTo(Core.SignatureKeyMessage key) {
        this.key = new VerifyKey(key.data);
    }

    protected SignatureKeyTo(Parcel in) {
        byte[] bytes = new byte[PUBLICKEY_BYTES];

        in.readByteArray(bytes);
        this.key = new VerifyKey(bytes);
    }

    public Core.SignatureKeyMessage toMessage() {
        Core.SignatureKeyMessage msg = new Core.SignatureKeyMessage();
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
