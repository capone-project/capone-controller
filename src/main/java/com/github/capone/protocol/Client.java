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

import com.github.capone.entities.ServerTo;
import com.github.capone.entities.ServiceDescriptionTo;
import com.github.capone.entities.ServiceTo;
import com.github.capone.entities.SessionTo;
import com.google.protobuf.nano.MessageNano;
import nano.Capone;
import nano.Discovery;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class Client {

    public interface SessionHandler {
        void onSessionStarted(ServiceDescriptionTo service, SessionTo session, Channel channel);
    }

    public static final int PROTOCOL_VERSION = 1;

    private final SigningKey localKeys;
    private final String serverAddress;
    private final VerifyKey serverKey;
    private Channel channel;

    public Client(SigningKey localKeys, ServerTo server) {
        this.localKeys = localKeys;
        this.serverAddress = server.address;
        this.serverKey = server.signatureKey.key;
    }

    public Client(SigningKey localKeys, String serverAddress, VerifyKey serverKey) {
        this.localKeys = localKeys;
        this.serverAddress = serverAddress;
        this.serverKey = serverKey;
    }

    private void initiateConnection(int port, int connectionType) throws
            IOException, VerifyKey.SignatureException {
        Capone.ConnectionInitiationMessage connectionInitiation =
                new Capone.ConnectionInitiationMessage();
        connectionInitiation.type = connectionType;

        channel = new TcpChannel(serverAddress, port);
        channel.connect();
        channel.enableEncryption(localKeys, serverKey);
        channel.writeProtobuf(connectionInitiation);
    }

    public ServiceDescriptionTo query(ServiceTo service)
            throws IOException, ProtocolException {
        Discovery.DiscoverMessage discovery = new Discovery.DiscoverMessage();
        discovery.version = PROTOCOL_VERSION;
        Capone.ServiceQueryResult results = new Capone.ServiceQueryResult();

        try {
            initiateConnection(service.port, Capone.ConnectionInitiationMessage.QUERY);

            channel.writeProtobuf(discovery);
            channel.readProtobuf(results);

            return new ServiceDescriptionTo(results);
        } catch (VerifyKey.SignatureException e) {
            throw new ProtocolException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public SessionTo request(ServiceDescriptionTo service, MessageNano parameters)
            throws IOException, ProtocolException {
        return request(service, MessageNano.toByteArray(parameters));
    }

    public SessionTo request(ServiceDescriptionTo service, byte[] parameters)
            throws IOException, ProtocolException {
        return request(service.port, parameters);
    }

    public SessionTo request(int port, byte[] parameters)
            throws IOException, ProtocolException {
        Capone.SessionRequestMessage request = new Capone.SessionRequestMessage();
        request.version = PROTOCOL_VERSION;
        request.parameters = parameters;
        Capone.SessionRequestResult sessionMessage = new Capone.SessionRequestResult();

        try {
            initiateConnection(port, Capone.ConnectionInitiationMessage.REQUEST);

            channel.writeProtobuf(request);
            channel.readProtobuf(sessionMessage);

            if (sessionMessage.error != null) {
                throw new ProtocolException("Received error");
            }

            return new SessionTo(sessionMessage);
        } catch (VerifyKey.SignatureException e) {
            throw new ProtocolException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void connect(ServiceDescriptionTo service, SessionTo session, SessionHandler handler)
            throws IOException, ProtocolException {
        Capone.SessionConnectMessage connect = new Capone.SessionConnectMessage();
        connect.version = PROTOCOL_VERSION;
        connect.capability = session.capability.toMessage();
        connect.identifier = session.identifier;
        Capone.SessionConnectResult result = new Capone.SessionConnectResult();

        try {
            initiateConnection(service.port, Capone.ConnectionInitiationMessage.CONNECT);

            channel.writeProtobuf(connect);
            channel.readProtobuf(result);

            if (result.error != null) {
                throw new ProtocolException("Error connecting to session");
            }

            handler.onSessionStarted(service, session, channel);
        } catch (VerifyKey.SignatureException e) {
            throw new ProtocolException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                /* ignore */
            } finally {
                channel = null;
            }
        }
    }

}
