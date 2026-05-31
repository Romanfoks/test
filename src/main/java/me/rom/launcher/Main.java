package me.rom.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Ядро Лаунчера Запущено ===");

        // 1. Пути к рабочим папкам клиента (подстроено под твою систему)
        String appData = System.getenv("APPDATA");
        String clientDir = appData + File.separator + ".my_server_client";
        String librariesDir = clientDir + File.separator + "libraries";
        String assetsDir = clientDir + File.separator + "assets";

        System.out.println("[Launcher] Рабочая директория: " + clientDir);

        // 2. Сканируем папку libraries и собираем абсолютно все JAR-ники
        File libsFolder = new File(librariesDir);
        List<File> allLibraries = new ArrayList<>();

        if (libsFolder.exists() && libsFolder.isDirectory()) {
            scanLibraries(libsFolder, allLibraries);
            System.out.println("[Launcher] Найдено библиотек на диске: " + allLibraries.size());
        } else {
            System.err.println("[Launcher] [Критическая ошибка] Папка libraries не найдена по пути: " + librariesDir);
            return;
        }

        // Включаем дебаг-вывод всех найденных файлов, как у тебя в логе
        System.out.println("=== НАЧАЛО СПИСКА ВСЕХ JAR В LIBRARIES ===");
        for (File jar : allLibraries) {
            System.out.println("[Файл на Диске]: " + jar.getAbsolutePath());
        }
        System.out.println("=== КОНЕЦ СПИСКА ВСЕХ JAR В LIBRARIES ===");

        // 3. Данные игрока для авторизации (можешь менять на ходу)
        String username = "Rom";
        String uuid = "00000000-0000-0000-0000-000000000000"; // Дефолт для Ely.by / Пиратки
        String accessToken = "token12345";

        // 4. Передаем управление в MinecraftRunner для сборки путей и старта игры
        System.out.println("[Launcher] Передача управления MinecraftRunner...");
        MinecraftRunner.runMinecraft(clientDir, assetsDir, username, uuid, accessToken, allLibraries);
    }

    /**
     * Рекурсивно обходит все подпапки в libraries и собирает файлы с расширением .jar
     */
    private static void scanLibraries(File folder, List<File> jarList) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Если это папка, заходим внутрь
                scanLibraries(file, jarList);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                // Если это jar-ник, добавляем в наш список
                jarList.add(file);
            }
        }
    }
}