package com.icoderman.p2p.domain;

public class Peer {
    private String hostName;
    private int port;

    public Peer() {
    }

    public Peer(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        if (port != peer.port) return false;
        return !(hostName != null ? !hostName.equals(peer.hostName) : peer.hostName != null);

    }

    @Override
    public int hashCode() {
        int result = hostName != null ? hostName.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
