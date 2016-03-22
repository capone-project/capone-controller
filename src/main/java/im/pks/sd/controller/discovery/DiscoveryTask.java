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

package im.pks.sd.controller.discovery;

import android.os.AsyncTask;
import im.pks.sd.protocol.UdpChannel;
import nano.Discovery;
import org.abstractj.kalium.keys.PublicKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class DiscoveryTask extends AsyncTask<Void, Server, Void> {

    public static final int LOCAL_DISCOVERY_PORT = 6668;
    public static final int REMOTE_DISCOVERY_PORT = 6667;
    public static final String BROADCAST_ADDRESS = "224.0.0.1";

    private Set<Server> servers = new HashSet<>();
    private VerifyKey key;

    public DiscoveryTask(VerifyKey key) {
        this.key = key;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
        discoverMessage.version = "0.0.1";
        discoverMessage.port = LOCAL_DISCOVERY_PORT;
        discoverMessage.signKey = key.toBytes();

        DatagramSocket broadcastSocket = null, announceSocket = null;
        try {
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            UdpChannel broadcastChannel = UdpChannel.createFromSocket(broadcastSocket, broadcastAddress, REMOTE_DISCOVERY_PORT);

            announceSocket = new DatagramSocket(LOCAL_DISCOVERY_PORT);
            announceSocket.setSoTimeout(10000);
            UdpChannel announceChannel = UdpChannel.createFromSocket(announceSocket, null, 0);

            while (true) {
                broadcastChannel.writeProtobuf(discoverMessage);

                while (true) {
                    try {
                        Discovery.AnnounceMessage announceMessage = new Discovery.AnnounceMessage();
                        announceChannel.readProtobuf(announceMessage);

                        /* TODO: announce to activities */
                        Server server = convertAnnouncement(null, announceMessage);

                        if (!servers.contains(server)) {
                            servers.add(server);
                            publishProgress(server);
                        }
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (broadcastSocket != null)
                broadcastSocket.close();
            if (announceSocket != null)
                announceSocket.close();
        }
        return null;
    }

    private Server convertAnnouncement(InetAddress address, Discovery.AnnounceMessage announceMessage) {
        Server server = new Server();
        server.publicKey = new PublicKey(announceMessage.signKey).toString();
        server.address = null; //address.getCanonicalHostName();
        server.services = new ArrayList<>();

        for (Discovery.AnnounceMessage.Service announcedService : announceMessage.services) {
            Service service = new Service();
            service.name = announcedService.name;
            service.type = announcedService.type;
            service.port = announcedService.port;
            server.services.add(service);
        }
        return server;
    }

    public abstract void onProgressUpdate(Server... server);

}
