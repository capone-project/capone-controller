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
import nano.Connect;
import org.abstractj.kalium.keys.VerifyKey;

import java.util.ArrayList;
import java.util.Date;

public class CapabilityRequestTo {

    public final VerifyKey invokerIdentity;
    public final VerifyKey requesterIdentity;
    public final VerifyKey serviceIdentity;

    public final ArrayList<ParameterTo> parameters;

    public final String serviceAddress;
    public final String servicePort;
    public final Date received;

    public final int requestId;

    public CapabilityRequestTo(Capabilities.CapabilityRequest request) {
        this(request, null);
    }

    public CapabilityRequestTo(Capabilities.CapabilityRequest request, Date received) {
        invokerIdentity = new VerifyKey(request.invokerIdentity);
        requesterIdentity = new VerifyKey(request.requesterIdentity);
        serviceIdentity = new VerifyKey(request.serviceIdentity);

        parameters = new ArrayList<>();
        for (Connect.Parameter parameter : request.parameters) {
            parameters.add(new ParameterTo(parameter.key, parameter.value));
        }

        serviceAddress = request.serviceAddress;
        servicePort = request.servicePort;

        this.received = received;
        this.requestId = request.requestid;
    }

}
