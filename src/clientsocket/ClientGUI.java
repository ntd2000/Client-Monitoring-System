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
import javax.swing.*;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientGUI {

    private JFrame clientFrame = new JFrame("Client");
    private JTextArea systemBoxMessage = new JTextArea(10, 10);

    public void connectServer(String ip, int port) {
        Thread connect = new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip, port);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Frame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Connect succesfully!",
                                    "Notify",
                                    JOptionPane.INFORMATION_MESSAGE);
                            clientFrame.dispose();
                            GUIAfterConnect();
                        }
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Frame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Connect failure! You should check the IP or PORT!",
                                    "Notify",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        };
        connect.start();
    }

    public void GUIAfterConnect() {
        clientFrame = new JFrame("Client");
        JPanel boxMessagePanel = new JPanel();
        boxMessagePanel.add(systemBoxMessage);
        systemBoxMessage.setText("Welcome!");
        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.add(boxMessagePanel);
        clientFrame.pack();
        clientFrame.setVisible(true);
    }

    public void createGUI() {
        JLabel headerLabel = new JLabel("Connect Server");
        JLabel labelIP = new JLabel("IP: ");
        JLabel labelPORT = new JLabel("PORT: ");
        JTextField fieldIP = new JTextField(10);
        JTextField fieldPORT = new JTextField(10);
        JButton connBtn = new JButton("Connect");

        JPanel contentPanel = new JPanel();
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, -60, 0));
        JPanel formPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        BoxLayout boxLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
        contentPanel.setLayout(boxLayout);

        connBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = fieldIP.getText();
                int port = Integer.valueOf(fieldPORT.getText());
                connectServer(ip, port);
            }
        });

        inputPanel.add(labelIP);
        inputPanel.add(fieldIP);
        inputPanel.add(labelPORT);
        inputPanel.add(fieldPORT);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        btnPanel.add(connBtn);
        headerPanel.add(headerLabel);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        formPanel.add(inputPanel, BorderLayout.CENTER);
        formPanel.add(btnPanel, BorderLayout.PAGE_END);
        formPanel.setBorder(BorderFactory.createTitledBorder("Form"));
        contentPanel.add(headerPanel);
        contentPanel.add(formPanel);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 40, 40));

        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.getContentPane().add(contentPanel);
        clientFrame.pack();
        clientFrame.setLocationRelativeTo(null);
        clientFrame.setVisible(true);;
        clientFrame.setResizable(false);
    }

}
