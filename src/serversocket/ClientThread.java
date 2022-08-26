/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversocket;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientThread extends Thread {

    private Socket client;
    private BufferedWriter bw;
    private ServerGUI serverGUI;

    public ClientThread(Socket client, ServerGUI serverGUI) {
        this.client = client;
        this.serverGUI = serverGUI;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientThread) {
            ClientThread anotherClient = (ClientThread) obj;
            if (this.getName().equals(anotherClient.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.client);
        return hash;
    }

    @Override
    public void run() {
        try {
            InputStream is = client.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            OutputStream os = client.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));

            String receiveMsg = br.readLine();
            while (receiveMsg != null) {
                if ("ROOT".equals(receiveMsg)) {
                    ArrayList<String> roots = new ArrayList<>();
                    receiveMsg = br.readLine();
                    while (!receiveMsg.equals("END ROOT")) {
                        roots.add(receiveMsg);
                        receiveMsg = br.readLine();
                    }
                    serverGUI.createTreeRoot(roots, getName());
                }
                if ("FILE".equals(receiveMsg)) {
                    ArrayList<String> files = new ArrayList<>();
                    receiveMsg = br.readLine();
                    while (!receiveMsg.equals("END FILE")) {
                        files.add(receiveMsg);
                        receiveMsg = br.readLine();
                    }
                    serverGUI.createTreeFile(files);
                }

                if ("CREATE".equals(receiveMsg) || "DELETE".equals(receiveMsg)) {
                    ArrayList<String> actionClient = new ArrayList<>();
                    receiveMsg = br.readLine();
                }

                receiveMsg = br.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendCommand(String command) throws IOException {
        bw.write(command);
        bw.newLine();
        bw.flush();
    }

    public void monitorFolder(String command, String path) throws IOException {
        bw.write(command);
        bw.newLine();
        bw.flush();
        bw.write(path);
        bw.newLine();
        bw.flush();
    }
}
