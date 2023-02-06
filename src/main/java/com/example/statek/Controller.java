package com.example.statek;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    public TextField shipCapacityN;
    @FXML
    public TextField BridgeCapacityK;
    @FXML
    public Button SaveButton;
    public TextField voyageLenght;
    public TextField departureInterval;
    public TextField passengerMinTime;
    public TextField passengerMaxTime;
    public TextField buyTicketTime;
    public TextField checkTicketTime;
    public TextField passingTime;
    public Button StopButton;
    public Button PauseButton;
    public Button StartButton;
    public Slider passengerSpeedSlider;
    public Slider captainSpeedSlider;

    public void initialize(){

    }
    @FXML
    protected void onStartButtonAction(){
        if (Main.config != null){
            Main.animationStatus = Animation.Status.RUNNING;
            Main.config.startThreads();
            StartButton.setDisable(true);
            PauseButton.setDisable(false);
            PauseButton.setText("Pause");
            StopButton.setDisable(false);
            passengerSpeedSlider.setDisable(false);
            captainSpeedSlider.setDisable(false);
        }
    }

    @FXML
    protected void onStopButtonAction(){
        if (Main.animationStatus == Animation.Status.RUNNING){
            Main.animationStatus = Animation.Status.STOPPED;
            Main.config.interruptThreads();
            Main.animationPane.getChildren().clear();
            SaveButton.setDisable(false);
            StartButton.setDisable(true);
            PauseButton.setDisable(true);
            PauseButton.setText("Pause/Replay");
            StopButton.setDisable(true);
            passengerSpeedSlider.setDisable(true);
            captainSpeedSlider.setDisable(true);
        }
    }

    @FXML
    protected void onPauseResumeButtonAction() throws InterruptedException {
        if (Main.config != null) {
            if (Main.animationStatus == Animation.Status.PAUSED) {
                Main.animationStatus = Animation.Status.RUNNING;
                Main.config.resumeAnimation();
                PauseButton.setText("Pause");
                StartButton.setDisable(true);
                StopButton.setDisable(false);
                passengerSpeedSlider.setDisable(false);
                captainSpeedSlider.setDisable(false);
            } else if (Main.animationStatus == Animation.Status.RUNNING) {
                Main.animationStatus = Animation.Status.PAUSED;
                Main.config.pauseAnimation();
                SaveButton.setDisable(true);
                StartButton.setDisable(true);
                PauseButton.setText("Replay");
                StopButton.setDisable(true);
                passengerSpeedSlider.setDisable(true);
                captainSpeedSlider.setDisable(true);
            }
        }
    }

    public void onPassengerSliderChange(){
        Main.config.passengerAnimationSpeedChange(passengerSpeedSlider.getValue());
    }
    public void onCaptainSliderChange(){
        Main.config.captainAnimationSpeedChange(captainSpeedSlider.getValue());
    }

    @FXML
    protected void onSaveButtonClick() {
        try{
            int screenWidth = (int) Main.animationPane.getWidth();
            int screenHeight = (int) Main.animationPane.getHeight();

            int shipCapacityN = 0;
            int bridgeCapacityK = 0;

            try{
                shipCapacityN = Integer.parseInt(this.shipCapacityN.getText());
            }catch (NumberFormatException e){
                this.shipCapacityN.setText("Error");
                throw new DataErrorException();
            }
            try{
                bridgeCapacityK = Integer.parseInt(BridgeCapacityK.getText());
            }catch (NumberFormatException e){
                BridgeCapacityK.setText("Error");
                throw new DataErrorException();
            }
            if (bridgeCapacityK > shipCapacityN){
                BridgeCapacityK.setText("Bridge > Ship");
                throw new DataErrorException();
            }
            Main.config = new Configuration(screenWidth, screenHeight, shipCapacityN, bridgeCapacityK);

            try {
                Main.config.checkTicketTime = Integer.parseInt(checkTicketTime.getText());
            } catch (NumberFormatException e) {
                checkTicketTime.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.buyTicketTime = Integer.parseInt(buyTicketTime.getText());
            } catch (NumberFormatException e) {
                buyTicketTime.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.passengerMaxTime = Integer.parseInt(passengerMaxTime.getText());
            } catch (NumberFormatException e) {
                passengerMaxTime.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.passengerMinTime = Integer.parseInt(passengerMinTime.getText());
            } catch (NumberFormatException e) {
                passengerMinTime.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.departureInterval = Integer.parseInt(departureInterval.getText());
            } catch (NumberFormatException e) {
                departureInterval.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.voyageLength = Integer.parseInt(voyageLenght.getText());
            } catch (NumberFormatException e) {
                voyageLenght.setText("Error");
                throw new DataErrorException();
            }
            try {
                Main.config.passingTime = Integer.parseInt(passingTime.getText());
            } catch (NumberFormatException e) {
                passingTime.setText("Error");
                throw new DataErrorException();
            }

            Main.config.prepareAnimation();
            SaveButton.setDisable(true);
            StartButton.setDisable(false);
            PauseButton.setDisable(true);
            StopButton.setDisable(true);
            passengerSpeedSlider.setDisable(false);
            captainSpeedSlider.setDisable(false);
            passengerSpeedSlider.setValue(0);
            captainSpeedSlider.setValue(0);
        }catch (DataErrorException e1){

        }

    }
}