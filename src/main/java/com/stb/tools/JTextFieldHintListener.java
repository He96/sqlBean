package com.stb.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 文本框提示焦点事件
 */
public class JTextFieldHintListener implements FocusListener {
    private String mHintText;
    private JTextField mTextField;

    public JTextFieldHintListener(String mHintText, JTextField mTextField) {
        this.mHintText = mHintText;
        this.mTextField = mTextField;
        mTextField.setForeground(Color.GRAY);
        mTextField.setForeground(Color.GRAY);
        mTextField.setText(mHintText);
    }

    public void focusGained(FocusEvent e) {
        String temp = mTextField.getText();
        if(temp.equals(mHintText)){
            mTextField.setText("");
            mTextField.setForeground(Color.BLACK);
        }
    }

    public void focusLost(FocusEvent e) {
        String temp = mTextField.getText();
        if(temp.equals("")){
            mTextField.setForeground(Color.GRAY);
            mTextField.setText(mHintText);
        }
    }
}
