package dev.patri9ck.a2ln.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ParsedNotification {

    private String title;
    private String text;
    private byte[] icon;

    private ParsedNotification(String title, String text, byte[] icon) {
        this.title = title;
        this.text = text;
        this.icon = icon;
    }

    public static ParsedNotification makeTestNotification() {
        return new ParsedNotification("Test", "This is a test notification.", null);
    }

    public static ParsedNotification parseNotification(Notification notification, Context context) {
        Object title = notification.extras.get(Notification.EXTRA_TITLE);
        Object text = notification.extras.get(Notification.EXTRA_TEXT);

        if (title == null || text == null) {
            return null;
        }

        Icon largeIcon = notification.getLargeIcon();

        if (largeIcon != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                Drawable drawable = largeIcon.loadDrawable(context);

                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                    return new ParsedNotification(title.toString(), text.toString(), byteArrayOutputStream.toByteArray());
                }
            } catch (IOException exception) {
                Log.e("A2LN", "Failed to convert picture to bytes", exception);
            }
        }

        return new ParsedNotification(title.toString(), text.toString(), null);
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public byte[] getIcon() {
        return icon;
    }
}
