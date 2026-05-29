package me.rom.launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Проект лаунчера успешно запущен!");

        // Тестовые данные для оффлайн-запуска (пока без авторизации на сайте)
        String testUser = "Rom_Dev";
        String dummyUuid = "00000000-0000-0000-0000-000000000000";
        String dummyToken = "00000000000000000000000000000000";

        // Вызываем запуск игры!
        MinecraftRunner.runMinecraft(testUser, dummyUuid, dummyToken);
    }
}