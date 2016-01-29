package com.icoderman.p2p;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Peer {

    public static void main(String[] args) {
        int trackerPort;
        int peerPort;
        String trackerHost;

        ServerSocket trackerSocket;
        Socket peerSocket;

        InputStream trackerInputStream;
        DataInputStream peerInputStream;
        DataOutputStream peerOutputStream;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Tracker address:");
        trackerHost = scanner.nextLine();
        System.out.print("Enter Tracker port:");
        trackerPort = Integer.parseInt(scanner.nextLine());

        try {
            peerSocket = new Socket(trackerHost, trackerPort);
            peerPort = peerSocket.getLocalPort() + 1;
            trackerSocket = new ServerSocket(peerPort);

            trackerInputStream = peerSocket.getInputStream();
            peerInputStream = new DataInputStream(trackerInputStream);
            peerOutputStream = new DataOutputStream(peerSocket.getOutputStream());
            if(peerInputStream.readByte() == 1){				//if server connection is successful display message
                System.out.println("Successfully connected to the Tracker");
                updateTracker(peerSocket, peerOutputStream);

                String command = "";
                while(!command.equals("CLOSE")){
                    System.out.println("Please enter command (CLOSE, SEARCH, DOWNLOAD): ");
                    command = scanner.nextLine();
                    switch (command) {
                        case "CLOSE":
                            peerOutputStream.writeUTF("CLOSE");
                            System.out.println("Disconnected from Tracker...");
                            break;
                        case "SEARCH":
                            searchFile(peerOutputStream, peerInputStream);
                            break;
                        case "DOWNLOAD":
                            downloadFile();
                            break;
                    }
                }

            }
            /*
            locManager = new ClientManager(trackerSocket);
            Thread t = new Thread(locManager);			//a thread is created for the local server
            t.start();
*/
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            System.out.println("Error: Connection to the Tracker failed!");
            e.printStackTrace();
        }
        scanner.close();
    }

    private static void searchFile(DataOutputStream peerOutputStream, DataInputStream peerInputStream) throws IOException {
        String fileName;
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the file name (with extension): ");
        fileName = scan.nextLine();
        peerOutputStream.writeUTF("SEARCH");
        peerOutputStream.writeUTF(fileName);
        int size = peerInputStream.readInt();
        if (size > 0) {
            System.out.println("File '"+fileName+"' is available on "+size+" peers, use DOWNLOAD command for downloading...");
        }
        scan.close();
    }

    private static void updateTracker(Socket myClient, DataOutputStream clientOut) throws IOException {
        // send file update request to server
        System.out.println("Updating tracker with shared files...");
        clientOut.writeUTF("UPDATE");
        File[] files = getSharedFiles();
        clientOut.writeInt(files.length);
        for(File file : files) {
            clientOut.writeUTF(file.getName());
        }
    }

    /**
     * Search for upload directory if doesn't exist create
     * @return
     */
    private static File[] getSharedFiles() {
        File directory = new File("shared");
        if(!directory.exists()){
            directory.mkdir();
            return new File[]{};
        }
        return directory.listFiles();
    }

    private static void downloadFile() {

    }
}
