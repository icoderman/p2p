package com.icoderman.p2p.service;


import com.icoderman.p2p.PeerServerHandlerThread;
import com.icoderman.p2p.domain.CommandType;
import com.icoderman.p2p.domain.Peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PeerHandlerService {

    private static final int SUCCESS_CONNECTION = 1;

    private String sharedDirectory;
    private String trackerHost;
    private int trackerPort;

    private ServerSocket peerServerSocket;

    InputStream trackerInputStream;
    DataInputStream trackerInputDataStream;
    DataOutputStream peerOutputStream;


    public PeerHandlerService(String trackerHost, int trackerPort, String sharedDirectory) {
        this.sharedDirectory = sharedDirectory;
        this.trackerHost = trackerHost;
        this.trackerPort = trackerPort;
    }

    private void init() throws IOException {
        Socket trackerSocket = new Socket(trackerHost, trackerPort);
        int peerPort = trackerSocket.getLocalPort() + 1;
        peerServerSocket = new ServerSocket(peerPort);

        trackerInputStream = trackerSocket.getInputStream();
        trackerInputDataStream = new DataInputStream(trackerInputStream);
        peerOutputStream = new DataOutputStream(trackerSocket.getOutputStream());
    }

    public void processCommands() {
        try {
            init();
            if (trackerInputDataStream.readByte() == SUCCESS_CONNECTION) {
                System.out.println("Successfully connected to the Tracker");
                updateTracker();
                processPeerCommands();
            }
        } catch (IOException e) {
            System.out.println("Error: Connection to the Tracker failed!");
            e.printStackTrace();
        }
    }

    private void processPeerCommands() throws IOException {
        Scanner scanner = new Scanner(System.in);
        startPeerServer();
        String fileName;
        while (true) {
            System.out.print("Please enter command (close, search, download): ");
            CommandType command = CommandType.getCommandByValue(scanner.nextLine());
            switch (command) {
                case UPDATE:
                    updateTracker();
                    break;
                case SEARCH:
                    System.out.println("Enter the file name (with extension): ");
                    fileName = scanner.nextLine();
                    searchFile(fileName);
                    break;
                case DOWNLOAD:
                    System.out.println("Enter the file name (with extension): ");
                    fileName = scanner.nextLine();
                    downloadFile(fileName);
                    break;
                case CLOSE:
                    peerOutputStream.writeUTF("close");
                    peerServerSocket.close();
                    System.out.println("Disconnected from Tracker...");
                    return;
                default:
                    System.out.println("Available commands: update, search, download, close");
            }
        }
    }

    private void startPeerServer() {
        PeerServerHandlerThread peerServerHandlerThread = new PeerServerHandlerThread(peerServerSocket, sharedDirectory);
        Thread t = new Thread(peerServerHandlerThread, "PeerServerHandlerThread");
        t.start();
    }

    private void searchFile(String fileName) throws IOException {
        peerOutputStream.writeUTF("search");
        peerOutputStream.writeUTF(fileName);
        int size = trackerInputDataStream.readInt();
        if (size > 0) {
            System.out.println("File '" + fileName + "' is available on " + size + " peers, use 'download' command for downloading...");
        }
        System.out.println("File " + fileName + " not found on the tracker...");
    }

    private void updateTracker() throws IOException {
        // send file update request to server
        System.out.println("Updating tracker with shared files...");
        peerOutputStream.writeUTF("update");
        File[] files = getSharedFiles();
        peerOutputStream.writeInt(files.length);
        for (File file : files) {
            peerOutputStream.writeUTF(file.getName());
        }
    }

    /**
     * Search for upload directory if doesn't exist create
     *
     * @return
     */
    private File[] getSharedFiles() {
        File directory = new File(sharedDirectory);
        if (!directory.exists()) {
            directory.mkdir();
            return new File[]{};
        }
        return directory.listFiles();
    }

    private void downloadFile(String fileName) throws IOException {
        peerOutputStream.writeUTF("search");
        peerOutputStream.writeUTF(fileName);
        int size = trackerInputDataStream.readInt();
        Set<Peer> availablePeers = new HashSet<>();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                String[] peer = trackerInputDataStream.readUTF().split(":");
                System.out.println(peer);
                availablePeers.add(new Peer(peer[0], Integer.parseInt(peer[1])));
            }
            System.out.println(availablePeers);
            // 1. Download file from one of the peer

            String pHost = "";
            Integer pPort = 0;

            Socket dnClient;
            DataOutputStream dnOut;
            DataInputStream dnIn;
            InputStream dnInput;
            BufferedOutputStream buffOut = null;
            FileOutputStream fileOut = null;
            String down = "down/" + fileName;

            dnClient = new Socket(pHost, pPort);        //connect to the peer

		/*Server IO streams are instantiated*/
            dnOut = new DataOutputStream(dnClient.getOutputStream());
            dnIn = new DataInputStream(dnClient.getInputStream());

            dnOut.writeUTF("get");            //send download request
            dnOut.writeUTF(fileName);        //send name of file to be downloaded
            int fileSize = dnIn.readInt();    //read size of file

            int bytesRead;
            int current = 0;

	    /*Initiate file receive using buffered stream*/
            try {
                System.out.println("\nRecieving file " + fileName + "...!");
                byte[] mybytearray = new byte[fileSize];
                dnInput = dnClient.getInputStream();
                fileOut = new FileOutputStream(down);
                buffOut = new BufferedOutputStream(fileOut);
                bytesRead = dnInput.read(mybytearray, 0, mybytearray.length);
                current = bytesRead;

                do {
                    bytesRead = dnInput.read(mybytearray, current, (mybytearray.length - current));
                    if (bytesRead >= 0) {
                        current += bytesRead;
                    }
                } while (bytesRead > 0);

                buffOut.write(mybytearray, 0, current);        //write the downloaded file
                buffOut.flush();
                System.out.println("File - " + fileName + " downloaded successfully !");

            } finally {
                dnClient.close();
                if (buffOut != null) buffOut.close();
            }
        }
    }
}
