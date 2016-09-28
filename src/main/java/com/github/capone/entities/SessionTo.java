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

import nano.Capone;

public class SessionTo {

    public int identifier;
    public CapabilityTo capability;

    public SessionTo(Capone.SessionRequestResult msg) {
        identifier = msg.result.identifier;
        capability = new CapabilityTo(msg.result.cap);
    }

    public long getUnsignedSessionId() {
        return identifier & 0xffffffffL;
    }

}