package com.stb.tools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

public class PageTools {
    private JPanel page1;
    private JTextField url;//地址
    private JTextField port;//端口
    private JTextField dbName;//数据库
    private JTextField userName;//用户名
    private JPasswordField password;//密码
    private JButton login;//登录
    private JButton start;//生成实体类
    private JComboBox selectCombo;
    private JProgressBar jProgressBar;
    private JLabel msgInfo;

    private String t_url = "";
    private String t_port = "";
    private String t_dbName = "";
    private String t_userName = "";
    private String t_password = "";
    private String t_pan = "";
    private SQLToBean sqlToBean;
    boolean isStop = false;

    public static void main(String[] args) {
        new PageTools().Frame();
    }

    public void Frame() {
        //创建JFrame
        JFrame frame = new JFrame("sql转实体类工具");
        frame.setSize(350, 400);
        int fWidth = 350;
        int fHeight = 400;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //窗口居中
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds((width-fWidth)/2,(height-fHeight)/2,fWidth,fHeight);
        //创建面板
        page1 = new JPanel();
        frame.add(page1);
        //调用用户定义的方法并添加组件到面板
        placeComponents(page1);
        //设置界面可见
        frame.setVisible(true);
    }

    private void placeComponents(JPanel page1) {
        page1.setLayout(null);
        //创建JLabel
        JLabel urlText = new JLabel("IP:");
        urlText.setBounds(10, 20, 80, 25);
        page1.add(urlText);

        //创建文本域用于用户输入
        url = new JTextField(20);
        url.addFocusListener(new JTextFieldHintListener("请输入数据库IP地址", url));
        url.setBounds(100, 20, 165, 25);
        page1.add(url);

        JLabel portText = new JLabel("端口:");
        portText.setBounds(10, 50, 80, 25);
        page1.add(portText);

        port = new JTextField(20);
        port.addFocusListener(new JTextFieldHintListener("请输入端口号", port));
        port.setBounds(100, 50, 165, 25);
        page1.add(port);

        JLabel dbText = new JLabel("数据库:");
        dbText.setBounds(10, 80, 80, 25);
        page1.add(dbText);

        dbName = new JTextField(20);
        dbName.addFocusListener(new JTextFieldHintListener("请输入数据库名称", dbName));
        dbName.setBounds(100, 80, 165, 25);
        page1.add(dbName);

        JLabel userText = new JLabel("用户名:");
        userText.setBounds(10, 110, 80, 25);
        page1.add(userText);

        userName = new JTextField(20);
        userName.addFocusListener(new JTextFieldHintListener("请输入用户名", userName));
        userName.setBounds(100, 110, 165, 25);
        page1.add(userName);

        JLabel passWordText = new JLabel("密码:");
        passWordText.setBounds(10, 140, 80, 25);
        page1.add(passWordText);

        password = new JPasswordField(20);
        password.setBounds(100, 140, 165, 25);
        page1.add(password);

        JLabel choosePan = new JLabel("选择存储位置:");
        choosePan.setBounds(10, 170, 100, 25);
        page1.add(choosePan);

        //final DefaultComboBoxModel chooseData = new DefaultComboBoxModel();

        selectCombo = new JComboBox();
        File[] roots = File.listRoots();
        for (File file : roots) {
            selectCombo.addItem(file.getPath());
        }
        selectCombo.setEditable(true);
        selectCombo.setSelectedIndex(0);
        selectCombo.setBounds(100, 170, 80, 25);
        //JScrollPane chooseList = new JScrollPane(selectCombo);
        page1.add(selectCombo);

        login = new JButton("连接测试");
        login.setBounds(60, 220, 200, 25);
        login.setActionCommand("loginBtn");
        login.addActionListener(new ButtonClickListener());
        page1.add(login);
        start = new JButton("生成实体类");
        start.setBounds(180, 220, 100, 25);
        start.setBackground(Color.GRAY);
        start.setActionCommand("stopStar");//stopStar
        start.addActionListener(new Change());
        page1.add(start);
        start.setVisible(false);//隐藏
        jProgressBar = new JProgressBar();
        jProgressBar.setBounds(20, 270, 290, 25);
        jProgressBar.setMaximum(100);
        jProgressBar.setMinimum(0);
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);//具体进度显示
        page1.add(jProgressBar);
        jProgressBar.setVisible(false);
        msgInfo = new JLabel("");
        msgInfo.setBounds(20, 300, 290, 25);
        page1.add(msgInfo);
    }

    class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("loginBtn")) {
                t_url = url.getText();
                t_port = port.getText();
                t_dbName = dbName.getText();
                t_userName = userName.getText();
                t_password = new String(password.getPassword());
                t_pan = selectCombo.getItemAt(selectCombo.getSelectedIndex()).toString();
                t_pan = t_pan.substring(0, t_pan.indexOf(":"));
                if (t_url.equals("")) {
                    JOptionPane.showMessageDialog(null, "IP地址不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (t_port.equals("")) {
                    JOptionPane.showMessageDialog(null, "端口号不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (t_dbName.equals("")) {
                    JOptionPane.showMessageDialog(null, "数据库名称不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (t_userName.equals("")) {
                    JOptionPane.showMessageDialog(null, "用户名不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (t_password.equals("")) {
                    JOptionPane.showMessageDialog(null, "密码不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                //连接测试
                sqlToBean = new SQLToBean(t_url, t_port, t_dbName, t_pan, t_userName, t_password);
                boolean flag = sqlToBean.init();
                if (flag) {
                    //连接成功
                    login.setText("断开连接");
                    login.setActionCommand("closeConn");
                    login.setBounds(60, 220, 100, 25);
                    start.setVisible(true);
                    start.setActionCommand("startBtn");
                    start.setBackground(null);
                    jProgressBar.setVisible(true);
                    jProgressBar.setString(null);
                    jProgressBar.setValue(0);
                    msgInfo.setText("");
                    JOptionPane.showMessageDialog(null, "数据库连接成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "数据库连接失败", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else if (command.equals("closeConn")) {
                sqlToBean.closeConn();
                jProgressBar.setVisible(false);
                start.setVisible(false);
                start.setActionCommand("stopStar");
                start.setBackground(Color.GRAY);
                login.setText("开始连接");
                login.setActionCommand("loginBtn");
                login.setBounds(60, 220, 200, 25);
                JOptionPane.showMessageDialog(null, "数据库连接已断开", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }


    }

    class Change implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("startBtn")) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            for (int i = 1; i <= 100; i++) {
                                if (i % 17 == 0) {
                                    Thread.sleep(500);
                                }
                                jProgressBar.setValue(i);
                            }
                            long startTime = System.currentTimeMillis();
                            //开始执行
                            sqlToBean.generate();
                            long endTime = System.currentTimeMillis();
                            jProgressBar.setValue(100);
                            Thread.sleep(500);
                            jProgressBar.setString("执行成功!共耗时:" + (endTime - startTime) + "ms");
                            start.setBackground(Color.GRAY);
                            start.setActionCommand("stopStar");
                            msgInfo.setText("生成文件位置  "+t_pan+":/hs_factory");
                            Runtime.getRuntime().exec("cmd /c start explorer " + t_pan + ":\\");
                            login.setText("开始连接");
                            login.setActionCommand("loginBtn");
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, "生成失败", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }).start();
            }else if (command.equals("stopStar")) {
                JOptionPane.showMessageDialog(null, "请先连接数据库", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

    }

}
