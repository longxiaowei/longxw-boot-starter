package com.longxw.boot.starter.updater.tool;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOTool {

    public static Charset defalutCharset = Charset.forName("UTF-8");

    public static List<String> readLines(BufferedReader reader) throws IOException{
        List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    public static String readToString(InputStream is,Charset charset) throws IOException{
        return new String(readBytes(is),charset);
    }

    public static String readToString(InputStream is) throws IOException{
        return readToString(is,defalutCharset);
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        int remaining = new Long(is.available()).intValue();
        if(remaining == 0){
            return new byte[0];
        }
        int offset = 0;
        byte[] result = new byte[remaining];
        while (remaining > 0){
            int read = is.read(result,offset,remaining);
            if(read < 0 )
                break;
            remaining -= read;
            offset += read;
        }
        return Arrays.copyOf(result, offset);
    }

}
