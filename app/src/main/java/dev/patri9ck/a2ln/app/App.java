package dev.patri9ck.a2ln.app;

import android.graphics.drawable.Drawable;

public class App {

    private String name;
    private String packageName;
    private Drawable icon;
    private boolean enabled;

    public App(String name, String packageName, Drawable icon, boolean enabled) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
