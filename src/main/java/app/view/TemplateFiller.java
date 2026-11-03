package app.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Loads one FXML template per item and puts the results into a pane. Used by the three repeaters. */
final class TemplateFiller {

    private TemplateFiller() { }

    @SuppressWarnings("unchecked")
    static void fill(Pane pane, List<?> items, String template) {
        if (template == null || template.isBlank() || items == null) {
            pane.getChildren().clear();
            return;
        }
        URL url = TemplateFiller.class.getResource(template);
        if (url == null) {
            throw new IllegalStateException("Template not found: " + template);
        }
        List<Node> nodes = new ArrayList<>(items.size());
        for (Object item : items) {
            FXMLLoader loader = new FXMLLoader(url);
            try {
                Node node = loader.load();
                Object controller = loader.getController();
                if (controller instanceof ItemView<?> view) {
                    ((ItemView<Object>) view).setItem(item);
                }
                nodes.add(node);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not load template " + template, e);
            }
        }
        pane.getChildren().setAll(nodes);
    }
}
