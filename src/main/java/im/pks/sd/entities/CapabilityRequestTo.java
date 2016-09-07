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

import nano.Capabilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CapabilityRequestTo {

    public final SignatureKeyTo invokerIdentity;
    public final SignatureKeyTo requesterIdentity;
    public final SignatureKeyTo serviceIdentity;

    public final List<String> parameters;

    public final String serviceAddress;
    public final String servicePort;
    public final Date received;

    public final int requestId;

    public CapabilityRequestTo(Capabilities.CapabilityRequest request) {
        this(request, null);
    }

    public CapabilityRequestTo(Capabilities.CapabilityRequest request, Date received) {
        invokerIdentity = new SignatureKeyTo(request.invokerIdentity);
        requesterIdentity = new SignatureKeyTo(request.requesterIdentity);
        serviceIdentity = new SignatureKeyTo(request.serviceIdentity);

        parameters = new ArrayList<>();
        for (String parameter : request.parameters) {
            parameters.add(parameter);
        }

        serviceAddress = request.serviceAddress;
        servicePort = request.servicePort;

        this.received = received;
        this.requestId = request.requestid;
    }

}

