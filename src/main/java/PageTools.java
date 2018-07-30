import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

    private String t_url = "";
    private String t_port = "";
    private String t_dbName = "";
    private String t_userName = "";
    private String t_password = "";
    private String t_pan = "";

    public static void main(String[] args) {
        new PageTools().Frame();
    }

    public void Frame() {
        //创建JFrame
        JFrame frame = new JFrame("sql转实体类工具");
        frame.setSize(350, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("favicon.ico");
        frame.setIconImage(icon.getImage());
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
        choosePan.setBounds(10,170,100,25);
        page1.add(choosePan);

        final DefaultComboBoxModel chooseData = new DefaultComboBoxModel();
        File[] roots = File.listRoots();
        for(File file:roots){
            chooseData.addElement(file.getPath());
        }
        selectCombo = new JComboBox(chooseData);
        selectCombo.setEditable(true);
        selectCombo.setSize(new Dimension(80,25));
        selectCombo.setPreferredSize(new Dimension(80,25));
        selectCombo.setSelectedIndex(0);

        JScrollPane chooseList = new JScrollPane(selectCombo);
        page1.add(chooseList);

        login = new JButton("连接");
        login.setBounds(80, 200, 80, 25);
        login.setActionCommand("loginBtn");
        login.addActionListener(new ButtonClickListener());
        page1.add(login);
        start = new JButton("生成实体类");
        start.setBounds(180, 200, 120, 25);
        start.setActionCommand("startBtn");
        start.addActionListener(new ButtonClickListener());
        page1.add(start);

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
                t_pan = t_pan.substring(0,t_pan.indexOf(":"));
                if(t_url.equals("")){
                    JOptionPane.showMessageDialog(null,"IP地址不能为空","提示",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if(t_port.equals("")){
                    JOptionPane.showMessageDialog(null,"端口号不能为空","提示",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if(t_dbName.equals("")){
                    JOptionPane.showMessageDialog(null,"数据库名称不能为空","提示",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if(t_userName.equals("")){
                    JOptionPane.showMessageDialog(null,"用户名不能为空","提示",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if(t_password.equals("")){
                    JOptionPane.showMessageDialog(null,"密码不能为空","提示",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                //连接测试
                SQLToBean sqlToBean = new SQLToBean(t_url,t_port,t_dbName,t_pan,t_userName,t_password);
                boolean flag = sqlToBean.init();
                if(flag){
                    login.setText("断开连接");
                    login.setActionCommand("closeConn");

                }else{
                    JOptionPane.showMessageDialog(null,"数据库连接失败","提示",JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }else if(command.equals("closeConn")){
                new SQLToBean(t_url,t_port,t_dbName,t_pan,t_userName,t_password).closeConn();
                JOptionPane.showMessageDialog(null,"数据库连接已断开","提示",JOptionPane.INFORMATION_MESSAGE);
                login.setText("开始连接");
                login.setActionCommand("loginBtn");
                return;
            }else if(command.equals("startBtn")){
                try {
                    new SQLToBean(t_url,t_port,t_dbName,t_pan,t_userName,t_password).generate();
                }catch (Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,"生成失败","提示",JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
    }


}
