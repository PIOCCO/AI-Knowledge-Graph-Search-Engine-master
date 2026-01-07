package org.example.service;

import org.example.model.Notification;
import org.example.model.Ticket;
import org.example.repository.Neo4jConnection;
import org.example.util.SecurityUtils;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.neo4j.driver.Values.parameters;

/**
 * Real-time Notification Manager with event-driven architecture
 */
public class NotificationManager {

    private static NotificationManager instance;
    private final Driver driver;
    private final ScheduledExecutorService scheduler;
    private final List<NotificationListener> listeners;
    private final Queue<Notification> notificationQueue;

    private NotificationManager() {
        this.driver = Neo4jConnection.getInstance().getDriver();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listeners = new CopyOnWriteArrayList<>();
        this.notificationQueue = new ConcurrentLinkedQueue<>();

        startNotificationProcessor();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Add notification listener for real-time updates
     */
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove notification listener
     */
    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Create and broadcast a notification
     */
    public void createNotification(String userId, String message, NotificationType type) {
        createNotification(userId, message, type, null, null);
    }

    /**
     * Create notification with entity reference
     */
    public void createNotification(String userId, String message, NotificationType type,
                                   String entityId, String entityType) {
        Notification notification = new Notification();
        notification.setId(SecurityUtils.generateId());
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type.name());
        notification.setRelatedEntityId(entityId);
        notification.setRelatedEntityType(entityType);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setPriority(getPriority(type));

        // Save to database
        saveNotification(notification);

        // Add to queue for processing
        notificationQueue.offer(notification);

        System.out.println("✅ Notification created: " + message);
    }

    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotifications(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) " +
                    "RETURN n ORDER BY n.createdAt DESC LIMIT 50";

            Result result = session.run(query, parameters("userId", userId));
            List<Notification> notifications = new ArrayList<>();

            while (result.hasNext()) {
                notifications.add(mapToNotification(result.next().get("n").asMap()));
            }

            return notifications;
        }
    }

    /**
     * Get all notifications for user
     */
    public List<Notification> getAllNotifications(String userId, int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId}) " +
                    "RETURN n ORDER BY n.createdAt DESC LIMIT $limit";

            Result result = session.run(query, parameters("userId", userId, "limit", limit));
            List<Notification> notifications = new ArrayList<>();

            while (result.hasNext()) {
                notifications.add(mapToNotification(result.next().get("n").asMap()));
            }

            return notifications;
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {id: $id}) " +
                    "SET n.read = true, n.readAt = $readAt";

            session.run(query, parameters(
                    "id", notificationId,
                    "readAt", LocalDateTime.now().toString()
            ));

            System.out.println("✅ Notification marked as read: " + notificationId);
        }
    }

    /**
     * Mark all notifications as read for user
     */
    public void markAllAsRead(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) " +
                    "SET n.read = true, n.readAt = $readAt";

            session.run(query, parameters(
                    "userId", userId,
                    "readAt", LocalDateTime.now().toString()
            ));

            System.out.println("✅ All notifications marked as read for user: " + userId);
        }
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId) {
        try (Session session = driver.session()) {
            session.run("MATCH (n:Notification {id: $id}) DELETE n",
                    parameters("id", notificationId));
        }
    }

    /**
     * Get unread count for user
     */
    public long getUnreadCount(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) " +
                    "RETURN count(n) as count";

            Result result = session.run(query, parameters("userId", userId));
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
            return 0;
        }
    }

    /**
     * Automatic notifications for ticket events
     */
    public void notifyTicketCreated(Ticket ticket, String creatorId) {
        String message = String.format("New ticket created: %s", ticket.getTitle());
        createNotification(creatorId, message, NotificationType.TICKET_CREATED,
                ticket.getId(), "Ticket");

        // Notify assigned user if different
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().equals(creatorId)) {
            String assignMessage = String.format("Ticket assigned to you: %s", ticket.getTitle());
            createNotification(ticket.getAssignedTo(), assignMessage,
                    NotificationType.TICKET_ASSIGNED, ticket.getId(), "Ticket");
        }
    }

    public void notifyTicketAssigned(Ticket ticket, String assigneeId) {
        String message = String.format("Ticket assigned to you: %s", ticket.getTitle());
        createNotification(assigneeId, message, NotificationType.TICKET_ASSIGNED,
                ticket.getId(), "Ticket");
    }

    public void notifyTicketUpdated(Ticket ticket, String userId) {
        String message = String.format("Ticket updated: %s", ticket.getTitle());
        createNotification(userId, message, NotificationType.TICKET_UPDATED,
                ticket.getId(), "Ticket");
    }

    public void notifyTicketResolved(Ticket ticket, String creatorId) {
        String message = String.format("Ticket resolved: %s", ticket.getTitle());
        createNotification(creatorId, message, NotificationType.TICKET_RESOLVED,
                ticket.getId(), "Ticket");
    }

    public void notifySLABreach(Ticket ticket, String assigneeId) {
        String message = String.format("⚠️ SLA BREACH: Ticket %s has exceeded its SLA",
                ticket.getId());
        createNotification(assigneeId, message, NotificationType.SLA_BREACH,
                ticket.getId(), "Ticket");
    }

    public void notifySLAWarning(Ticket ticket, String assigneeId) {
        String message = String.format("⏰ SLA WARNING: Ticket %s approaching SLA deadline",
                ticket.getId());
        createNotification(assigneeId, message, NotificationType.SLA_WARNING,
                ticket.getId(), "Ticket");
    }

    public void notifyCommentAdded(String ticketId, String commentAuthor, String ticketOwner) {
        if (!commentAuthor.equals(ticketOwner)) {
            String message = String.format("New comment on your ticket: %s", ticketId);
            createNotification(ticketOwner, message, NotificationType.COMMENT_ADDED,
                    ticketId, "Ticket");
        }
    }

    // Private Methods

    private void saveNotification(Notification notification) {
        try (Session session = driver.session()) {
            String query = "CREATE (n:Notification {" +
                    "id: $id, userId: $userId, message: $message, type: $type, " +
                    "relatedEntityId: $relatedEntityId, relatedEntityType: $relatedEntityType, " +
                    "read: $read, createdAt: $createdAt, priority: $priority})";

            session.run(query, parameters(
                    "id", notification.getId(),
                    "userId", notification.getUserId(),
                    "message", notification.getMessage(),
                    "type", notification.getType(),
                    "relatedEntityId", notification.getRelatedEntityId(),
                    "relatedEntityType", notification.getRelatedEntityType(),
                    "read", notification.isRead(),
                    "createdAt", notification.getCreatedAt().toString(),
                    "priority", notification.getPriority()
            ));
        }
    }

    private void startNotificationProcessor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                while (!notificationQueue.isEmpty()) {
                    Notification notification = notificationQueue.poll();
                    if (notification != null) {
                        broadcastToListeners(notification);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing notifications: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void broadcastToListeners(Notification notification) {
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotificationReceived(notification);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private String getPriority(NotificationType type) {
        switch (type) {
            case SLA_BREACH:
            case CRITICAL_ALERT:
                return "HIGH";
            case SLA_WARNING:
            case TICKET_ASSIGNED:
                return "MEDIUM";
            default:
                return "NORMAL";
        }
    }

    private Notification mapToNotification(Map<String, Object> map) {
        Notification notification = new Notification();
        notification.setId((String) map.get("id"));
        notification.setUserId((String) map.get("userId"));
        notification.setMessage((String) map.get("message"));
        notification.setType((String) map.get("type"));
        notification.setRelatedEntityId((String) map.get("relatedEntityId"));
        notification.setRelatedEntityType((String) map.get("relatedEntityType"));
        notification.setRead(map.get("read") != null ? (Boolean) map.get("read") : false);
        notification.setPriority((String) map.get("priority"));

        if (map.get("createdAt") != null) {
            notification.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        if (map.get("readAt") != null) {
            notification.setReadAt(LocalDateTime.parse((String) map.get("readAt")));
        }

        return notification;
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    // Listener Interface
    public interface NotificationListener {
        void onNotificationReceived(Notification notification);
    }

    // Notification Types
    public enum NotificationType {
        TICKET_CREATED,
        TICKET_ASSIGNED,
        TICKET_UPDATED,
        TICKET_RESOLVED,
        TICKET_CLOSED,
        COMMENT_ADDED,
        SLA_WARNING,
        SLA_BREACH,
        CRITICAL_ALERT,
        SYSTEM_MESSAGE,
        USER_MENTION
    }
}