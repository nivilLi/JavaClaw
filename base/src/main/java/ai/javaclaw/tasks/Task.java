package ai.javaclaw.tasks;

import java.time.Instant;

public class Task {

    public enum Status {
        todo, in_progress, completed, awaiting_human_input
    }

    private final String id;
    private final String name;
    private final Instant createdAt;
    private final Status status;
    private final String description;
    private final String channelName;

    public Task(String id, String name, Instant createdAt, Status status, String description) {
        this(id, name, createdAt, status, description, null);
    }

    public Task(String id, String name, Instant createdAt, Status status, String description, String channelName) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = status;
        this.description = description;
        this.channelName = channelName;
    }

    public static Task newTask(String name, String description) {
        return new Task(null, name, Instant.now(), Status.todo, description, null);
    }

    public static Task newTask(String name, String description, String channelName) {
        return new Task(null, name, Instant.now(), Status.todo, description, channelName);
    }

    public static Task newTask(String name, Instant createdAt, String description) {
        return new Task(null, name, createdAt, Status.todo, description, null);
    }

    public static Task newTask(String name, Instant createdAt, String description, String channelName) {
        return new Task(null, name, createdAt, Status.todo, description, channelName);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelName() {
        return channelName;
    }

    public Task withStatus(Status newStatus) {
        return new Task(id, name, createdAt, newStatus, description, channelName);
    }

    public Task withFeedback(String feedback) {
        return new Task(id, name, createdAt, status, description + "\n\nAgent feedback: " + feedback, channelName);
    }

    public Task withChannelName(String newChannelName) {
        return new Task(id, name, createdAt, status, description, newChannelName);
    }

    @Override
    public String toString() {
        return "Task '" + name + "'";
    }
}
