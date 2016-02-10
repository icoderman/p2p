package com.icoderman.p2p.dao;

import com.icoderman.p2p.domain.Peer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps all available files and peers
 */
public interface TrackerRepository {
    Set<Peer> getPeers();

    Map<String, Set<Peer>> getFiles();

    void addPeer(Peer peer);

    void addFiles(List<String> fileNames, Peer peer);

    Set<Peer> searchFile(String fileName);

    void removePeer(Peer peer);
}
