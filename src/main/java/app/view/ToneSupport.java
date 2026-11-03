package app.view;

import javafx.scene.Node;

/** Keeps exactly one "tone-xxx" style class on a node. The colours of each tone are in style.css. */
final class ToneSupport {

    private ToneSupport() { }

    static void change(Node node, String oldTone, String newTone) {
        if (oldTone != null) {
            node.getStyleClass().remove("tone-" + oldTone);
        }
        if (newTone != null && !newTone.isBlank()) {
            node.getStyleClass().add("tone-" + newTone);
        }
    }
}
