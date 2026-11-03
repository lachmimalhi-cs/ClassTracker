package app.state;

import app.data.SettingsStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Light/dark mode and the four user colours. It has no JavaFX code: it only produces one string of
 * CSS variables (for example "-acc:#0F766E;-bg:#F4F6F8;...") that main.fxml puts on the root node.
 * The settings are saved through a SettingsStore, so the class can be tested without a database.
 */
public class ThemeService {

    public static final String[] KEYS = {"acc", "ok", "amb", "red"};
    private static final String[] LIGHT = {"#0F766E", "#0F766E", "#92400E", "#B91C1C"};
    private static final String[] NIGHT = {"#4FD1C5", "#4FD1C5", "#FCD34D", "#FCA5A5"};

    private final SettingsStore store;
    private final Map<String, String> custom = new HashMap<>();
    private boolean dark;

    public ThemeService(SettingsStore store) {
        if (store == null) {
            throw new IllegalArgumentException("The settings store must not be null.");
        }
        this.store = store;
        this.dark = "1".equals(store.getSetting("dark"));
        for (String k : KEYS) {
            String v = store.getSetting("c_" + k);
            if (isHex(v)) {
                custom.put(k, v.toUpperCase());
            }
        }
    }

    public boolean isDark() {
        return dark;
    }

    public void toggleDark() {
        dark = !dark;
        store.setSetting("dark", dark ? "1" : "0");
    }

    /** The colour for acc, ok, amb or red: the user's choice or the default of the current mode. */
    public String colour(String key) {
        int i = indexOf(key);
        return custom.getOrDefault(key, dark ? NIGHT[i] : LIGHT[i]);
    }

    /** Saves a colour such as "#2563EB". Throws IllegalArgumentException for a bad key or a bad hex. */
    public void setColour(String key, String hex) {
        indexOf(key);
        if (!isHex(hex)) {
            throw new IllegalArgumentException("Colour must look like #RRGGBB but was: " + hex);
        }
        custom.put(key, hex.toUpperCase());
        save();
    }

    /** A preset changes the main colour and the Present colour together. */
    public void applyPreset(String hex) {
        setColour("acc", hex);
        setColour("ok", hex);
    }

    public void reset() {
        custom.clear();
        save();
    }

    /** All CSS variables of the current theme as one string. */
    public String style() {
        String card = dark ? "#18222A" : "#FFFFFF";
        StringBuilder sb = new StringBuilder();
        for (String k : KEYS) {
            String c = colour(k);
            sb.append('-').append(k).append(':').append(c).append(';');
            if (!k.equals("acc")) {
                sb.append('-').append(k).append("-bg:").append(mix(c, card, 0.84)).append(';');
            }
        }
        sb.append(dark ? "-bg:#10171D;-card:#18222A;-ink:#E8EEF2;-mute:#A3B1BD;-line:#2A3742;-on-acc:#10171D;"
                       : "-bg:#F4F6F8;-card:#FFFFFF;-ink:#1B2430;-mute:#4A5766;-line:#DDE3E8;-on-acc:#FFFFFF;");
        return sb.toString();
    }

    /** Mixes two #RRGGBB colours: result = a * (1 - t) + b * t. */
    public static String mix(String a, String b, double t) {
        StringBuilder sb = new StringBuilder("#");
        for (int i = 1; i <= 5; i += 2) {
            int x = Integer.parseInt(a.substring(i, i + 2), 16);
            int y = Integer.parseInt(b.substring(i, i + 2), 16);
            sb.append(String.format("%02X", (int) Math.round(x * (1 - t) + y * t)));
        }
        return sb.toString();
    }

    static boolean isHex(String s) {
        return s != null && s.matches("#[0-9A-Fa-f]{6}");
    }

    private void save() {
        for (String k : KEYS) {
            store.setSetting("c_" + k, custom.get(k));
        }
    }

    private static int indexOf(String key) {
        for (int i = 0; i < KEYS.length; i++) {
            if (KEYS[i].equals(key)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown colour key: " + key);
    }
}
