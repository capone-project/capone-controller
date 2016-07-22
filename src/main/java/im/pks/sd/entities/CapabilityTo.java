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

public class CapabilityTo {

    public final int objectId;
    public final int rights;
    public final byte[] secret;

    public CapabilityTo(Connect.CapabilityMessage msg) {
        objectId = msg.objectid;
        rights = msg.rights;
        secret = msg.secret;
    }

    public Connect.CapabilityMessage toMessage() {
        Connect.CapabilityMessage msg = new Connect.CapabilityMessage();
        msg.objectid = objectId;
        msg.rights = rights;
        msg.secret = secret;
        return msg;
    }

}
