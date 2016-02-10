package com.icoderman.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServerHandlerThread implements Runnable {
    private ServerSocket peerServerSocket;
    private String sharedDirectory;

    public PeerServerHandlerThread(ServerSocket peerServerSocket, String sharedDirectory) {
        this.peerServerSocket = peerServerSocket;
        this.sharedDirectory = sharedDirectory;
    }

    @Override
    public void run() {
        while(true){
            try {
                if (peerServerSocket.isClosed()) {
                    return;
                }
                Socket incomingPeerSocket = peerServerSocket.accept();
                IncomingPeerHandlerThread incomingPeerHandlerThread = new IncomingPeerHandlerThread(incomingPeerSocket, sharedDirectory);
                // create a thread for each peer connection
                Thread t = new Thread(incomingPeerHandlerThread);
                t.start();
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
