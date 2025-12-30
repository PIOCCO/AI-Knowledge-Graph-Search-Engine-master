package org.example.service;

import org.example.model.Notification;
import org.example.repository.Neo4jConnection;
import org.example.util.SecurityUtils;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class NotificationService {
    private final Driver driver;

    public NotificationService() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Notification createNotification(String userId, String message, String type) {
        Notification notification = new Notification();
        notification.setId(SecurityUtils.generateId());
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return saveNotification(notification);
    }

    public Notification createNotificationWithAction(String userId, String message, String type,
            String relatedEntityId, String relatedEntityType, String actionUrl) {
        Notification notification = createNotification(userId, message, type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setActionUrl(actionUrl);

        return saveNotification(notification);
    }

    private Notification saveNotification(Notification notification) {
        try (Session session = driver.session()) {
            String query = "CREATE (n:Notification {id: $id, userId: $userId, message: $message, type: $type, " +
                    "relatedEntityId: $relatedEntityId, relatedEntityType: $relatedEntityType, read: $read, " +
                    "createdAt: $createdAt, priority: $priority, actionUrl: $actionUrl}) RETURN n";

            session.run(query, parameters(
                    "id", notification.getId(),
                    "userId", notification.getUserId(),
                    "message", notification.getMessage(),
                    "type", notification.getType(),
                    "relatedEntityId", notification.getRelatedEntityId(),
                    "relatedEntityType", notification.getRelatedEntityType(),
                    "read", notification.isRead(),
                    "createdAt", notification.getCreatedAt().toString(),
                    "priority", notification.getPriority(),
                    "actionUrl", notification.getActionUrl()));
            return notification;
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId}) RETURN n ORDER BY n.createdAt DESC";
            Result result = session.run(query, parameters("userId", userId));

            List<Notification> notifications = new ArrayList<>();
            while (result.hasNext()) {
                notifications.add(mapToNotification(result.next().get("n").asMap()));
            }
            return notifications;
        }
    }

    public List<Notification> getUnreadNotifications(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) RETURN n ORDER BY n.createdAt DESC";
            Result result = session.run(query, parameters("userId", userId));

            List<Notification> notifications = new ArrayList<>();
            while (result.hasNext()) {
                notifications.add(mapToNotification(result.next().get("n").asMap()));
            }
            return notifications;
        }
    }

    public void markAsRead(String notificationId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {id: $id}) SET n.read = true, n.readAt = $readAt";
            session.run(query, parameters("id", notificationId, "readAt", LocalDateTime.now().toString()));
        }
    }

    public void markAllAsRead(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) SET n.read = true, n.readAt = $readAt";
            session.run(query, parameters("userId", userId, "readAt", LocalDateTime.now().toString()));
        }
    }

    public long getUnreadCount(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId, read: false}) RETURN count(n) as count";
            Result result = session.run(query, parameters("userId", userId));
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
            return 0;
        }
    }

    public void deleteNotification(String notificationId) {
        try (Session session = driver.session()) {
            session.run("MATCH (n:Notification {id: $id}) DELETE n", parameters("id", notificationId));
        }
    }

    public void deleteOldNotifications(String userId, int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        try (Session session = driver.session()) {
            String query = "MATCH (n:Notification {userId: $userId}) WHERE n.createdAt < $cutoffDate DELETE n";
            session.run(query, parameters("userId", userId, "cutoffDate", cutoffDate.toString()));
        }
    }

    public void clearAll(String userId) {
        try (Session session = driver.session()) {
            session.run("MATCH (n:Notification {userId: $userId}) DELETE n", parameters("userId", userId));
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
        notification.setActionUrl((String) map.get("actionUrl"));
        if (map.get("createdAt") != null) {
            notification.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        if (map.get("readAt") != null) {
            notification.setReadAt(LocalDateTime.parse((String) map.get("readAt")));
        }
        return notification;
    }
}
