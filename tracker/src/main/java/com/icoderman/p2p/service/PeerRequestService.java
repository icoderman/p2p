package com.icoderman.p2p.service;

import com.icoderman.p2p.dao.TrackerRepository;
import com.icoderman.p2p.domain.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PeerRequestService {

    private Peer peer;
    private Socket peerSocket;
    private TrackerRepository trackerRepository;

    public PeerRequestService(Peer peer, Socket peerSocket, TrackerRepository trackerRepository) {
        this.peer = peer;
        this.peerSocket = peerSocket;
        this.trackerRepository = trackerRepository;
    }

    /**
     * Handles peer's requests
     *
     * @param trackerOutputStream
     * @param peerInputStream
     */
    public void processRequests(DataOutputStream trackerOutputStream, DataInputStream peerInputStream) {
        System.out.println("Process Request from Peer:" + peer);
        try {
            // till the peer connection with tracker is bound, it will listen for requests from peer
            while (peerSocket.isBound()) {
                String code = peerInputStream.readUTF();
                switch (code) {
                    case "UPDATE":
                        processUpdate(peerInputStream);
                        System.out.println(trackerRepository.getFiles());
                        break;
                    case "SEARCH":
                        processSearch(trackerOutputStream, peerInputStream);
                        break;
                    case "CLOSE":
                        processClose();
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates TrackerRepository with available files from peer
     * @param peerInputStream
     */
    private void processUpdate(DataInputStream peerInputStream) throws IOException {
        List<String> files = new ArrayList<>();
        int filesCount = peerInputStream.readInt();
        for (int i = 0; i < filesCount; i++) {
            files.add(peerInputStream.readUTF());
        }
        trackerRepository.addFiles(files, peer);
    }

    /**
     * Search requested filename in the trackerRepository and returns size and list of peers
     * @param serverOut
     * @param clientIn
     * @throws IOException
     */
    private void processSearch(DataOutputStream serverOut, DataInputStream clientIn) throws IOException {
        String fileName = clientIn.readUTF();
        System.out.println("Peer ["+ peer +"] looking for file: "+fileName);
        Set<Peer> availablePeers = trackerRepository.searchFile(fileName);
        if (availablePeers.contains(peer)) {
            availablePeers.remove(peer);
        }
        int size = availablePeers.size();
        System.out.println(size + " peers have requested file: "+ fileName);
        if (size > 0) {
            serverOut.writeInt(size);
            for (Peer availablePeer : availablePeers) {
                serverOut.writeUTF(availablePeer.getHostName() + ":" + availablePeer.getPort());
            }
        }
        serverOut.write(0);
    }

    /**
     * Removes peer from repository and closes socket
     * @throws IOException
     */
    private void processClose() throws IOException {
        trackerRepository.removePeer(peer);
        peerSocket.close();
        System.out.println("Peer [ " + peer + " ] disconnected !");
    }



}

