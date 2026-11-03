package app.data;

/** Small key/value storage for the theme settings. Database implements it; tests use a Map. */
public interface SettingsStore {
    /** Returns the saved value, or null when nothing is saved. */
    String getSetting(String key);

    /** Saves the value. A null value deletes the key. */
    void setSetting(String key, String value);
}
