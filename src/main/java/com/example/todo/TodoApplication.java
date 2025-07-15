package com.example.todo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TodoApplication {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/todo_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "eldarzh123";

    // кэш
    private static final Map<Integer, Task> taskCache = new HashMap<>();

    public static void main(String[] args) {
        loadTasksFromDatabase();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n===== Менеджер Задач (HashMap + PostgreSQL) =====");
                System.out.println("1. Создать задачу");
                System.out.println("2. Посмотреть все задачи");
                System.out.println("3. Отметить задачу как выполненную");
                System.out.println("4. Удалить задачу");
                System.out.println("5. Выход");
                System.out.print("Выберите действие: ");

                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> createTask(scanner);
                    case "2" -> viewTasks();
                    case "3" -> updateTaskStatus(scanner);
                    case "4" -> deleteTask(scanner);
                    case "5" -> {
                        System.out.println("Выход из программы.");
                        return;
                    }
                    default -> System.out.println("Неверный ввод.");
                }
            }
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    private static void loadTasksFromDatabase() {
        String sql = "SELECT id, title, description, is_completed FROM tasks";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                boolean isCompleted = rs.getBoolean("is_completed");

                taskCache.put(id, new Task(title, description, isCompleted));
            }
            System.out.println("Задачи успешно загружены из базы данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки задач из БД: " + e.getMessage());
        }
    }

    private static void createTask(Scanner scanner) {
        System.out.print("Введите название задачи: ");
        String title = scanner.nextLine();
        System.out.print("Введите описание задачи: ");
        String description = scanner.nextLine();
    
        String sql = "INSERT INTO tasks (title, description) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int newId = rs.getInt(1);
                taskCache.put(newId, new Task(title, description, false));
                System.out.println("Задача с ID " + newId + " успешно создана и сохранена в БД.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения задачи в БД: " + e.getMessage());
        }
    }

    private static void viewTasks() {
        System.out.println("\n--- Список задач (из кэша) ---");
        if (taskCache.isEmpty()) {
            System.out.println("Список задач пуст.");
        } else {
            taskCache.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Task task = entry.getValue();
                    String status = task.isCompleted() ? " Compleated" : "Not yet";
                    System.out.printf("[%d] %s - %s\n", entry.getKey(), task.toString(), status);
                });
        }
        System.out.println("--------------------------------\n");
    }

    private static void updateTaskStatus(Scanner scanner) {
        System.out.print("Введите ID задачи для отметки о выполнении: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID.");
            return;
        }

        if (!taskCache.containsKey(id)) {
            System.out.println("Задача с таким ID не найдена.");
            return;
        }

        String sql = "UPDATE tasks SET is_completed = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                taskCache.get(id).setCompleted(true);
                System.out.println("Статус задачи с ID " + id + " обновлен.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка обновления статуса в БД: " + e.getMessage());
        }
    }

    private static void deleteTask(Scanner scanner) {
        System.out.print("Введите ID задачи для удаления: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID.");
            return;
        }

        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                taskCache.remove(id);
                System.out.println("Задача с ID " + id + " удалена.");
            } else {
                System.out.println("Задача с таким ID не найдена.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка удаления задачи из БД: " + e.getMessage());
        }
    }
}