package com.zjfgh.bluedhook.simple;

import android.util.Log;

public class LogUtil {
    private static final int MAX_LOG_LENGTH = 4000; // Android Log的最大长度限制

    public static void longLog(String tag, String message) {
        if (message.length() > MAX_LOG_LENGTH) {
            logLongMessage(tag, message);
        } else {
            Log.d(tag, message);
        }
    }

    private static void logLongMessage(String tag, String longMessage) {
        int chunkCount = (int) Math.ceil((double) longMessage.length() / MAX_LOG_LENGTH);
        for (int i = 0; i < chunkCount; i++) {
            int max = Math.min(longMessage.length(), (i + 1) * MAX_LOG_LENGTH);
            String chunk = longMessage.substring(i * MAX_LOG_LENGTH, max);
            Log.d(tag + "_PART_" + (i + 1), chunk);
        }
    }

    // 用于打印JSON等结构化数据
    public static void logJson(String tag, String json) {
        try {
            if (json == null || json.isEmpty()) {
                Log.d(tag, "Empty JSON");
                return;
            }

            // 尝试格式化JSON
            String formattedJson = formatJson(json);
            longLog(tag, formattedJson);
        } catch (Exception e) {
            longLog(tag, "Invalid JSON: " + json);
        }
    }

    private static String formatJson(String json) {
        // 简单的JSON格式化
        return json.replace(",", ",\n")
                .replace("{", "{\n")
                .replace("}", "\n}")
                .replace("[", "[\n")
                .replace("]", "\n]");
    }
}
