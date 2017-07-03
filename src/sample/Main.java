package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static PrintedStrings PrintedStrings;


    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("serverGUI.fxml"));
        primaryStage.setTitle("AAA Server Application");
        primaryStage.setScene(new Scene(root, 900, 650));
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setResizable(false);
        primaryStage.show();



    }


    public static void main(String[] args) {
        //server.janusz.pl janusz.pl
        PrintedStrings = new PrintedStrings(); //TODO mozliwosc podania secretu
        new ServerStarter(args);

        launch(args);

    }
}
