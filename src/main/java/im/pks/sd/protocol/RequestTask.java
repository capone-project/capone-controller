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
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class RequestTask extends AsyncTask<RequestTask.Parameters, Void, RequestTask.Session> {

    public static class Parameters {
        public final SigningKey localKey;
        public final QueryResults service;
        public final List<QueryResults.Parameter> parameters;

        public Parameters(SigningKey localKey, QueryResults service, List<QueryResults.Parameter> parameters) {
            this.localKey = localKey;
            this.service = service;
            this.parameters = parameters;
        }
    }

    public static class Session {
        public final int sessionId;
        public final byte[] key;

        public Session(int sessionId, byte[] key) {
            this.sessionId = sessionId;
            this.key = key;
        }
    }

    private Channel channel;

    @Override
    protected Session doInBackground(Parameters... params) {
        Parameters requestParameters = params[0];

        List<Connect.Parameter> parameters = new ArrayList<>();
        for (QueryResults.Parameter parameter : requestParameters.parameters) {
            Connect.Parameter serviceParam = new Connect.Parameter();
            serviceParam.key = parameter.name;
            serviceParam.values = parameter.values.toArray(new String[0]);
            parameters.add(serviceParam);
        }

        try {
            VerifyKey remoteKey = new VerifyKey(requestParameters.service.server.publicKey, Encoder.HEX);

            channel = new TcpChannel(requestParameters.service.server.address, requestParameters.service.service.port);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(), remoteKey);

            Connect.ConnectionInitiationMessage initiation = new Connect
                    .ConnectionInitiationMessage();
            initiation.type = Connect.ConnectionInitiationMessage.REQUEST;
            channel.writeProtobuf(initiation);

            Connect.SessionRequestMessage requestMessage = new Connect.SessionRequestMessage();
            requestMessage.parameters = parameters.toArray(new Connect.Parameter[parameters.size()]);

            channel.writeProtobuf(requestMessage);

            Connect.SessionMessage sessionMessage = new Connect.SessionMessage();
            channel.readProtobuf(sessionMessage);

            return new Session(sessionMessage.sessionid, sessionMessage.sessionkey);
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

    @Override
    public abstract void onPostExecute(RequestTask.Session details);

}
