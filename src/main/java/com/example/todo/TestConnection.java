package com.example.todo;
import java.sql.Connection;
import java.sql.DriverManager;
public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "eldarzh123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Успешно подключились!");
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }
}