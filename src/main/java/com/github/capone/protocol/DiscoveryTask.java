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
import com.github.capone.entities.ServerTo;
import nano.Discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class DiscoveryTask extends AsyncTask<Void, ServerTo, Throwable> {

    public static final int LOCAL_DISCOVERY_PORT = 6668;
    public static final int REMOTE_DISCOVERY_PORT = 6667;
    public static final String BROADCAST_ADDRESS = "224.0.0.1";

    private DatagramSocket broadcastSocket;
    private DatagramSocket announceSocket;

    @Override
    protected Throwable doInBackground(Void... ignored) {
        Discovery.DiscoverMessage discoverMessage = new Discovery.DiscoverMessage();
        discoverMessage.version = "0.0.1";
        discoverMessage.port = LOCAL_DISCOVERY_PORT;

        try {
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);

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

                        ServerTo server = ServerTo.fromAnnounce(
                                announcePacket.getAddress().getCanonicalHostName(),
                                announceMessage);

                        publishProgress(server);
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            return e;
        } finally {
            if (broadcastSocket != null)
                broadcastSocket.close();
            if (announceSocket != null)
                announceSocket.close();
        }
    }

    public void cancel() {
        if (broadcastSocket != null)
            broadcastSocket.close();
        if (announceSocket != null)
            announceSocket.close();
        super.cancel(true);
    }

}
