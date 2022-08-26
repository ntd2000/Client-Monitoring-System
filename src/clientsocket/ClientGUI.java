/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientsocket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientGUI {

    private JFrame clientFrame = new JFrame("Client");
    private JTextArea systemBoxMessage = new JTextArea(10, 20);
    private BufferedWriter bw;

    public void fileRoots() throws IOException {
        bw.write("ROOT");
        bw.newLine();
        bw.flush();
        File[] roots = File.listRoots();
        for (File root : roots) {
            bw.write(root.getAbsolutePath());
            bw.newLine();
            bw.flush();
        }
        bw.write("END ROOT");
        bw.newLine();
        bw.flush();
    }

    public void listFile(String path) throws IOException {
        bw.write("FILE");
        bw.newLine();
        bw.flush();
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (File file : children) {
                if (file.isDirectory() && !file.isHidden()) {
                    bw.write(file.getAbsolutePath());
                    bw.newLine();
                    bw.flush();
                }
            }
        }
        bw.write("END FILE");
        bw.newLine();
        bw.flush();
    }

    public void watchFolder(String path) {
        Thread watchThread = new Thread() {
            @Override
            public void run() {
                try {
                    WatchService watcher = FileSystems.getDefault().newWatchService();
                    Path dir;
                    if (path.equals("")) {
                        dir = Paths.get("C:/ClientMonitoringSystem/Data");
                    } else {
                        dir = Paths.get(path);
                    }
                    dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    System.out.println("Watch Service registered for dir: " + dir.getFileName());

                    WatchKey key = null;
                    while (true) {
                        try {
                            key = watcher.take();
                        } catch (InterruptedException ex) {
                            System.out.println("InterruptedException: " + ex.getMessage());
                            return;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                long lastModified = fileName.toFile().lastModified();
                                Date date = new Date(lastModified);
                                String desc = "A file" + fileName.getFileName() + " was created";
                                bw.write("CREATE");
                                bw.newLine();
                                bw.flush();
                                bw.write(date.toString());
                                bw.newLine();
                                bw.flush();
                                bw.write(desc);
                                bw.newLine();
                                bw.flush();
                                bw.write("END NOTIFY");
                                bw.newLine();
                                bw.flush();
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                System.out.printf("A file %s was modified.%n", fileName.getFileName());
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                long lastModified = fileName.toFile().lastModified();
                                Date date = new Date(lastModified);
                                String desc = "A file" + fileName.getFileName() + " was deleted";
                                bw.write("DELETE");
                                bw.newLine();
                                bw.flush();
                                bw.write(date.toString());
                                bw.newLine();
                                bw.flush();
                                bw.write(desc);
                                bw.newLine();
                                bw.flush();
                                bw.write("END NOTIFY");
                                bw.newLine();
                                bw.flush();
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        watchThread.start();
    }

    public void communicate(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        OutputStream os = socket.getOutputStream();
        bw = new BufferedWriter(new OutputStreamWriter(os));

        String receiveMsg = br.readLine();
        while (true) {
            System.out.println(receiveMsg);
            if (receiveMsg.equals("ROOT")) {
                fileRoots();
            } else if (receiveMsg.equals("MONITORING")) {
                String path = br.readLine();
                watchFolder(path);
            } else {
                System.out.println(receiveMsg);
                listFile(receiveMsg);
            }
            receiveMsg = br.readLine();
        }
    }

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
                    watchFolder("");
                    communicate(socket);
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
        systemBoxMessage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        boxMessagePanel.add(new JScrollPane(systemBoxMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        boxMessagePanel.setBorder(BorderFactory.createTitledBorder("System Message"));

        Font font = new Font("Times New Roman", Font.BOLD, 14);
        systemBoxMessage.setFont(font);
        systemBoxMessage.setText("Connected to the server");
        systemBoxMessage.setEditable(false);

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
