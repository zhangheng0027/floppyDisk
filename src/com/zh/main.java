package com.zh;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by zh on 2020/4/7.
 */
public class main {

    public static void main(String[] args) throws IOException{
        Disk disk = new Disk("F:\\project\\nasm\\2020\\test\\test.img");
        byte[] bytes = Files.readAllBytes(new File("F:\\project\\nasm\\2020\\test\\test.bat").toPath());
        disk.writeDisk(bytes, 1, 0, 0);
        disk.saveDisk();
    }
}
