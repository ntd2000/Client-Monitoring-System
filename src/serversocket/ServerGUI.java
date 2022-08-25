/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversocket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NguyenThanhDat
 */
public class ServerGUI {

    private JFrame serverFrame = new JFrame("Server");
    private final int PORT = 9999;
    private ServerSocket serverSocket = null;
    private JTextArea systemBoxMessage = new JTextArea(10, 10);

    public void createServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void updateText(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                systemBoxMessage.setText(message);
            }
        });
    }

    public void acceptClient() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = serverSocket.accept();
                    updateText("New user connected!");
                } catch (IOException ex) {
                    Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void GUIAfterConnect() {
        serverFrame = new JFrame("Server");
        JPanel boxMessagePanel = new JPanel();
        boxMessagePanel.add(systemBoxMessage);
        updateText("Server is waiting to accept user...");
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.add(boxMessagePanel);
        serverFrame.pack();
        serverFrame.setVisible(true);
        acceptClient();
    }

    public void createGUI() {
        JButton startBtn = new JButton("Start Server");
        JPanel btnPanel = new JPanel();
        JPanel contentPanel = new JPanel(new BorderLayout());

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "";
                boolean isSucces = true;
                Frame frame = new JFrame();
                try {
                    String IP = Inet4Address.getLocalHost().getHostAddress();
                    createServer();
                    message = "IP: " + IP + "\n" + "PORT: " + PORT;
                } catch (UnknownHostException ex) {
                    message = "Error! Cannot get IP address!";
                    isSucces = false;
                } catch (IOException ex) {
                    message = "Error! Cannot start server!";
                    isSucces = false;
                }
                if (isSucces == true) {
                    JOptionPane.showMessageDialog(frame,
                            message,
                            "IP/PORT",
                            JOptionPane.INFORMATION_MESSAGE);
                    serverFrame.dispose();
                    GUIAfterConnect();
                } else {
                    JOptionPane.showMessageDialog(frame,
                            message,
                            "IP/PORT",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        btnPanel.add(startBtn);
        contentPanel.add(btnPanel, BorderLayout.CENTER);

        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.getContentPane().add(contentPanel);
        serverFrame.setVisible(true);
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
    }
}
