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

import nano.Capabilities;

import java.util.Date;

public class CapabilityRequestTo {

    public final IdentityTo requesterIdentity;
    public final IdentityTo serviceIdentity;

    public final byte[] parameters;

    public final String serviceAddress;
    public final int servicePort;
    public final String serviceType;
    public final Date received;

    public final int requestId;

    public CapabilityRequestTo(Capabilities.CapabilitiesRequest request) {
        this(request, null);
    }

    public CapabilityRequestTo(Capabilities.CapabilitiesRequest request, Date received) {
        requesterIdentity = new IdentityTo(request.requesterIdentity);
        serviceIdentity = new IdentityTo(request.serviceIdentity);
        parameters = request.parameters;
        serviceAddress = request.serviceAddress;
        servicePort = request.servicePort;
        serviceType = request.serviceType;

        this.received = received;
        this.requestId = request.requestid;
    }

}

