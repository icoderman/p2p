package com.icoderman.p2p.dao;

import com.icoderman.p2p.domain.Peer;

import java.util.*;

public class InMemoryTrackerRepository implements TrackerRepository {
    private Set<Peer> peers;
    private Map<String, Set<Peer>> files;

    @Override
    public Set<Peer> getPeers() {
        return peers;
    }

    @Override
    public Map<String, Set<Peer>> getFiles() {
        return files;
    }

    public InMemoryTrackerRepository() {
        peers = new HashSet<>();
        files = new HashMap<>();
    }

    @Override
    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    @Override
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

    @Override
    public Set<Peer> searchFile(String fileName) {
        if (files.containsKey(fileName)) {
            return files.get(fileName);
        }
        return Collections.emptySet();
    }

    @Override
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
