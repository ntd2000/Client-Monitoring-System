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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author NguyenThanhDat
 */
public class ServerGUI {

    private JFrame serverFrame = new JFrame("Server");
    private final int PORT = 9999;
    private ServerSocket serverSocket = null;
    private JTextArea systemBoxMessage = new JTextArea(10, 20);
    private ArrayList<ClientThread> clientThreads = new ArrayList<>();
    private DefaultListModel<String> modelListClient = new DefaultListModel<>();
    private JList clientList = new JList(modelListClient);

    public void createServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void acceptClients() {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket client = serverSocket.accept();
                        String ip = client.getInetAddress().getHostAddress();
                        ClientThread newClient = new ClientThread(client);
                        if (clientThreads.contains(newClient)) {
                            ip += "(1)";
                        }
                        String name = ip;
                        newClient.setName(ip);
                        clientThreads.add(newClient);
                        newClient.start();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                modelListClient.addElement(name);
                                systemBoxMessage.append("\n[" + name + "]" + " connected");
                            }
                        });
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        serverThread.start();
    }

    public void filterList(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            public void filter() {
                String keyword = field.getText();
                for (ClientThread client : clientThreads) {
                    String name = client.getName();
                    if (!name.startsWith(keyword)) {
                        if (modelListClient.contains(name)) {
                            modelListClient.removeElement(name);
                        }
                    } else {
                        if (!modelListClient.contains(name)) {
                            modelListClient.addElement(name);
                        }
                    }
                }
            }
        });
    }

    public void GUIAfterConnect() {
        serverFrame = new JFrame("Server");
        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField(20);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel boxMessagePanel = new JPanel();
        JPanel listClientPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel();
        JPanel listPanel = new JPanel();
        systemBoxMessage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        boxMessagePanel.add(new JScrollPane(systemBoxMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        boxMessagePanel.setBorder(BorderFactory.createTitledBorder("System Message"));
        listPanel.add(new JScrollPane(clientList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        listClientPanel.add(listPanel);
        listClientPanel.setBorder(BorderFactory.createTitledBorder("List Clients"));

        contentPanel.add(boxMessagePanel);
        contentPanel.add(listClientPanel);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        listClientPanel.add(searchPanel, BorderLayout.PAGE_START);

        Font font = new Font("Times New Roman", Font.BOLD, 14);
        systemBoxMessage.setFont(font);
        systemBoxMessage.setText("Server is waiting to accept user...");
        systemBoxMessage.setEditable(false);

        filterList(searchField);

        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.add(contentPanel);
        serverFrame.pack();
        serverFrame.setVisible(true);
        serverFrame.setLocationRelativeTo(null);
        acceptClients();
    }

    public void createGUI() {
        JButton startBtn = new JButton("Start Server");
        JLabel headerLabel = new JLabel("Welcome!");

        JPanel btnPanel = new JPanel();
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel contentPanel = new JPanel(new BorderLayout());
        BoxLayout boxLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
        contentPanel.setLayout(boxLayout);

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "";
                boolean isSucces = true;
                Frame frame = new JFrame();
                try {
                    String IP = Inet4Address.getLocalHost().getHostAddress();
                    createServer();
                    message = "Start Server Succesfully!\n" + "IP: " + IP + "\n" + "PORT: " + PORT
                            + "\nClick OK to start listen clients";
                } catch (UnknownHostException ex) {
                    message = "Error! Cannot get IP address!\n";
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
                    try {
                        if (!serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                    } catch (IOException ex1) {
                        message = ex1.getMessage();
                    } finally {
                        JOptionPane.showMessageDialog(frame,
                                message,
                                "IP/PORT",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });

        btnPanel.add(startBtn);
        headerPanel.add(headerLabel);
        contentPanel.add(headerPanel);
        contentPanel.add(btnPanel);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.getContentPane().add(contentPanel);
        serverFrame.setVisible(true);
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
        serverFrame.setResizable(false);
    }
}
