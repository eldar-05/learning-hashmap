package com.example.todo;

public class Task {
    private String title;
    private String description;
    private boolean isCompleted;

    public Task(String title, String description, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }

    @Override
    public String toString() {
        return String.format("%s\n    Описание: %s", title, description);
    }
}