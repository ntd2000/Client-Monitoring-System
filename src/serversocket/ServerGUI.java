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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author NguyenThanhDat
 */
public class ServerGUI {

    private JFrame serverFrame = new JFrame("Server");
    private final int PORT = 9999;
    private ServerSocket serverSocket = null;
    private JTextArea systemBoxMessage = new JTextArea(10, 30);
    private ArrayList<ClientThread> clientThreads = new ArrayList<>();
    private DefaultListModel<String> modelListClient = new DefaultListModel<>();
    private JList clientList = new JList(modelListClient);
    private JTree treeFolder;
    private DefaultTableModel tableModel;
    private JTable table = new JTable();

    public void createServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void acceptClients() {
        ServerGUI serverGUI = this;
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket client = serverSocket.accept();
                        String ip = client.getInetAddress().getHostAddress();
                        ClientThread newClient = new ClientThread(client, serverGUI);
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
                                systemBoxMessage.append("\n" + name + " connected");
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

    public void removeClient(String ip) {
        for(int i = 0; i<clientThreads.size();i++){
            if(clientThreads.get(i).getName().equals(ip)){
                clientThreads.remove(i);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                modelListClient.removeElement(ip);
            }
        });
        for (ClientThread client1 : clientThreads) {
            System.out.println(client1.getName());
        }
    }

    public void createTreeRoot(ArrayList<String> roots, String ip) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                JDialog dialog = new JDialog(frame, "Tree Folder");
                JButton monitorBtn = new JButton("Monitoring");
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
                for (String disk : roots) {
                    root.add(new DefaultMutableTreeNode(disk));
                }
                treeFolder = new JTree(root);

                treeFolder.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                        try {
                            browseClient(ip, node.toString());
                        } catch (IOException ex) {
                            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        treeFolder.setShowsRootHandles(true);
                    }
                });

                monitorBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeFolder.getLastSelectedPathComponent();
                            monitorClient(ip, "MONITORING", node.toString());
                            JFrame frame1 = new JFrame();
                            JOptionPane.showMessageDialog(frame1,
                                    "Monitoring successfully!",
                                    "Notify",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                        } catch (IOException ex) {
                            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                treeFolder.setRootVisible(false);
                btnPanel.add(monitorBtn);
                dialog.setLayout(new BorderLayout());
                dialog.add(new JScrollPane(treeFolder), BorderLayout.CENTER);
                dialog.add(btnPanel, BorderLayout.PAGE_END);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(null);
                dialog.setModal(true);
                dialog.setResizable(false);
                dialog.setVisible(true);

                serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    public void createTreeFile(ArrayList<String> files) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeFolder.getLastSelectedPathComponent();
        int countTemp = 0;
        Enumeration children = node.children();
        while (true) {
            if (children.hasMoreElements()) {
                children.nextElement();
                countTemp++;
            } else {
                break;
            }
        }
        int count = countTemp;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (count != files.size()) {
                    node.removeAllChildren();
                    for (String file : files) {
                        node.add(new DefaultMutableTreeNode(file));
                    }
                }
            }
        });
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

    public void filterTable(JTextField field, TableRowSorter<TableModel> sorter) {
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
                sorter.setRowFilter(RowFilter.regexFilter(keyword));
            }
        });
    }

    public void browseClient(String ip, String command) throws IOException {
        for (ClientThread client : clientThreads) {
            if (client.getName().equals(ip)) {
                client.sendCommand(command);
            }
        }
    }

    public void monitorClient(String ip, String command, String path) throws IOException {
        for (ClientThread client : clientThreads) {
            if (client.getName().equals(ip)) {
                client.monitorFolder(command, path);
            }
        }
    }

    public void GUIAfterConnect() {
        String columns[] = {"STT", "Time", "Action", "IP", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        serverFrame = new JFrame("Server");
        JLabel searchLabel = new JLabel("Search: ");
        JButton browseBtn = new JButton("Browse");
        JTextField searchField = new JTextField(15);
        JTextField filterField = new JTextField(15);
        JLabel filterLabel = new JLabel("Filter: ");
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel servicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel boxMessagePanel = new JPanel();
        JPanel listClientPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel actionTablePanel = new JPanel(new BorderLayout());

        systemBoxMessage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        browseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();
                if (clientList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "You have not selected the client",
                            "Notify",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String selected = clientList.getSelectedValue().toString();
                        browseClient(selected, "ROOT");
                    } catch (IOException ex) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        filterTable(filterField, sorter);

        boxMessagePanel.add(new JScrollPane(systemBoxMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        boxMessagePanel.setBorder(BorderFactory.createTitledBorder("System Message"));
        listPanel.add(new JScrollPane(clientList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        listClientPanel.add(listPanel);
        listClientPanel.setBorder(BorderFactory.createTitledBorder("List Clients"));
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);

        table.setModel(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, 100));
        table.setFillsViewportHeight(true);
        JScrollPane spTable = new JScrollPane(table);

        tablePanel.add(spTable, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        actionTablePanel.add(tablePanel, BorderLayout.CENTER);
        actionTablePanel.setBorder(BorderFactory.createTitledBorder("Actions Table"));
        actionTablePanel.add(filterPanel, BorderLayout.PAGE_START);
        servicePanel.add(searchLabel);
        servicePanel.add(searchField);
        servicePanel.add(browseBtn);
        listClientPanel.add(servicePanel, BorderLayout.PAGE_START);
        contentPanel.add(boxMessagePanel, BorderLayout.CENTER);
        contentPanel.add(listClientPanel, BorderLayout.LINE_END);
        contentPanel.add(actionTablePanel, BorderLayout.PAGE_END);

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
        serverFrame.setResizable(false);
        acceptClients();
    }

    public void loadTable(ArrayList<String> detailActions) {
        Object[] obj = {table.getRowCount() + 1,
            detailActions.get(1),
            detailActions.get(0),
            detailActions.get(3),
            detailActions.get(2)};
        tableModel.addRow(obj);
    }

    public void updateTextArea(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                systemBoxMessage.append(message);
            }
        });
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
