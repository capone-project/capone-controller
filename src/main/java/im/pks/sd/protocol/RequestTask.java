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

package im.pks.sd.protocol;

import android.os.AsyncTask;
import im.pks.sd.controller.invoke.QueryResults;
import im.pks.sd.persistence.Identity;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RequestTask extends AsyncTask<Void, Void, RequestTask.Session> {

    private final VerifyKey identity;
    private final QueryResults service;
    private final List<QueryResults.Parameter> parameters;

    public static class Session {
        public final int sessionId;

        public Session(int sessionId) {
            this.sessionId = sessionId;
        }
    }

    private Channel channel;

    public RequestTask(VerifyKey identity, QueryResults service,
                       List<QueryResults.Parameter> parameters) {
        this.identity = identity;
        this.service = service;
        this.parameters = parameters;
    }

    @Override
    protected Session doInBackground(Void... params) {
        return requestSession();
    }

    public Session requestSession() {
        List<Connect.Parameter> connectParams = new ArrayList<>();
        for (QueryResults.Parameter parameter : parameters) {
            Connect.Parameter serviceParam = new Connect.Parameter();
            serviceParam.key = parameter.name;
            serviceParam.values = parameter.values.toArray(new String[0]);
            connectParams.add(serviceParam);
        }

        Connect.ConnectionInitiationMessage initiation = new Connect.ConnectionInitiationMessage();
        initiation.type = Connect.ConnectionInitiationMessage.REQUEST;

        Connect.SessionRequestMessage requestMessage = new Connect.SessionRequestMessage();
        requestMessage.parameters = connectParams.toArray(
                new Connect.Parameter[connectParams.size()]);
        requestMessage.identity = identity.toBytes();

        Connect.SessionMessage sessionMessage = new Connect.SessionMessage();

        try {
            VerifyKey remoteKey = new VerifyKey(service.server.publicKey, Encoder.HEX);

            channel = new TcpChannel(service.server.address, service.service.port);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(), remoteKey);

            channel.writeProtobuf(initiation);
            channel.writeProtobuf(requestMessage);

            channel.readProtobuf(sessionMessage);

            return new Session(sessionMessage.sessionid);
        } catch (IOException | VerifyKey.SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cancel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
