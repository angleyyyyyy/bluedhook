package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileStorageHelper {

    // 保存文件到内部存储
    public static boolean saveFileToInternalStorage(Context context, String fileName, String data) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 保存二进制数据（如图片）
    public static boolean saveBinaryFileToInternalStorage(Context context, String fileName, byte[] data) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String readFileFromInternalStorage(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(fileName);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("BluedHook", e.toString());
        }
        return stringBuilder.toString();
    }
}
