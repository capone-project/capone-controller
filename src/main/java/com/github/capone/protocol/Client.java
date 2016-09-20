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
import com.github.capone.entities.ServiceTo;
import com.github.capone.entities.SessionTo;
import nano.Capone;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;

public class Client {

    public interface SessionHandler {
        void onSessionStarted(ServiceTo service, SessionTo session, Channel channel);

        void onError();
    }

    private final SigningKey localKeys;
    private final ServerTo server;
    private Channel channel;

    public Client(SigningKey localKeys, ServerTo server) {
        this.localKeys = localKeys;
        this.server = server;
    }

    private void initiateConnection(int port, int connectionType) throws
            IOException, VerifyKey.SignatureException {
        Capone.ConnectionInitiationMessage connectionInitiation =
                new Capone.ConnectionInitiationMessage();
        connectionInitiation.type = connectionType;

        channel = new TcpChannel(server.address, port);
        channel.connect();
        channel.enableEncryption(localKeys, server.signatureKey.key);
        channel.writeProtobuf(connectionInitiation);
    }

    public void connect(ServiceTo service, SessionTo session, SessionHandler handler) {
        Capone.SessionConnectMessage connect = new Capone.SessionConnectMessage();
        connect.capability = session.capability.toMessage();
        connect.identifier = session.identifier;
        Capone.SessionConnectResult result = new Capone.SessionConnectResult();

        try {
            initiateConnection(service.port, Capone.ConnectionInitiationMessage.CONNECT);

            channel.writeProtobuf(connect);
            channel.readProtobuf(result);

            if (result.error != null) {
                handler.onError();
            }

            handler.onSessionStarted(service, session, channel);
        } catch (IOException | VerifyKey.SignatureException e) {
            handler.onError();
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
