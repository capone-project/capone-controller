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

package com.github.capone.protocol;

import android.os.AsyncTask;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.SessionTo;
import com.github.capone.persistence.Identity;
import com.google.protobuf.nano.MessageNano;
import nano.Connect;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class RequestTask extends AsyncTask<Void, Void, RequestTask.Result> {

    public static class Result {
        public final Throwable throwable;
        public final SessionTo session;

        public Result(Throwable throwable) {
            this.session = null;
            this.throwable = throwable;
        }

        public Result(SessionTo session) {
            this.session = session;
            this.throwable = null;
        }
    }

    private final VerifyKey serviceIdentity;
    private final String serviceAddress;
    private final int servicePort;
    private final byte[] parameters;

    private Channel channel;

    public RequestTask(ServiceDescriptionTo service, MessageNano parameters) {
        this.serviceIdentity = service.server.signatureKey.key;
        this.serviceAddress = service.server.address;
        this.servicePort = service.service.port;
        this.parameters = MessageNano.toByteArray(parameters);
    }

    public RequestTask(VerifyKey serviceIdentity, String serviceAddress, int servicePort,
                       byte[] parameters) {
        this.serviceIdentity = serviceIdentity;
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

    public SessionTo requestSession() throws IOException, VerifyKey.SignatureException {
        Connect.ConnectionInitiationMessage initiation = new Connect.ConnectionInitiationMessage();
        initiation.type = Connect.ConnectionInitiationMessage.REQUEST;

        Connect.SessionRequestMessage requestMessage = new Connect.SessionRequestMessage();
        requestMessage.parameters = parameters;

        Connect.SessionRequestResult sessionMessage = new Connect.SessionRequestResult();

        try {
            channel = new TcpChannel(serviceAddress, servicePort);
            channel.connect();
            channel.enableEncryption(Identity.getSigningKey(), serviceIdentity);

            channel.writeProtobuf(initiation);
            channel.writeProtobuf(requestMessage);

            channel.readProtobuf(sessionMessage);

            return new SessionTo(sessionMessage);
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
