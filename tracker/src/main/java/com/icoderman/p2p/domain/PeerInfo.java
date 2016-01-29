package com.icoderman.p2p.domain;

public class PeerInfo {
    private String hostName;
    private int port;

    public PeerInfo() {
    }

    public PeerInfo(String hostName, int port) {
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

        PeerInfo peerInfo = (PeerInfo) o;

        if (port != peerInfo.port) return false;
        return !(hostName != null ? !hostName.equals(peerInfo.hostName) : peerInfo.hostName != null);

    }

    @Override
    public int hashCode() {
        int result = hostName != null ? hostName.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
