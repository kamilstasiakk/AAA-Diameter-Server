package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.CubicCurve;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static java.awt.SystemColor.info;
import static java.awt.SystemColor.text;
import static sample.Main.PrintedStrings;

/**
 * Created by Bartłomiej on 04.01.2017.
 */
public class serverGUIController implements Initializable{

    @FXML ImageView leftComp;
    @FXML ImageView rightComp;
    @FXML ImageView bottomLeftComp;
    @FXML ImageView bottomRightComp;
    @FXML ImageView serverImage;

    @FXML TextArea textComp1;
    @FXML TextArea textComp2;
    @FXML TextArea textComp3;
    @FXML TextArea textComp4;

    @FXML Button startServerButton;

    @FXML CubicCurve leftCompLine;
    @FXML CubicCurve rightCompLine;
    @FXML CubicCurve bottomRightCompLine;
    @FXML CubicCurve bottomLeftCompLine;

    @FXML CubicCurve leftServerLine;
    @FXML CubicCurve rightServerLine;
    @FXML CubicCurve leftBottomServerLine;
    @FXML CubicCurve rightBottomServerLine;

    @FXML TextField secretText;
    @FXML CheckBox defaultSecretCheckBox;
    @FXML Label secretLabel;
    @FXML Label infoLabel;

    public static boolean firstConnected;
    public static boolean secondConnected;
    public static boolean thirdConnected;
    public static boolean fourthCOnnected;

    public static boolean firstInClaster;
    public static boolean secondInClaster;
    public static boolean thirdInClaster;
    public static boolean fourthInClaster;

    public static boolean firstTextClear;
    public static boolean secondTextClear;
    public static boolean thirdTextClear;
    public static boolean fourthTextClear;

    private boolean allowToStartServer;
    private int compCounter;

    int counter = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //leftComp.setImage(new Image("file:/C:/Users/Bartłomiej/IdeaProjects/ServerAAApp/desktop-computer-keyboard-mouse.p"));

        leftComp.setVisible(false);
        rightComp.setVisible(false);
        bottomLeftComp.setVisible(false);
        bottomRightComp.setVisible(false);

        leftServerLine.setVisible(false);
        rightBottomServerLine.setVisible(false);
        leftBottomServerLine.setVisible(false);
        rightServerLine.setVisible(false);

        rightCompLine.setVisible(false);
        bottomLeftCompLine.setVisible(false);
        bottomRightCompLine.setVisible(false);
        leftCompLine.setVisible(false);

        textComp1.setVisible(false);

        allowToStartServer = false;

        compCounter = 0;
    }

    public void printLogs(int n){
        synchronized (PrintedStrings.stringsToPrint){
            String text = "";
            if(!PrintedStrings.stringsToPrint.isEmpty()) {
                for (String str : PrintedStrings.stringsToPrint) {
                    text = text + str + "\n";
                }
            }


            switch (n) {
                case 1: {
                    textComp1.setText(text);
                    //firstConnected = false;
                    firstTextClear = false;
                    break;
                }
                case 2:{
                    textComp2.setText(text);
                    //secondConnected = false;
                    secondTextClear = false;
                    break;
                }
                case 3:{
                    textComp3.setText(text);
                    //thirdConnected = false;
                    thirdTextClear = false;
                    break;
                }
                case 4:{
                    textComp4.setText(text);
                    //fourthCOnnected = false;
                    fourthTextClear = false;
                    break;
                }
            }

            PrintedStrings.stringsToPrint.clear();
        }
    }


    public void startServerAction(ActionEvent event) {
        String secret = "";

        if(secretText.getText().equals("")){
            allowToStartServer = false;
            infoLabel.setText("please enter the secret");
        }
        else {
            allowToStartServer=true;
            infoLabel.setVisible(false);
            defaultSecretCheckBox.setVisible(false);
            secretLabel.setVisible(false);
            secretText.setVisible(false);
            secret = secretText.getText();
        }

        if(allowToStartServer) {
            ServerStarter.start(secret);

            startServerButton.setVisible(false);
            serverImage.setEffect(null);

            Runnable task = () -> {

                while (true) {
                    flagCoordinator();
                    if (textComp1.getText().equals("")) {
                        printLogs(1);
                        //System.out.println("cos");
                    }
                    if (textComp2.getText().equals("")) {
                        printLogs(2);
                    }
                    if (textComp3.getText().equals("")) {
                        printLogs(3);
                    }
                    if (textComp4.getText().equals("")) {
                        printLogs(4);
                    }

                    try {
                        Thread.sleep(100);
                        counter++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (counter >= 60) {
                        firstConnected = false;
                        secondConnected = false;
                        thirdConnected = false;
                        fourthCOnnected = false;
                        counter = 0;
                    }

                }
            };
            Thread t = new Thread(task);
            t.start();
            allowToStartServer = false;
        }
    }

    public void flagCoordinator(){
        if(firstConnected){
            leftComp.setVisible(true);
            leftServerLine.setVisible(true);
            textComp1.setVisible(true);
        }
        else{
            leftServerLine.setVisible(false);
        }
        if(secondConnected){
            bottomLeftComp.setVisible(true);
            leftBottomServerLine.setVisible(true);
            textComp2.setVisible(true);
        }
        else {
            leftBottomServerLine.setVisible(false);
        }
        if(thirdConnected){
            bottomRightComp.setVisible(true);
            rightBottomServerLine.setVisible(true);
            textComp3.setVisible(true);
        }
        else {
            rightBottomServerLine.setVisible(false);
        }
        if(fourthCOnnected){
            rightComp.setVisible(true);
            rightServerLine.setVisible(true);
            textComp4.setVisible(true);
        }
        else {
            rightServerLine.setVisible(false);
        }

        if(firstInClaster)
            leftCompLine.setVisible(true);
        if(secondInClaster)
            bottomLeftCompLine.setVisible(true);
        if(thirdInClaster)
            bottomRightCompLine.setVisible(true);
        if(fourthInClaster)
            rightCompLine.setVisible(true);

        if(firstTextClear)
            textComp1.clear();
        if(secondTextClear)
            textComp2.clear();
        if(thirdTextClear)
            textComp3.clear();
        if(fourthTextClear)
            textComp4.clear();


    }

    public void defaultSecretAction(){
        if(defaultSecretCheckBox.isSelected()){
            secretText.setText("czescczescczesc1");
        }
        else {
            secretText.clear();
        }
    }
}
