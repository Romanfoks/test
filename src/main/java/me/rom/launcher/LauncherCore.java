package me.rom.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;

public class LauncherCore {

    private static final OkHttpClient client = new OkHttpClient();

    public static void syncClient(String manifestUrl, File clientDir) throws Exception {
        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }

        System.out.println("[Core] Запрос списка файлов с сервера...");
        Request request = new Request.Builder().url(manifestUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Сервер обновлений недоступен: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonObject manifest = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray filesArray = manifest.getAsJsonArray("files");

            System.out.println("[Core] Проверка файлов клиента...");
            for (JsonElement element : filesArray) {
                JsonObject fileObj = element.getAsJsonObject();
                String relPath = fileObj.get("path").getAsString();
                String downloadUrl = fileObj.get("url").getAsString();
                String serverSha1 = fileObj.get("sha1").getAsString();

                File localFile = new File(clientDir, relPath);

                if (localFile.exists()) {
                    String localSha1 = getFileSHA1(localFile);
                    if (localSha1.equalsIgnoreCase(serverSha1)) {
                        continue; // Файл совпадает, пропускаем
                    }
                    System.out.println("[Core] Обновление файла: " + relPath);
                    localFile.delete();
                }

                System.out.println("[Core] Скачивание: " + relPath);
                localFile.getParentFile().mkdirs();
                FileUtils.copyURLToFile(new URL(downloadUrl), localFile, 15000, 15000);
            }
            System.out.println("[Core] Проверка файлов завершена.");
        }
    }

    private static String getFileSHA1(File file) {
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] mdbytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}