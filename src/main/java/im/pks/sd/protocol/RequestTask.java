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
import im.pks.sd.entities.ParameterTo;
import im.pks.sd.entities.ServiceDescriptionTo;
import im.pks.sd.persistence.Identity;
import nano.Connect;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RequestTask extends AsyncTask<Void, Void, RequestTask.Result> {

    public static class Session {
        public final int sessionId;

        public Session(int sessionId) {
            this.sessionId = sessionId;
        }

        /* Ints are saved with the sign bit representing the most significant bit as Java has no
         * notion of unsigned ints. This converts the session ID to the unsigned long
         * representation.
         */
        public long getUnsignedSessionId() {
            return sessionId & 0xffffffffL;
        }
    }

    public static class Result {
        public final Throwable throwable;
        public final Session session;

        public Result(Throwable throwable) {
            this.session = null;
            this.throwable = throwable;
        }

        public Result(Session session) {
            this.session = session;
            this.throwable = null;
        }
    }

    private final VerifyKey invoker;
    private final String serviceIdentity;
    private final String serviceAddress;
    private final int servicePort;
    private final List<ParameterTo> parameters;

    private Channel channel;

    public RequestTask(VerifyKey invoker, ServiceDescriptionTo service,
                       List<ParameterTo> parameters) {
        this.invoker = invoker;
        this.serviceIdentity = service.server.publicKey;
        this.serviceAddress = service.server.address;
        this.servicePort = service.service.port;
        this.parameters = parameters;
    }

    public RequestTask(VerifyKey invoker, VerifyKey serviceIdentity,
                       String serviceAddress, int servicePort,
                       List<ParameterTo> parameters) {
        this.invoker = invoker;
        this.serviceIdentity = serviceIdentity.toString();
        this.serviceAddress = serviceAddress;
        this.servicePort = servicePort;
        this.parameters = parameters;
    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            return new Result(requestSession());
        } catch (IOException | VerifyKey.SignatureException e) {
            return new Result(e);
        }
    }

    public Session requestSession() throws IOException, VerifyKey.SignatureException {
        List<Connect.Parameter> connectParams = new ArrayList<>();
        if (parameters != null) {
            for (ParameterTo parameter : parameters) {
                Connect.Parameter serviceParam = new Connect.Parameter();
                serviceParam.key = parameter.name;
                serviceParam.value = parameter.value;
                connectParams.add(serviceParam);
            }
        }

        Connect.ConnectionInitiationMessage initiation = new Connect.ConnectionInitiationMessage();
        initiation.type = Connect.ConnectionInitiationMessage.REQUEST;

        Connect.SessionRequestMessage requestMessage = new Connect.SessionRequestMessage();
        requestMessage.parameters = connectParams.toArray(
                new Connect.Parameter[connectParams.size()]);
        requestMessage.invoker = invoker.toBytes();

        Connect.SessionMessage sessionMessage = new Connect.SessionMessage();

        try {
            VerifyKey remoteKey = new VerifyKey(serviceIdentity, Encoder.HEX);

            channel = new TcpChannel(serviceAddress, servicePort);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(), remoteKey);

            channel.writeProtobuf(initiation);
            channel.writeProtobuf(requestMessage);

            channel.readProtobuf(sessionMessage);

            return new Session(sessionMessage.sessionid);
        } catch (IOException | VerifyKey.SignatureException e) {
            throw e;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ignore
                } finally {
                    channel = null;
                }
            }
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
