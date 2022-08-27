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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author NguyenThanhDat
 */
public class ClientGUI {

    private JFrame clientFrame;
    private JTextArea systemBoxMessage;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private Socket socket;
    private DefaultTableModel tableModel;
    private JTable table = new JTable();
    private Logger logger;
    private String logCurrentName;
    private WatchService watcher = null;
    private WatchKey key = null;

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

    
    //Tham khảo cách khởi tạo Watcher cách bắt sự kiện 
    //Link: https://www.baeldung.com/java-nio2-watchservice
    public void watchFolder(String path) {
        Thread watchThread = new Thread() {
            @Override
            public void run() {
                try {
                    watcher = FileSystems.getDefault().newWatchService();
                    Path dir;
                    if (path.equals("")) {
                        dir = Paths.get("C:/ClientMonitoringSystem/Data");
                    } else {
                        dir = Paths.get(path);
                    }
                    dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            systemBoxMessage.append("\nServer is watching folder " + dir.getFileName());
                        }
                    });

                    bw.write("NOTIFY");
                    bw.newLine();
                    bw.flush();
                    bw.write(dir.getFileName().toString());
                    bw.newLine();
                    bw.flush();

                    while ((key = watcher.take()) != null) {
                        String kindTemp = "";
                        String time1 = "";
                        String time2 = "";
                        String desc = "";
                        String nameFile = "";
                        String nameFileCreate = "";
                        String nameFileDelete = "";
                        ArrayList<String> detailActions = new ArrayList<>();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                LocalDateTime local = LocalDateTime.now();
                                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                time1 = local.format(format);
                                nameFile = fileName.getFileName().toString();
                                nameFileCreate = nameFile;
                                desc = "A file " + nameFile + " was created";
                                kindTemp = "CREATE";
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                LocalDateTime local = LocalDateTime.now();
                                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                nameFile = fileName.getFileName().toString();
                                time1 = local.format(format);
                                desc = "A file " + nameFile + " was updated";
                                kindTemp = "UPDATE";
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                LocalDateTime local = LocalDateTime.now();
                                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                time2 = local.format(format);
                                nameFile = fileName.getFileName().toString();
                                nameFileDelete = nameFile;
                                desc = "A file " + nameFile + " was deleted";
                                kindTemp = "DELETE";
                            }

                        }
                        if (time1.equals(time2)) {
                            kindTemp = "RENAME";
                            desc = "A file " + nameFileDelete + " was renamed to " + nameFileCreate;
                            bw.write(kindTemp);
                            bw.newLine();
                            bw.flush();
                            bw.write(time1);
                            bw.newLine();
                            bw.flush();
                            bw.write(desc);
                            bw.newLine();
                            bw.flush();
                            bw.write("END NOTIFY");
                            bw.newLine();
                            bw.flush();
                        } else {
                            if (kindTemp.equals("DELETE")) {
                                time1 = time2;
                            }
                            bw.write(kindTemp);
                            bw.newLine();
                            bw.flush();
                            bw.write(time1);
                            bw.newLine();
                            bw.flush();
                            bw.write(desc);
                            bw.newLine();
                            bw.flush();
                            bw.write("END NOTIFY");
                            bw.newLine();
                            bw.flush();
                        }
                        String log = kindTemp + "; " + time1 + "; " + desc;
                        detailActions.add(time1);
                        detailActions.add(kindTemp);
                        detailActions.add(desc);
                        loadTable(detailActions);
                        writeLog(log);
                        key.reset();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        watchThread.start();
    }

    public void communicate() throws IOException {
        InputStream is = socket.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));

        OutputStream os = socket.getOutputStream();
        bw = new BufferedWriter(new OutputStreamWriter(os));

        LocalDateTime local = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String time = local.format(format);
        bw.write("LOG-IN");
        bw.newLine();
        bw.flush();
        bw.write(time);
        bw.newLine();
        bw.flush();
        bw.write("connected");
        bw.newLine();
        bw.flush();
        bw.write("END NOTIFY");
        bw.newLine();
        bw.flush();

        do {
            String receiveMsg = br.readLine();
            if (receiveMsg.equals("ROOT")) {
                fileRoots();
            } else if (receiveMsg.equals("MONITORING")) {
                String path = br.readLine();
                watchFolder(path);
            } else {
                listFile(receiveMsg);
            }
        } while (true);
    }

    public void connectServer(String ip, int port) {
        Thread connect = new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    Thread createLogTheard = new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (logger == null) {
                                    logger = Logger.getLogger("Logger");
                                    LocalDateTime local = LocalDateTime.now();
                                    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
                                    logCurrentName = "Log-" + local.format(format) + ".log";
                                    FileHandler fh = new FileHandler("logs/" + logCurrentName);
                                    SimpleFormatter formatter = new SimpleFormatter();
                                    logger.addHandler(fh);
                                    fh.setFormatter(formatter);
                                }
                            } catch (IOException ex) {
                            }
                        }
                    };
                    createLogTheard.start();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Frame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Connect succesfully!",
                                    "Notify",
                                    JOptionPane.INFORMATION_MESSAGE);
                            clientFrame.dispose();
                            try {
                                GUIAfterConnect();
                            } catch (IOException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    communicate();
                } catch (IOException ex) {
                    try {
                        if (br != null) {
                            bw.close();
                        }
                        if (bw != null) {
                            br.close();
                        }
                    } catch (IOException ex1) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            clientFrame.dispose();
                            Frame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                                    "Error!" + ex.getMessage(),
                                    "Notify",
                                    JOptionPane.ERROR_MESSAGE);
                            createGUI();
                        }
                    });
                }
            }
        };
        connect.start();
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

    public void loadTable(ArrayList<String> detailActions) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object[] obj = {table.getRowCount() + 1,
                    detailActions.get(0),
                    detailActions.get(1),
                    detailActions.get(2)};
                tableModel.addRow(obj);
            }
        });
    }

    public void createTreeLog(ArrayList<String> logFiles) {
        String columns[] = {"STT", "Time", "Action", "Description"};
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

        JTree treeFolder = new JTree(root);
        treeFolder.setRootVisible(false);
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

        treeFolder.setPreferredSize(new Dimension(200, treeFolder.getPreferredSize().height));
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
                    detailActions.get(0),
                    detailActions.get(1),
                    detailActions.get(2)};
                tableLogModel.addRow(obj);
            }
        });
    }

    public void GUIAfterConnect() throws IOException, InterruptedException {
        clientFrame = new JFrame("Client");
        systemBoxMessage = new JTextArea(10, 20);
        String columns[] = {"STT", "Time", "Action", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JButton logBtn = new JButton("Read Log");
        JTextField filterField = new JTextField(15);
        JLabel filterLabel = new JLabel("Filter: ");
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel boxMessagePanel = new JPanel();
        JPanel contentPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel actionTablePanel = new JPanel(new BorderLayout());

        boxMessagePanel.add(systemBoxMessage);
        systemBoxMessage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        boxMessagePanel.add(new JScrollPane(systemBoxMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        boxMessagePanel.setBorder(BorderFactory.createTitledBorder("System Message"));
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);
        filterPanel.add(logBtn);
        table.setModel(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(600, table.getPreferredSize().height));
        table.setFillsViewportHeight(true);
        JScrollPane spTable = new JScrollPane(table);

        tablePanel.add(spTable, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        actionTablePanel.add(tablePanel, BorderLayout.CENTER);
        actionTablePanel.setBorder(BorderFactory.createTitledBorder("Actions Table"));
        actionTablePanel.add(filterPanel, BorderLayout.PAGE_START);

        contentPanel.add(btnPanel, BorderLayout.PAGE_START);
        contentPanel.add(boxMessagePanel, BorderLayout.LINE_START);
        contentPanel.add(actionTablePanel, BorderLayout.LINE_END);

        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTreeLog(loadFileLog(logCurrentName));
            }
        });

        filterTable(filterField, sorter);

        ArrayList<String> detailActions = new ArrayList<>();
        LocalDateTime local = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String time = local.format(format);
        detailActions.add(time);
        detailActions.add("LOG-IN");
        detailActions.add("Connected to the server");
        loadTable(detailActions);
        String log = "LOG-IN; " + time + "; Connected to the server";
        writeLog(log);

        Font font = new Font("Times New Roman", Font.BOLD, 14);
        systemBoxMessage.setFont(font);
        systemBoxMessage.setText("Connected to the server");
        systemBoxMessage.setEditable(false);

        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.add(contentPanel);
        clientFrame.pack();
        clientFrame.setLocationRelativeTo(null);
        clientFrame.setVisible(true);

        if (watcher == null) {
            watchFolder("");
        } else {
            key.pollEvents();
            watcher.close();           
            watchFolder("");
        }
    }

    public void createGUI() {
        clientFrame = new JFrame("Client");
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
