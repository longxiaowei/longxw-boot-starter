package com.longxw.boot.starter.updater.tool;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class FileTool {

    private static Charset defaultCharset = Charset.forName("UTF-8");

    public static String readText(File file) throws IOException{
        try(FileInputStream fileInputStream = openFileInputStream(file)){
            return IOTool.readToString(fileInputStream);
        }
    }

    public static byte[] readBytes(File file) throws IOException{
        try (FileInputStream fileInputStream = openFileInputStream(file)){
            return IOTool.readBytes(fileInputStream);
        }
    }

    public static List<String> readLines(File file)throws IOException {
        try (FileInputStream fileInputStream = openFileInputStream(file);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream,defaultCharset))
        ){
            return IOTool.readLines(bufferedReader);
        }
    }

    public static FileInputStream openFileInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

}
