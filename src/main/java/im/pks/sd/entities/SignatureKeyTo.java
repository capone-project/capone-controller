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

import nano.Connect;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.VerifyKey;

public class SignatureKeyTo {

    public final VerifyKey key;

    public SignatureKeyTo(Connect.SignatureKey key) {
        this.key = new VerifyKey(key.data);
    }

    public SignatureKeyTo(String data) {
        this.key = new VerifyKey(Hex.HEX.decode(data));
    }

    public Connect.SignatureKey toMessage() {
        Connect.SignatureKey msg = new Connect.SignatureKey();
        msg.data = key.toBytes();
        return msg;
    }

}
