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

import java.io.IOException;
import java.net.*;

public class UdpChannel extends Channel {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private DatagramPacket peekPacket;

    private UdpChannel(DatagramSocket socket, InetAddress remoteAddress, int remotePort) {
        super();
        this.socket = socket;
        this.address = remoteAddress;
        this.port = remotePort;
    }

    public static UdpChannel createFromHost(String host, int port) throws UnknownHostException, SocketException {
        InetAddress address = InetAddress.getByName(host);
        DatagramSocket socket = new DatagramSocket(port, address);
        return createFromSocket(socket, address, port);
    }

    public static UdpChannel createFromSocket(DatagramSocket socket, InetAddress remoteAddress, int remotePort) {
        return new UdpChannel(socket, remoteAddress, remotePort);
    }

    @Override
    protected void write(byte[] msg, int len) throws IOException {
        DatagramPacket packet = new DatagramPacket(msg, len, address, port);
        socket.send(packet);
    }

    @Override
    protected int read(byte[] msg, int len) throws IOException {
        if (peekPacket != null) {
            int length = peekPacket.getLength();
            System.arraycopy(peekPacket.getData(), 0, msg, 0,
                             Math.min(length, len));
            peekPacket = null;
            return length;
        }

        DatagramPacket packet = new DatagramPacket(msg, len);
        socket.receive(packet);
        return packet.getLength();
    }

    public DatagramPacket peek(int len) throws IOException {
        if (peekPacket != null) {
            return peekPacket;
        }

        DatagramPacket packet = new DatagramPacket(new byte[len], len);
        socket.receive(packet);
        this.peekPacket = packet;
        return peekPacket;
    }

    @Override
    public void connect() throws IOException {
    }

    public void close() {
        socket.close();
    }

}
