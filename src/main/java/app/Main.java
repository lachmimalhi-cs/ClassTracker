package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/** Loads main.fxml (the screen) and its controller (MainController). */
public class Main extends Application {

    private MainController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        // Window size: 80% of your screen, but never bigger than 900 x 620.
        // To make it smaller change 0.80 to 0.70. To make it bigger use 0.90.
        double SCALE = 0.80;
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double w = Math.min(900, screen.getWidth() * SCALE);
        double h = Math.min(620, screen.getHeight() * SCALE);

        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        controller.attach(scene);

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
        if (controller != null) controller.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}