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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
    private Logger logger = Logger.getLogger("LogAction");
    private String logCurrentName;

    public void createServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        Thread createLogTheard = new Thread() {
            @Override
            public void run() {
                try {
                    LocalDateTime local = LocalDateTime.now();
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
                    logCurrentName = "Log-" + local.format(format) + ".log";
                    FileHandler fh = new FileHandler("logs/" + logCurrentName);
                    SimpleFormatter formatter = new SimpleFormatter();
                    logger.addHandler(fh);
                    fh.setFormatter(formatter);
                } catch (IOException ex) {
                    Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        createLogTheard.start();
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
                            ip += "(" + clientThreads.size() + ")";
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
        for (int i = 0; i < clientThreads.size(); i++) {
            if (clientThreads.get(i).getName().equals(ip)) {
                clientThreads.remove(i);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                modelListClient.removeElement(ip);
            }
        });
    }

    public void writeLog(String log) {
        logger.info(log);
    }

    public void readLog(String logFile, JTable tabelLog, DefaultTableModel tabelLogModel) {
        DefaultTableModel model = (DefaultTableModel) tabelLog.getModel();
        model.setRowCount(0);
        Thread readLogThread = new Thread() {
            @Override
            public void run() {
                BufferedReader reader = null;
                try {
                    ArrayList<String> logs = new ArrayList<>();
                    String COMMA_DELIMITER = ";";
                    reader = new BufferedReader(new FileReader(logFile));
                    do {
                        ArrayList<String> detailActions = new ArrayList<>();
                        String log = reader.readLine();
                        if (log == null) {
                            break;
                        }
                        log = reader.readLine();
                        String[] splitData = log.split(COMMA_DELIMITER);
                        String[] kind = splitData[0].split(":");
                        detailActions.add(kind[1].trim());
                        for (int i = 1; i < splitData.length; i++) {
                            detailActions.add(splitData[i].trim());
                        }
                        loadTableLog(detailActions, tabelLog, tabelLogModel);
                    } while (true);
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JFrame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Cannot load file logs!",
                                    "Notify",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JFrame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Cannot load file logs! You should check the file content format!",
                                    "Notify",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException crunchifyException) {
                    }
                }
            }
        };
        readLogThread.start();
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
                            JFrame frame1 = new JFrame();
                            if (node == null) {
                                JOptionPane.showMessageDialog(frame1,
                                        "You have not selected folder!",
                                        "Notify",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                monitorClient(ip, "MONITORING", node.toString());
                                dialog.dispose();
                                JOptionPane.showMessageDialog(frame1,
                                        "Monitoring successfully!",
                                        "Notify",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }

                        } catch (IOException ex) {
                            Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                treeFolder.setRootVisible(false);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                btnPanel.add(monitorBtn);
                dialog.setLayout(new BorderLayout());
                dialog.add(new JScrollPane(treeFolder), BorderLayout.CENTER);
                dialog.add(btnPanel, BorderLayout.PAGE_END);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(null);
                dialog.setModal(true);
                dialog.setResizable(false);
                dialog.setVisible(true);
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

    public ArrayList<String> loadFileLog(String logCurrentName) {
        ArrayList<String> logFiles = new ArrayList<>();
        Thread loadLogThread = new Thread() {
            @Override
            public void run() {
                File dir = new File("logs");
                if (dir.isDirectory()) {
                    File[] logFile = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getAbsolutePath().endsWith(".log") && !pathname.getName().equals(logCurrentName)) {
                                return true;
                            }
                            return false;
                        }
                    });
                    for (File file : logFile) {
                        logFiles.add(file.getName());
                    }
                }
            }
        };
        loadLogThread.start();
        do {
            if (loadLogThread.getState() == Thread.State.TERMINATED) {
                return logFiles;
            }
        } while (true);
    }

    public void createTreeLog(ArrayList<String> logFiles) {
        String columns[] = {"STT", "Time", "Action", "IP", "Description"};
        DefaultTableModel tableTreeModel = new DefaultTableModel(columns, 0);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableTreeModel);

        JTable tableLog = new JTable();
        JTextField filterField = new JTextField(15);
        JButton logBtn = new JButton("Logs");
        JLabel filterLabel = new JLabel("Filter: ");
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tableLog.setRowSorter(sorter);
        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel actionPanel = new JPanel(new BorderLayout());
        JPanel treePanel = new JPanel();
        JFrame frame = new JFrame();
        JDialog dialog = new JDialog(frame, "Tree Log");
        JButton loadBtn = new JButton("Load");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        for (String file : logFiles) {
            root.add(new DefaultMutableTreeNode(file));
        }

        
        treeFolder = new JTree(root);
        treeFolder.setRootVisible(false);
        treeFolder.setPreferredSize(new Dimension(200, treeFolder.getPreferredSize().height));
        btnPanel.add(loadBtn);
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);

        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeFolder.getLastSelectedPathComponent();
                JFrame frame1 = new JFrame();
                if (node == null) {
                    JOptionPane.showMessageDialog(frame1,
                            "You have not selected log file!",
                            "Notify",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    readLog("logs/" + node.toString(), tableLog, tableTreeModel);
                }
            }
        });

        treePanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        treePanel.add(new JScrollPane(treeFolder));
        actionPanel.setPreferredSize(new Dimension(700, actionPanel.getPreferredSize().height));
        tableLog.setModel(tableTreeModel);
        tableLog.setFillsViewportHeight(true);
        JScrollPane spTable = new JScrollPane(tableLog);

        tablePanel.add(spTable, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.add(tablePanel, BorderLayout.CENTER);
        actionPanel.add(filterPanel, BorderLayout.PAGE_START);

        filterTable(filterField, sorter);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(treePanel, BorderLayout.CENTER);
        dialog.add(actionPanel, BorderLayout.LINE_END);
        dialog.add(btnPanel, BorderLayout.PAGE_END);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setVisible(true);

    }

    public void loadTableLog(ArrayList<String> detailActions, JTable tableLog, DefaultTableModel tableLogModel) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object[] obj = {tableLog.getRowCount() + 1,
                    detailActions.get(1),
                    detailActions.get(0),
                    detailActions.get(3),
                    detailActions.get(2)};
                tableLogModel.addRow(obj);
            }
        });
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
        JButton logBtn = new JButton("Logs");
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

        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTreeLog(loadFileLog(logCurrentName));
            }
        });

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
        filterPanel.add(logBtn);

        table.setModel(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, 200));
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object[] obj = {table.getRowCount() + 1,
                    detailActions.get(1),
                    detailActions.get(0),
                    detailActions.get(3),
                    detailActions.get(2)};
                tableModel.addRow(obj);
            }
        });
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
