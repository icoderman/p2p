package com.icoderman.p2p;

import com.icoderman.p2p.domain.Peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PeerApp {

    private static final int SUCCESS_CONNECTION = 1;
    private static String SHARED_DIRECTORY;

    public static void main(String[] args) {
        SHARED_DIRECTORY = args[0];
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter shared directory: " + SHARED_DIRECTORY);
        System.out.print("Enter Tracker address:port:");
        String[] trackerHostPort = scanner.nextLine().split(":");
        try {
            Socket trackerSocket = new Socket(trackerHostPort[0], Integer.parseInt(trackerHostPort[1]));
            int peerPort = trackerSocket.getLocalPort() + 1;
            ServerSocket peerServerSocket = new ServerSocket(peerPort);

            InputStream trackerInputStream = trackerSocket.getInputStream();
            DataInputStream trackerInputDataStream = new DataInputStream(trackerInputStream);
            DataOutputStream peerOutputStream = new DataOutputStream(trackerSocket.getOutputStream());

            if (trackerInputDataStream.readByte() == SUCCESS_CONNECTION) {
                System.out.println("Successfully connected to the Tracker");
                updateTracker(peerOutputStream);
                PeerServerHandlerThread peerServerHandlerThread =  new PeerServerHandlerThread(peerServerSocket, SHARED_DIRECTORY);
                Thread t = new Thread(peerServerHandlerThread, "PeerServerHandlerThread");
                t.start();

                String command = "";
                String fileName = "";
                while (!command.equals("CLOSE")) {
                    System.out.println("Please enter command (CLOSE, SEARCH, DOWNLOAD): ");
                    command = scanner.nextLine();
                    switch (command) {
                        case "UPDATE":
                            updateTracker(peerOutputStream);
                            break;
                        case "SEARCH":
                            System.out.println("Enter the file name (with extension): ");
                            fileName = scanner.nextLine();
                            searchFile(fileName, peerOutputStream, trackerInputDataStream);
                            break;
                        case "DOWNLOAD":
                            System.out.println("Enter the file name (with extension): ");
                            fileName = scanner.nextLine();
                            downloadFile(fileName, peerOutputStream, trackerInputDataStream);
                            break;
                        case "CLOSE":
                            peerOutputStream.writeUTF("CLOSE");
                            peerServerSocket.close();
                            System.out.println("Disconnected from Tracker...");
                            break;
                        default:
                            System.out.println("Available commands: UPDATE, SEARCH, DOWNLOAD, CLOSE");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: Connection to the Tracker failed!");
            e.printStackTrace();
        }
        scanner.close();
    }

    private static void searchFile(String fileName, DataOutputStream peerOutputStream, DataInputStream trackerInputDataStream) throws IOException {
        peerOutputStream.writeUTF("SEARCH");
        peerOutputStream.writeUTF(fileName);
        int size = trackerInputDataStream.readInt();
        if (size > 0) {
            System.out.println("File '" + fileName + "' is available on " + size + " peers, use DOWNLOAD command for downloading...");
        }
        System.out.println("File " + fileName + " not found on the tracker...");
    }

    private static void updateTracker(DataOutputStream clientOut) throws IOException {
        // send file update request to server
        System.out.println("Updating tracker with shared files...");
        clientOut.writeUTF("UPDATE");
        File[] files = getSharedFiles();
        clientOut.writeInt(files.length);
        for (File file : files) {
            clientOut.writeUTF(file.getName());
        }
    }

    /**
     * Search for upload directory if doesn't exist create
     *
     * @return
     */
    private static File[] getSharedFiles() {
        File directory = new File(SHARED_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
            return new File[]{};
        }
        return directory.listFiles();
    }

    private static void downloadFile(String fileName, DataOutputStream peerOutputStream, DataInputStream trackerInputDataStream) throws IOException {
        peerOutputStream.writeUTF("SEARCH");
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

            dnOut.writeUTF("GET");            //send download request
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
