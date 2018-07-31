package com.stb.tools;

import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args){
        String url = Main.class.getClass().getResource("/").getPath();
        System.out.println("url: "+url);
        ;
        File file = new File(Toolkit.getDefaultToolkit().getImage("src/image/miss.ico").toString());
        if(file.exists()){
            System.out.println("存在");
        }else{
            System.out.println("不存在");
        }
    }
}
