package app;

import app.state.AppState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/** Loads fxml/main.fxml (the whole screen). All layout is in the FXML files; this class only opens the window. */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        // Window size: 80% of your screen, but never bigger than 900 x 620.
        double scale = 0.80;
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double w = Math.min(900, screen.getWidth() * scale);
        double h = Math.min(620, screen.getHeight() * scale);

        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("ClassTrack - Student Attendance");
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(460);
        stage.setX(screen.getMinX() + (screen.getWidth() - w) / 2);
        stage.setY(screen.getMinY() + (screen.getHeight() - h) / 2);
        stage.show();
    }

    @Override
    public void stop() {
        AppState.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
