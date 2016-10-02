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

import com.github.capone.protocol.crypto.SigningKey;
import com.github.capone.protocol.crypto.VerifyKey;
import com.github.capone.protocol.entities.Server;
import com.github.capone.protocol.entities.Service;
import com.github.capone.protocol.entities.ServiceDescription;
import com.github.capone.protocol.entities.Session;
import com.google.protobuf.nano.MessageNano;
import nano.Capone;
import nano.Discovery;

import java.io.IOException;

public class Client {

    public interface SessionHandler {
        void onSessionStarted(ServiceDescription service, Session session, Channel channel);
    }

    public static final int PROTOCOL_VERSION = 1;

    private final SigningKey localKeys;
    private final String serverAddress;
    private final VerifyKey serverKey;
    private Channel channel;

    public Client(SigningKey localKeys, Server server) {
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

    public ServiceDescription query(Service service)
            throws IOException, ProtocolException {
        Discovery.DiscoverMessage discovery = new Discovery.DiscoverMessage();
        discovery.version = PROTOCOL_VERSION;
        Capone.ServiceQueryResult results = new Capone.ServiceQueryResult();

        try {
            initiateConnection(service.port, Capone.ConnectionInitiationMessage.QUERY);

            channel.writeProtobuf(discovery);
            channel.readProtobuf(results);

            return new ServiceDescription(results);
        } catch (VerifyKey.SignatureException e) {
            throw new ProtocolException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public Session request(ServiceDescription service, MessageNano parameters)
            throws IOException, ProtocolException {
        return request(service, MessageNano.toByteArray(parameters));
    }

    public Session request(ServiceDescription service, byte[] parameters)
            throws IOException, ProtocolException {
        return request(service.port, parameters);
    }

    public Session request(int port, byte[] parameters)
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

            return Session.fromMessage(sessionMessage);
        } catch (VerifyKey.SignatureException | VerifyKey.InvalidKeyException e) {
            throw new ProtocolException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void connect(ServiceDescription service, Session session, SessionHandler handler)
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

            if (handler != null) {
                handler.onSessionStarted(service, session, channel);
            }
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
