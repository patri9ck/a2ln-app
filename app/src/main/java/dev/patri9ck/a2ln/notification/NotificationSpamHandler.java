package dev.patri9ck.a2ln.notification;

import java.util.HashMap;
import java.util.Map;

public class NotificationSpamHandler {

    private static final long BLOCK_SECONDS = 5;

    private final Map<String, Long> parsedNotifications = new HashMap<>();

    public void cleanUp() {
        parsedNotifications.keySet().removeIf(parsedNotification -> !isSpammed(parsedNotification));
    }

    public void addParsedNotification(ParsedNotification parsedNotification) {
        parsedNotifications.put(getKey(parsedNotification), System.currentTimeMillis() + BLOCK_SECONDS * 1000);
    }

    public boolean isSpammed(ParsedNotification parsedNotification) {
        return isSpammed(getKey(parsedNotification));
    }

    private boolean isSpammed(String key) {
        return parsedNotifications.containsKey(key) && parsedNotifications.get(key) > System.currentTimeMillis();
    }

    private String getKey(ParsedNotification parsedNotification) {
        return parsedNotification.getTitle() + parsedNotification.getText();
    }
}
