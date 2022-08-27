/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversocket;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientThread extends Thread {

    private Socket client;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private ServerGUI serverGUI;
    private Logger logger = Logger.getLogger("LogAction");

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

    public void createLog() throws IOException {
        LocalDateTime local = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");

        FileHandler fh = new FileHandler("Log-" + local.format(format) + ".log");
        SimpleFormatter formatter = new SimpleFormatter();
        logger.addHandler(fh);
        fh.setFormatter(formatter);
        logger.info("RENAME; 2018-10-5 21:23:25; A file txt was rename; 192.168.58.1");
        logger.info("RENAME; 2018-10-5 21:23:25; A file txt was rename; 192.168.58.1");
    }

    @Override
    public void run() {
        try {
            InputStream is = client.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            OutputStream os = client.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os));

            String receiveMsg = br.readLine();
            while (true) {
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

                if ("CREATE".equals(receiveMsg) || "DELETE".equals(receiveMsg)
                        || "LOG-IN".equals(receiveMsg) || "RENAME".equals(receiveMsg)
                        || "UPDATE".equals(receiveMsg)) {
                    String log = "";
                    ArrayList<String> detailActions = new ArrayList<>();
                    while (!receiveMsg.equals("END NOTIFY")) {
                        if ("connected".equals(receiveMsg)) {
                            receiveMsg = getName() + " " + receiveMsg;
                        }
                        log += receiveMsg + "; ";
                        detailActions.add(receiveMsg);
                        receiveMsg = br.readLine();
                    }
                    detailActions.add(getName());
                    log += getName();
                    serverGUI.loadTable(detailActions);
                    serverGUI.writeLog(log);
                }

                if ("NOTIFY".equals(receiveMsg)) {
                    receiveMsg = br.readLine();
                    String message = "\nServer is watching folder " + receiveMsg + " of " + getName();
                    serverGUI.updateTextArea(message);
                }
                receiveMsg = br.readLine();
            }
        } catch (IOException ex) {
        } finally {
            try {
                String message = getName() + " has quitted";
                ArrayList<String> detailActions = new ArrayList<>();
                LocalDateTime local = LocalDateTime.now();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String time = local.format(format);
                String log = "LOG-OUT; " + time + "; " + message + "; " + getName();
                serverGUI.writeLog(log);
                detailActions.add("LOG-OUT");
                detailActions.add(time);
                detailActions.add(message);
                detailActions.add(getName());
                serverGUI.loadTable(detailActions);
                if (bw != null) {
                    bw.close();
                }
                if (br != null) {
                    br.close();
                }
                serverGUI.updateTextArea("\n" + message);
                serverGUI.removeClient(getName());
            } catch (IOException ex1) {
            }
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
