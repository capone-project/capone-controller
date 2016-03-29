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
import com.google.protobuf.nano.MessageNano;
import im.pks.sd.entities.ServerTo;
import im.pks.sd.entities.ServiceTo;
import im.pks.sd.protocol.UdpChannel;
import nano.Discovery;
import org.abstractj.kalium.keys.PublicKey;
import org.abstractj.kalium.keys.VerifyKey;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DiscoveryTask extends AsyncTask<Void, ServerTo, Void> {

    public static final int LOCAL_DISCOVERY_PORT = 6668;
    public static final int REMOTE_DISCOVERY_PORT = 6667;
    public static final String BROADCAST_ADDRESS = "224.0.0.1";

    private Set<ServerTo> servers = new HashSet<>();

    private VerifyKey key;
    private DatagramSocket broadcastSocket;
    private DatagramSocket announceSocket;

    public DiscoveryTask(List<ServerTo> servers, VerifyKey key) {
        if (servers != null) {
            this.servers.addAll(servers);
        }
        this.key = key;
    }

    @Override
    protected Void doInBackground(Void... ignored) {
        Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
        discoverMessage.version = "0.0.1";
        discoverMessage.port = LOCAL_DISCOVERY_PORT;
        discoverMessage.signKey = key.toBytes();

        try {
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            UdpChannel broadcastChannel = UdpChannel.createFromSocket(broadcastSocket, broadcastAddress, REMOTE_DISCOVERY_PORT);

            announceSocket = new DatagramSocket(LOCAL_DISCOVERY_PORT);
            announceSocket.setSoTimeout(10000);

            while (true) {
                broadcastChannel.writeProtobuf(discoverMessage);

                while (true) {
                    if (this.isCancelled()) {
                        return null;
                    }

                    try {
                        ByteBuffer lenBuf = ByteBuffer.allocate(4);
                        DatagramPacket lenPacket = new DatagramPacket(lenBuf.array(), lenBuf.array().length);
                        announceSocket.receive(lenPacket);
                        int len = lenBuf.order(ByteOrder.BIG_ENDIAN).getInt();
                        if (len < 0) {
                            throw new InvalidParameterException();
                        }

                        DatagramPacket announcePacket = new DatagramPacket(new byte[len], len);
                        announceSocket.receive(announcePacket);

                        if (!lenPacket.getAddress().equals(announcePacket.getAddress())) {
                            continue;
                        }

                        Discovery.AnnounceMessage announceMessage = new Discovery.AnnounceMessage();
                        MessageNano.mergeFrom(announceMessage, announcePacket.getData());

                        /* TODO: announce to activities */
                        ServerTo server = convertAnnouncement(announcePacket.getAddress(), announceMessage);

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
