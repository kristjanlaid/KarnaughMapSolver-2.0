package project.karnaughmapsolver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), screenWidth, screenHeight);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Karnaugh Map Solver");
        primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("/images/Kmap_logo.png")));
        primaryStage.setScene(scene);
        primaryStage.maxWidthProperty().bind(primaryStage.widthProperty());
        primaryStage.minWidthProperty().bind(primaryStage.widthProperty());
        primaryStage.setMinHeight(670);
        primaryStage.show();
    }
}
