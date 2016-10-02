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

import com.github.capone.protocol.crypto.VerifyKey;
import nano.Capone;

public class Session {

    public final int identifier;
    public final Capability capability;

    private Session(int identifier, Capability capability) {
        this.identifier = identifier;
        this.capability = capability;
    }

    public static Session fromMessage(Capone.SessionRequestResult msg)
            throws VerifyKey.InvalidKeyException {
        return new Session(msg.result.identifier, Capability.fromMessage(msg.result.cap));
    }

    public long getUnsignedSessionId() {
        return identifier & 0xffffffffL;
    }

}
