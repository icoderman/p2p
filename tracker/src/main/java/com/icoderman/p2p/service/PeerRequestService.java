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
     * Handles connected peer's requests
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
                    case "update":
                        processUpdate(peerInputStream);
                        System.out.println(trackerRepository.getFiles());
                        break;
                    case "search":
                        processSearch(trackerOutputStream, peerInputStream);
                        break;
                    case "close":
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
     *
     * @param peerInputStream
     */
    private void processUpdate(DataInputStream peerInputStream) throws IOException {
        List<String> files = new ArrayList<>();
        int filesCount = peerInputStream.readInt();
        for (int i = 0; i < filesCount; i++) {
            files.add(peerInputStream.readUTF());
        }
        trackerRepository.addFiles(files, peer);
        System.out.println(peer + " added " + filesCount + " shared files to TrackerRepository: " + files);
    }

    /**
     * Search requested filename in the trackerRepository and returns size and list of peers
     *
     * @param trackerOut
     * @param peerIn
     * @throws IOException
     */
    private void processSearch(DataOutputStream trackerOut, DataInputStream peerIn) throws IOException {
        String fileName = peerIn.readUTF();
        System.out.println(peer + " looking for file: " + fileName);
        Set<Peer> availablePeers = trackerRepository.searchFile(fileName);
        if (availablePeers.contains(peer)) {
            availablePeers.remove(peer);
        }
        int size = availablePeers.size();
        System.out.println(size + " peers have requested file: " + fileName);
        if (size > 0) {
            trackerOut.writeInt(size);
            for (Peer availablePeer : availablePeers) {
                trackerOut.writeUTF(availablePeer.getHostName() + ":" + availablePeer.getPort());
            }
        }
        System.out.println("Requested file not found...");
        trackerOut.writeInt(0);
    }

    /**
     * Removes peer from tracker repository and closes socket
     *
     * @throws IOException
     */
    private void processClose() throws IOException {
        trackerRepository.removePeer(peer);
        peerSocket.close();
        System.out.println(peer + " disconnected!");
    }


}

