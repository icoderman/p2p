package com.icoderman.p2p;

import com.icoderman.p2p.dao.InMemoryTrackerRepository;
import com.icoderman.p2p.dao.TrackerRepository;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Main P2P TrackerApp that responsible for:
 *  1. tracker should accept connection with list of shared files (file names and hashes) from the peer
 *  2. tracker should check requested file in the index and return list of peers that contains this file
 */
public class TrackerApp {

    public static void main(String[] args) {
        int trackerPort;
        ServerSocket trackerServerSocket;
        Socket peerSocket;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter port for Tracker: ");
        trackerPort = Integer.parseInt(scanner.nextLine());

        try {
            InetAddress trackerIp = InetAddress.getLocalHost();
            trackerServerSocket = new ServerSocket(trackerPort);
            System.out.println("Tracker is active on the following address: " + trackerIp.getHostAddress() + ":" + trackerServerSocket.getLocalPort());

            TrackerRepository trackerRepository = new InMemoryTrackerRepository();
            System.out.println("Peers:");

            boolean listening = true;
            while (listening) {
                peerSocket = trackerServerSocket.accept();
                System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] connected...");
                // todo: replace with pool
                Thread peerHandlerThread = new Thread(new PeerHandlerThread(peerSocket, trackerRepository));
                peerHandlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        scanner.close();
    }
}
