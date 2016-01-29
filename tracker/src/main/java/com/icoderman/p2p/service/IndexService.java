package com.icoderman.p2p.service;

import com.icoderman.p2p.domain.PeerInfo;

import java.util.*;

public class IndexService {

    private List<PeerInfo> peers;
    private Map<String, Set<Integer>> files;

    public IndexService() {
        peers = new ArrayList<>();
        files = new HashMap<>();
    }

    public int addPeer(PeerInfo peerInfo) {
        if (peers.contains(peerInfo)) {
            return 0;
        }
        peers.add(peerInfo);
        return peerInfo.hashCode();
    }


    public void addFiles(List<String> fileNames, int peerId) {
        for (String fileName : fileNames) {
            Set<Integer> peers;
            if (files.containsKey(fileName)) {
                peers = files.get(fileName);
                peers.add(peerId);
            } else {
                peers = new HashSet<>();
                peers.add(peerId);
                files.put(fileName, peers);
            }
        }
    }

    public Set<Integer> searchFiles(String fileName) {
        return files.get(fileName);
    }

    public void removePeer(int peerId) {
        for (String fileName: files.keySet()) {
            Set<Integer> peers = files.get(fileName);
            peers.remove(peerId);
        }
    }

    public PeerInfo searchPeers(Integer pid) {
        return null;
    }
}
