package com.icoderman.p2p;

import com.icoderman.p2p.dao.TrackerRepository;
import com.icoderman.p2p.domain.Peer;
import com.icoderman.p2p.service.PeerRequestService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * PeerHandlerThread adds information about new connected Peer to the TrackerRepository
 * and handles communication with connected Peer.
 */
public class PeerHandlerThread implements Runnable {
    private static final int SUCCESS_CONNECTION = 1;

    private Socket peerSocket;
    private TrackerRepository trackerRepository;

    public PeerHandlerThread(Socket peerSocket, TrackerRepository trackerRepository) {
        this.peerSocket = peerSocket;
        this.trackerRepository = trackerRepository;
    }

    @Override
    public void run() {
        try {
            DataInputStream peerDataInputStream = new DataInputStream(peerSocket.getInputStream());
            //addPeer() will add an entry of peer in Central Index
            Peer newPeer = new Peer(peerSocket.getInetAddress().getHostAddress(), peerSocket.getPort());
            trackerRepository.addPeer(newPeer);
            DataOutputStream trackerDataOutputStream = new DataOutputStream(peerSocket.getOutputStream());
            // Send a byte to client indicating connection and central update was successful
            trackerDataOutputStream.writeByte(SUCCESS_CONNECTION);

            PeerRequestService peerRequestService = new PeerRequestService(newPeer, peerSocket, trackerRepository);
            peerRequestService.processRequests(trackerDataOutputStream, peerDataInputStream);

        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] disconnected !");
        }
    }
}
