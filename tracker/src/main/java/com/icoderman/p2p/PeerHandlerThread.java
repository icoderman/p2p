package com.icoderman.p2p;

import com.icoderman.p2p.domain.PeerInfo;
import com.icoderman.p2p.service.IndexService;
import com.icoderman.p2p.service.PeerRequestService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PeerHandlerThread implements Runnable {
    private Socket peerSocket;
    private IndexService indexService;

    public PeerHandlerThread(Socket peerSocket, IndexService indexService) {
        this.peerSocket = peerSocket;
        this.indexService = indexService;
    }

    @Override
    public void run() {
        try {
            DataInputStream peerDataInputStream = new DataInputStream(peerSocket.getInputStream());
            //addPeer() will add an entry of peer in Central Index
            int peerId = indexService.addPeer(new PeerInfo(peerSocket.getInetAddress().getHostAddress(), peerSocket.getPort()));
            DataOutputStream trackerDataOutputStream;
            if(peerId != 0){
                trackerDataOutputStream = new DataOutputStream(peerSocket.getOutputStream());
                // Send a byte to client indicating connection and central update was successful
                trackerDataOutputStream.writeByte(1);
            } else {
                trackerDataOutputStream = new DataOutputStream(peerSocket.getOutputStream());
                trackerDataOutputStream.writeByte(0);
            }
            PeerRequestService peerRequestService = new PeerRequestService(peerSocket, indexService, peerId);
            peerRequestService.processRequests(trackerDataOutputStream, peerDataInputStream);
        } catch (IOException e) {
            System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] disconnected !");
        }
    }
}
