package com.icoderman.p2p.service;

import com.icoderman.p2p.domain.PeerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PeerRequestService {

    private Socket peerSocket;
    private IndexService indexService;
    private int peerId;

    public PeerRequestService(Socket peerSocket, IndexService index, int peerId) {
        this.peerSocket = peerSocket;
        this.indexService = index;
        this.peerId = peerId;
    }

    // This method will take client requests and process it.
    public void processRequests(DataOutputStream trackerOutputStream, DataInputStream peerInputStream) {
        System.out.println("Process Request from Peer...");
        String fileName;
        int flag = 0;

        try {
            //till the client connection with server is bound, it will listen for requests from client
            while (peerSocket.isBound()) {
                String code;
                code = peerInputStream.readUTF();

				/* "UPDATE" request will update files available at client to central indexService */
                if (code.equalsIgnoreCase("UPDATE")) {
                    processUpdate(peerInputStream);
                }
                /* "SEARCH" request will search for client requested file central indexService */
                else if (code.equalsIgnoreCase("SEARCH")) {
                    processSearch(trackerOutputStream, peerInputStream);
                }
				/* "CLOSE" request will end the client connection with server */
                else if (code.equalsIgnoreCase("CLOSE")) {
                    flag = processClose();
                }
            }
        } catch (IOException e) {
            if (flag != 1) {
                System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] disconnected !");
            }
        }
    }

    private int processClose() throws IOException {
        int flag;
        flag = 1;
        indexService.removePeer(peerId);
        peerSocket.close();
        System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] disconnected !");
        return flag;
    }

    private void processSearch(DataOutputStream serverOut, DataInputStream clientIn) throws IOException {
        String fileName = clientIn.readUTF();
        System.out.println("Peer looking for file: "+fileName);
        Set<Integer> currentList = indexService.searchFiles(fileName);
        if (currentList.contains(peerId)) {
            currentList.remove(peerId);
        }
        int size = currentList.size();
        if (size > 0) {
            System.out.println("Size > 0");
            serverOut.writeInt(size);
            for (int peerId : currentList) {
                PeerInfo resPeer = indexService.searchPeers(peerId);
                String peer = resPeer.getHostName() + " " + resPeer.getPort();
                try {
                    serverOut.writeUTF(peer);
                } catch (IOException e) {
                    System.out.println("Peer [ " + (peerSocket.getInetAddress()).getHostAddress() + ":" + peerSocket.getPort() + " ] disconnected !");
                }
            }
        }
        System.out.println("writing 0");
        serverOut.write(0);
    }

    private void processUpdate(DataInputStream peerInputStream) throws IOException {
        List<String> files = new ArrayList<>();
        int filesCount = peerInputStream.readInt();
        for (int i = 0; i < filesCount; i++) {                //Receive file names from client sequentially
            files.add(peerInputStream.readUTF());
        }
        indexService.addFiles(files, peerId);    //addFiles() will add file details to central indexService
    }

}

