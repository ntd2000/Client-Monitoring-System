/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversocket;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientThread extends Thread {

    private Socket socket;
    private BufferedWriter writer;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientThread) {
            ClientThread client = (ClientThread) obj;
            if (this.getName().equals(client.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.socket);
        return hash;
    }
    
    @Override
    public void run() {
        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
