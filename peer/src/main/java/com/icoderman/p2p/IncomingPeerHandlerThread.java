package com.icoderman.p2p;

import java.io.*;
import java.net.Socket;

public class IncomingPeerHandlerThread implements Runnable {

    private String sharedDirectory;
    private Socket incomingPeerSocket;
    private DataInputStream clientIn;
    private DataOutputStream clientOut;

    public IncomingPeerHandlerThread(Socket incomingPeerSocket, String sharedDirectory) {
        this.incomingPeerSocket = incomingPeerSocket;
        this.sharedDirectory = sharedDirectory;
    }

    @Override
    public void run() {
        try {
            processRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequests() throws IOException {
        /*Server IO streams are instantiated*/
        try {
            clientIn = new DataInputStream(incomingPeerSocket.getInputStream());
            clientOut = new DataOutputStream(incomingPeerSocket.getOutputStream());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String fileName;
        FileInputStream fis;
        FileOutputStream fileOut = null;
        BufferedInputStream bis;
        BufferedOutputStream buffOut = null;
        OutputStream ois;
        String file;

		/*Till the peer is available at port accept requests*/
        while(clientIn.available() != 0){

            String code;
            code = clientIn.readUTF();			//read the request from peer
            ois = incomingPeerSocket.getOutputStream();

			/*"GET" request will send the request file to the peer*/
            if(code.equalsIgnoreCase("GET")){
                fileName = clientIn.readUTF();

                file = sharedDirectory + fileName;
                File myFile = new File(file);

				/*Initiate file send using buffered stream*/
                byte [] mybytearray  = new byte [(int)myFile.length()];
                clientOut.writeInt((int)myFile.length());
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                ois.write(mybytearray,0,mybytearray.length);
                ois.flush();
            }

			/*"REPLICATE" request will receive the file to be replicated from peer */
            else if(code.equalsIgnoreCase("REPLICATE")){
                fileName = clientIn.readUTF();

                String down = sharedDirectory + fileName;
                int fileSize = clientIn.readInt();
                int bytesRead;
                int current = 0;

			    /*Initiate file send using buffered stream*/
                try {
                    byte [] mybytearray  = new byte [fileSize];
                    fileOut = new FileOutputStream(down);
                    buffOut = new BufferedOutputStream(fileOut);
                    bytesRead = clientIn.read(mybytearray,0,mybytearray.length);
                    current = bytesRead;

                    do {
                        bytesRead = clientIn.read(mybytearray, current, (mybytearray.length-current));
                        if(bytesRead >= 0){
                            current += bytesRead;
                        }
                    } while(bytesRead > 0);

                    buffOut.write(mybytearray, 0 , current);
                    buffOut.flush();
                }
                finally {
                    if (buffOut != null) buffOut.close();
                    if (fileOut != null) fileOut.close();
                }
            }
        }
    }
}
