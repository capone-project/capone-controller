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
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.persistence.Server;
import nano.Discovery;
import org.abstractj.kalium.keys.PublicKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DiscoveryTask extends AsyncTask<Void, ServerTo, Void> {

    public static final int LOCAL_DISCOVERY_PORT = 6668;
    public static final int REMOTE_DISCOVERY_PORT = 6667;
    public static final String BROADCAST_ADDRESS = "224.0.0.1";

    private Set<ServerTo> servers = new HashSet<>();

    private final String address;
    private final VerifyKey key;

    private DatagramSocket broadcastSocket;
    private DatagramSocket announceSocket;

    public DiscoveryTask(VerifyKey key) {
        this.key = key;
        this.address = BROADCAST_ADDRESS;
    }

    public DiscoveryTask(List<ServerTo> servers, VerifyKey key) {
        if (servers != null) {
            this.servers.addAll(servers);
        }
        this.key = key;
        this.address = BROADCAST_ADDRESS;
    }

    public DiscoveryTask(Server server, VerifyKey key) {
        this.key = key;
        this.address = server.getAddress();
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
        discoverMessage.version = "0.0.1";
        discoverMessage.port = LOCAL_DISCOVERY_PORT;
        discoverMessage.signKey = key.toBytes();

        try {
            InetAddress broadcastAddress = InetAddress.getByName(this.address);
            broadcastSocket = new DatagramSocket();
            if (this.address.equals(BROADCAST_ADDRESS)) {
                broadcastSocket.setBroadcast(true);
            }

            UdpChannel broadcastChannel = UdpChannel.createFromSocket(broadcastSocket,
                                                                      broadcastAddress,
                                                                      REMOTE_DISCOVERY_PORT);

            announceSocket = new DatagramSocket(LOCAL_DISCOVERY_PORT);
            announceSocket.setSoTimeout(10000);
            UdpChannel announceChannel = UdpChannel.createFromSocket(announceSocket, null, 0);

            while (true) {
                broadcastChannel.writeProtobuf(discoverMessage);

                while (true) {
                    if (this.isCancelled()) {
                        return null;
                    }

                    try {
                        DatagramPacket announcePacket = announceChannel.peek(512);
                        Discovery.AnnounceMessage announceMessage = new Discovery.AnnounceMessage();
                        announceChannel.readProtobuf(announceMessage);

                        ServerTo server = convertAnnouncement(announcePacket.getAddress(),
                                                              announceMessage);

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

    private ServerTo convertAnnouncement(InetAddress address, Discovery.AnnounceMessage announceMessage) {
        ServerTo server = new ServerTo();
        server.publicKey = new PublicKey(announceMessage.signKey).toString();
        server.address = address.getCanonicalHostName();
        server.services = new ArrayList<>();

        for (Discovery.AnnounceMessage.Service announcedService : announceMessage.services) {
            ServiceTo service = new ServiceTo();
            service.name = announcedService.name;
            service.category = announcedService.category;
            service.port = Integer.valueOf(announcedService.port);
            server.services.add(service);
        }
        return server;
    }

    public void cancel() {
        if (broadcastSocket != null)
            broadcastSocket.close();
        if (announceSocket != null)
            announceSocket.close();
        super.cancel(true);
    }

    public abstract void onProgressUpdate(ServerTo... server);

}
