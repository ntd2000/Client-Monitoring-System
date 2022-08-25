/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientsocket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientGUI {

    private JFrame clientFrame = new JFrame("Client");
    private JTextArea systemBoxMessage = new JTextArea(10, 10);

    public void updateText(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                systemBoxMessage.setText(message);
            }
        });
    }
    
    public void GUIAfterConnect() {
        clientFrame = new JFrame("Client");
        JPanel boxMessagePanel = new JPanel();
        boxMessagePanel.add(systemBoxMessage);
        updateText("Welcome!");
        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.add(boxMessagePanel);
        clientFrame.pack();
        clientFrame.setVisible(true);
    }
    
    public void createGUI() {
        JLabel headerLabel = new JLabel("Connect Server", JLabel.CENTER);
        JLabel labelIP = new JLabel("IP: ");
        JLabel labelPORT = new JLabel("PORT: ");
        JTextField fieldIP = new JTextField(10);
        JTextField fieldPORT = new JTextField(10);
        JButton connBtn = new JButton("Connect");

        JPanel contentPanel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel formPanel = new JPanel(new GridLayout(2, 2, -60, 0));

        connBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Frame frame = new JFrame();
                String ip = fieldIP.getText();
                int port = Integer.valueOf(fieldPORT.getText());
                try {
                    Socket socket = new Socket(ip, port);
                    JOptionPane.showMessageDialog(frame,
                            "Connect succesfully!",
                            "IP/PORT",
                            JOptionPane.INFORMATION_MESSAGE);
                    clientFrame.dispose();
                    GUIAfterConnect();
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        formPanel.add(labelIP);
        formPanel.add(fieldIP);
        formPanel.add(labelPORT);
        formPanel.add(fieldPORT);
        btnPanel.add(connBtn);
        contentPanel.add(headerLabel, BorderLayout.PAGE_START);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(btnPanel, BorderLayout.PAGE_END);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.getContentPane().add(contentPanel);
        clientFrame.pack();
        clientFrame.setLocationRelativeTo(null);
        clientFrame.setVisible(true);;
    }

}
