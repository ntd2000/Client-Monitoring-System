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

/**
 *
 * @author NguyenThanhDat
 */
public class ServerGUI {

    private JFrame serverFrame = new JFrame("Server");

    public void createGUI() {
        ServerService service = new ServerService();
        JButton startBtn = new JButton("Start Server");
        JPanel panel = new JPanel();

        panel.add(startBtn);

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = service.startServer();
                Frame frame = new JFrame();
                JOptionPane.showMessageDialog(frame,
                        message,
                        "IP/PORT",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.getContentPane().add(panel, BorderLayout.CENTER);
        serverFrame.pack();
        serverFrame.setVisible(true);
    }
}
