/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversocket;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NguyenThanhDat
 */
public class ServerService {

    private final int PORT = 8082;

    public String startServer() {
        try {
            String IP = Inet4Address.getLocalHost().getHostAddress();
            String message = "IP: " + IP + "\n" + "PORT: " + PORT;
            return message;
        } catch (UnknownHostException ex) {
            Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error";
    }
}
