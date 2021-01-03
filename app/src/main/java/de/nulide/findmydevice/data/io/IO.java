package de.nulide.findmydevice.data.io;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;

public class IO {

    public static final String whiteListFileName = "whitelist.json";
    public static final String settingsFileName = "settings-001.json";
    public static Context context;

    public static <T> void write(T obj, String fileName){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(context.getFilesDir(), fileName);
        try {
            String json = mapper.writeValueAsString(obj);
            PrintWriter out = new PrintWriter(file);
            out.write(json);
            out.close();
        } catch (FileNotFoundException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static <T> T read(Class<T> type, String fileName){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(context.getFilesDir(), fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            try {
                String line;

                while ((line = br.readLine()) != null) {
                    json.append(line);
                    json.append('\n');
                }
                br.close();
                T obj = mapper.readValue(json.toString(), type);
                return obj;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            return type.newInstance();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}