package com.icoderman.p2p.dao;

import com.icoderman.p2p.domain.Peer;

import java.util.*;

public class TrackerRepository {
    private Set<Peer> peers;
    private Map<String, Set<Peer>> files;

    public Set<Peer> getPeers() {
        return peers;
    }

    public Map<String, Set<Peer>> getFiles() {
        return files;
    }

    public TrackerRepository() {
        peers = new HashSet<>();
        files = new HashMap<>();
    }

    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    public void addFiles(List<String> fileNames, Peer peer) {
        for (String fileName : fileNames) {
            if (files.containsKey(fileName)) {
                files.get(fileName).add(peer);
            } else {
                Set<Peer> newPeers = new HashSet<>();
                newPeers.add(peer);
                files.put(fileName, newPeers);
            }
        }
    }

    public Set<Peer> searchFile(String fileName) {
        if (files.containsKey(fileName)) {
            return files.get(fileName);
        }
        return Collections.emptySet();
    }

    public void removePeer(Peer peer) {
        peers.remove(peer);
        removePeerFromFiles(peer);
    }

    private void removePeerFromFiles(Peer peer) {
        for (String fileName : files.keySet()) {
            if (files.get(fileName).contains(peer)) {
                files.get(fileName).remove(peer);
            }
        }
    }

}
