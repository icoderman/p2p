package com.icoderman.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServerHandlerThread implements Runnable {
    private ServerSocket peerServerSocket;

    public PeerServerHandlerThread(ServerSocket peerServerSocket) {
        this.peerServerSocket = peerServerSocket;
    }

    @Override
    public void run() {
        Socket incomingPeerSocket;
        IncomingPeerHandlerThread incomingPeerHandlerThread;
        while(true){
            try {
                incomingPeerSocket = peerServerSocket.accept();
                incomingPeerHandlerThread = new IncomingPeerHandlerThread(incomingPeerSocket);
                // create a thread for each peer connection
                Thread t = new Thread(incomingPeerHandlerThread);
                t.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
