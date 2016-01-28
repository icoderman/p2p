package com.icoderman.p2p;

import com.icoderman.p2p.service.IndexService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Tracker {

    public static void main(String[] args) {
        int trackerPort;
        ServerSocket trackerServerSocket;
        Socket peerSocket;

        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter port for Tracker :");
        trackerPort = Integer.parseInt(scan.nextLine());

        try {
            InetAddress trackerIp = InetAddress.getLocalHost();
            trackerServerSocket = new ServerSocket(trackerPort);
            System.out.println("Tracker is active on the following address:" + trackerIp.getHostAddress() + ":" + trackerServerSocket.getLocalPort());

            IndexService indexService = new IndexService();
            System.out.println("Peers:");

            boolean listening = true;
            while (listening) {
                // TODO: 1. tracker should accept connection with list of shared files from the peer
                // TODO: 2. tracker should check requested file and return list of peers that contains this file
                peerSocket = trackerServerSocket.accept();
                System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] connected...");
                (new Thread(new PeerHandlerThread(peerSocket, indexService))).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        scan.close();
    }
}
