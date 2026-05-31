package me.rom.launcher;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MinecraftRunner {

    public static void runMinecraft(String gameDir, String assetsDir, String username, String uuid, String accessToken, List<File> allLibraries) {
        try {
            List<String> modularPaths = buildStrictModularPathList(allLibraries);
            List<String> classPaths = buildClassPathList(allLibraries, modularPaths);

            List<String> command = new ArrayList<>();
            String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            command.add(javaPath);

            // --- ДОБАВЛЕННЫЕ АРГУМЕНТЫ ДЛЯ ИСПРАВЛЕНИЯ ОШИБКИ ---
            command.add("--add-opens"); command.add("java.base/java.lang.invoke=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/java.lang=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/java.lang.reflect=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/java.io=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/java.util=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/java.nio=ALL-UNNAMED");
            command.add("--add-opens"); command.add("java.base/sun.nio.fs=ALL-UNNAMED");
            // ----------------------------------------------------

            command.add("-Dfile.encoding=UTF-8");
            command.add("-Dsun.stdout.encoding=UTF-8");
            command.add("-Dsun.stderr.encoding=UTF-8");

            File authAgent = new File(gameDir, "authlib-injector.jar");
            if (authAgent.exists()) {
                command.add("-javaagent:" + authAgent.getAbsolutePath().replace("\\", "/") + "=https://authserver.ely.by");
            }

            if (!modularPaths.isEmpty()) {
                command.add("--module-path");
                command.add(String.join(File.pathSeparator, modularPaths).replace("\\", "/"));
            }
            // После добавления --module-path и пути к библиотекам, добавь это:
            command.add("--add-modules");
            command.add("ALL-MODULE-PATH");

// И чтобы точно избежать конфликтов с внутренними API:
            command.add("--add-reads");
            command.add("net.neoforged.accesstransformer.modlauncher=ALL-UNNAMED");

            command.add("-classpath");
            command.add(String.join(File.pathSeparator, classPaths).replace("\\", "/"));

            command.add("me.rom.launcher.Main"); // или твой основной класс игры

            // ... далее код запуска через ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(gameDir));
            pb.inheritIO();
            pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> buildStrictModularPathList(List<File> allLibraries) {
        List<String> modularPaths = new ArrayList<>();
        String[] strictLibs = {
                "securejarhandler", "modlauncher", "bootstraplauncher", "accesstransformers",
                "loader", "earlydisplay", "bus", "coremods", "typetools", "sponge-mixin",
                "core-3.8.3", "toml-3.8.3", "srgutils", "antlr4-runtime", "jopt-simple",
                "guava", "commons-lang3", "nashorn-core", "slf4j-api", "log4j-api", "log4j-core",
                "asm"
        };



        for (File file : allLibraries) {
            if (!file.exists() || file.isDirectory()) continue;
            String fileName = file.getName().toLowerCase();
            if (fileName.contains("guava-20.0")) continue;

            for (String lib : strictLibs) {
                if (fileName.contains(lib.toLowerCase())) {
                    String absolutePath = file.getAbsolutePath();
                    if (!modularPaths.contains(absolutePath)) {
                        modularPaths.add(absolutePath);
                    }
                    break;
                }
            }
        }
        return modularPaths;
    }


    private static List<String> buildClassPathList(List<File> allLibraries, List<String> modularPaths) {
        List<String> classPaths = new ArrayList<>();
        for (File file : allLibraries) {
            if (!file.exists() || file.isDirectory()) continue;
            String absolutePath = file.getAbsolutePath();
            if (!modularPaths.contains(absolutePath) && !classPaths.contains(absolutePath)) {
                classPaths.add(absolutePath);
            }
        }
        return classPaths;
    }
}