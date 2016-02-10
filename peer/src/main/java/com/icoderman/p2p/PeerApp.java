package com.icoderman.p2p;

import com.icoderman.p2p.service.PeerHandlerService;

import java.util.Scanner;

public class PeerApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String sharedDirectory = args[0];
        System.out.println("Enter shared directory: " + sharedDirectory);
        System.out.print("Enter Tracker [address:port]: ");
        String[] trackerHostPort = scanner.nextLine().split(":");
        PeerHandlerService peerHandlerService = new PeerHandlerService(trackerHostPort[0], Integer.parseInt(trackerHostPort[1]), sharedDirectory);
        peerHandlerService.processCommands();
        scanner.close();
    }
}
