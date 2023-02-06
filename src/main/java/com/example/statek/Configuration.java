package com.example.statek;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.lang.Math;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Configuration {

    public Configuration(int screenWidth, int screenHeight, int shipCapacity, int bridgeCapacity) {
        this.bridgeCapacity = bridgeCapacity;
        this.shipCapacity = shipCapacity;

        queueSeatSize = 45;
        bridgeSeatSize = Math.round(750/ this.bridgeCapacity);
        shipSeatSize = Math.round(700/ this.shipCapacity);
        if(this.shipCapacity <= 15) {
            manSize = 20;
        }else{
            manSize = (shipSeatSize / 2) - 2;
        }

        //arrangement of rectangles and positions for circles for queue
        int free_space = screenHeight - 690;
        int yref = free_space/2;
        queueSquareCoords = new XYCoord[100];
        queueCircleCoords = new XYCoord[100];
        for (int i = 0; i < queueSquareCoords.length; i++){
            queueSquareCoords[i] = new XYCoord();
            queueSquareCoords[i].x = queueSeatXref;
            queueSquareCoords[i].y = yref + i * queueSeatSize;
            queueCircleCoords[i] = new XYCoord();
            queueCircleCoords[i].x = queueSquareCoords[i].x + queueSeatSize /2;
            queueCircleCoords[i].y = queueSquareCoords[i].y + queueSeatSize /2;
        }

        //arrangement of rectangles and positions for circles for bridge
        free_space = screenWidth - 795;
        int xref = free_space/2;
        bridgeSquareCoords = new XYCoord[this.bridgeCapacity];
        bridgeCircleCoords = new XYCoord[this.bridgeCapacity];
        for (int i = 0; i< bridgeSquareCoords.length; i++){
            bridgeSquareCoords[i] = new XYCoord();
            bridgeSquareCoords[i].x = xref + i * bridgeSeatSize;
            bridgeSquareCoords[i].y = bridgeSeatYref;
            bridgeCircleCoords[i] = new XYCoord();
            bridgeCircleCoords[i].x = bridgeSquareCoords[i].x + bridgeSeatSize /2;
            bridgeCircleCoords[i].y = bridgeSquareCoords[i].y + 25;
        }

        //arrangement of rectangles and positions for circles for ship
        free_space = screenHeight - 685;
        yref = free_space/2;
        shipSquareCoords = new XYCoord[this.shipCapacity];
        shipCircleCoords = new XYCoord[this.shipCapacity];
        for (int i = 0; i < shipSquareCoords.length; i++){
            shipSquareCoords[i] = new XYCoord();
            shipSquareCoords[i].x = shipSeatXref;
            shipSquareCoords[i].y = yref + i * (shipSeatSize + space);
            shipCircleCoords[i] = new XYCoord();
            shipCircleCoords[i].x = shipSquareCoords[i].x + 23;
            shipCircleCoords[i].y = shipSquareCoords[i].y + shipSeatSize /2;
        }


        captainBridgeCoords = new XYCoord[1];
        captainBridgeCoords[0] = new XYCoord();
        captainBridgeCoords[0].x = 685;
        captainBridgeCoords[0].y = 92;

    }

    public XYCoord[] captainBridgeCoords;

    public XYCoord[] bridgeSquareCoords;
    public XYCoord[] shipSquareCoords;
    public XYCoord[] queueSquareCoords;
    public XYCoord[] bridgeCircleCoords;
    public XYCoord[] shipCircleCoords;
    public XYCoord[] queueCircleCoords;
    public int shipSeatSize;
    public int bridgeSeatSize;
    public int queueSeatSize;
    public int manSize;
    public int space = 1;
    public int shipSeatXref = 710;
    public int bridgeSeatYref = 5;
    public int queueSeatXref = 8;

    public int shipCapacity;
    public Ship ship;

    public int bridgeCapacity;
    public int departureInterval;
    public int passengerMinTime;
    public int passengerMaxTime;
    public int voyageLength;
    public int checkTicketTime;
    public int buyTicketTime;
    public int passingTime;

    double passengerAnimationRate;
    double captainAnimationRate;

    Rectangle[] queueSeats;
    public Rectangle[] bridgeSeats;
    public Rectangle[] shipSeats;
    public Label[] shipLabels;
    public Rectangle[] Ship;
    public Rectangle[] Dock;

    List<Animation> passengerAnimations;
    List<Animation> captainAnimations;
    List<Animation> shipAnimations;
    Passenger[] passengers = new Passenger[30];
    Captain[] captains = new Captain[1];

    public void prepareAnimation(){

        passengerAnimations = new ArrayList<Animation>();
        captainAnimations = new ArrayList<Animation>();
        shipAnimations = new ArrayList<Animation>();
        captainAnimationRate = 1;
        passengerAnimationRate = 1;
        Ship = new Rectangle[1];
        Ship[0] = new Rectangle(656, 0, 150, 800);
        Ship[0].setArcWidth(100);
        Ship[0].setArcHeight(150);
        Ship[0].setStroke(Color.BLACK);
        Color maroon = new Color(0.4737, 0.1425, 0.1425, 1.0);
        Ship[0].setFill(maroon);
        Main.animationPane.getChildren().addAll(Ship[0]);

        Dock = new Rectangle[1];
        Dock[0] = new Rectangle(0, 0, 110, 800);
        Dock[0].setStroke(Color.BLACK);
        Dock[0].setFill(Color.GRAY);
        Main.animationPane.getChildren().addAll(Dock[0]);

        queueSeats = new Rectangle[100];
        for (int i = 0; i < queueSquareCoords.length; i++){
            queueSeats[i] = new Rectangle(queueSquareCoords[i].x, queueSquareCoords[i].y, 45, queueSeatSize);
            queueSeats[i].setFill(Color.LIGHTGRAY);
            queueSeats[i].setStroke(Color.BLACK);
            Main.animationPane.getChildren().addAll(queueSeats[i]);
        }

        bridgeSeats = new Rectangle[bridgeCapacity];
        for (int i = 0; i< bridgeSquareCoords.length; i++){
            bridgeSeats[i] = new Rectangle(bridgeSquareCoords[i].x, bridgeSquareCoords[i].y, bridgeSeatSize, 50);
            Color brown = new Color(0.7368, 0.5697, 0.4503, 1.0);
            bridgeSeats[i].setFill(brown);
            bridgeSeats[i].setStroke(Color.BLACK);
            Main.animationPane.getChildren().addAll(bridgeSeats[i]);
        }

        shipSeats = new Rectangle[shipCapacity];
        shipLabels = new Label[shipCapacity];
        for (int i = 0; i < shipSquareCoords.length; i++){
            shipSeats[i] = new Rectangle(shipSquareCoords[i].x, shipSquareCoords[i].y, 45, shipSeatSize);
            shipSeats[i].setFill(Color.LIGHTGRAY);
            shipSeats[i].setStroke(Color.BLACK);
            int j = i+1;
            shipLabels[i] = new Label(""+j);
            shipLabels[i].setLayoutX(shipSquareCoords[i].x - 15);
            shipLabels[i].setLayoutY(shipSquareCoords[i].y + 10);
            shipLabels[i].setTextFill(Color.WHITE);
            Main.animationPane.getChildren().addAll(shipSeats[i], shipLabels[i]);
        }
    }

    public void shipDeparture(){
        captains[0].circle.setVisible(false);
        captains[0].circle.setVisible(false);
        for (int j = 0; j < shipCapacity; j++) {
            shipSeats[j].setVisible(false);
            shipSeats[j].setManaged(false);
        }
        for (int j = 0; j < bridgeCapacity; j++) {
            bridgeSeats[j].setVisible(false);
            bridgeSeats[j].setManaged(false);
        }
        for (int j = 0; j < shipCapacity; j++) {
            shipLabels[j].setVisible(false);
            shipLabels[j].setManaged(false);
        }
        for (Passenger passenger : ship.Ship) {
            passenger.circle.setVisible(false);
            passenger.circle.setManaged(false);

        }
        for (int j = 0; j < ship.passengersOnShip; j++) {
            ship.Ship.poll();
        }
    }


    @FXML
    public void shipArrival(){
        Ship[0].setVisible(true);
        Ship[0].setManaged(true);
        for (int j = 0; j<shipCapacity; j++){
            shipSeats[j].setVisible(true);
            shipSeats[j].setManaged(true);
        }
        for (int j = 0; j<bridgeCapacity; j++){
            bridgeSeats[j].setVisible(true);
            bridgeSeats[j].setManaged(true);
            bridgeSeats[j].setStroke(Color.BLACK);
        }
        for (int j = 0; j<shipCapacity; j++){
            shipLabels[j].setVisible(true);
            shipLabels[j].setManaged(true);
        }
    }

    public void pauseAnimation() throws InterruptedException {
        synchronized (passengerAnimations) {
            for (Animation p : passengerAnimations) {
                p.pause();
            }
        }
        synchronized (captainAnimations){
            for (Animation c: captainAnimations){
                c.pause();
            }
        }
        synchronized (shipAnimations){
            for (Animation s: shipAnimations){
                s.pause();
            }
        }
    }
    public void resumeAnimation() {
        synchronized (passengerAnimations) {
            for (Animation p : passengerAnimations) {
                p.play();
            }
        }
        synchronized (captainAnimations){
            for (Animation c: captainAnimations){
                c.play();
            }
        }
        synchronized (shipAnimations){
            for (Animation s: shipAnimations){
                s.play();
            }
        }
    }

    public void passengerAnimationSpeedChange(double sliderValue){
        synchronized (passengerAnimations){
            if (sliderValue >= 0){
                passengerAnimationRate = 0.02 * sliderValue + 1;
            }
            else{
                passengerAnimationRate = 10.0 / 1000 * sliderValue +1;
            }
            for (Animation a: passengerAnimations){
                a.setRate(passengerAnimationRate);
            }
        }
    }

    public void captainAnimationSpeedChange(double sliderValue){
        synchronized (captainAnimations){
            if (sliderValue >= 0){
                captainAnimationRate = 0.02 * sliderValue + 1;
            }
            else{
                captainAnimationRate = 10.0 / 1000 * sliderValue + 1;
            }
            for (Animation a: captainAnimations){
                a.setRate(captainAnimationRate);
            }
            for (Animation s: shipAnimations){
                s.setRate(captainAnimationRate);
            }
        }
    }

    public void startThreads(){
        ship = new Ship(shipCapacity, bridgeCapacity);
        Semaphore bridgeFree = new Semaphore(ship.bridgeCapacityK);
        Semaphore shipFree = new Semaphore(ship.shipCapacityN);
        Semaphore departure = new Semaphore(ship.shipCapacityN);
        Lock queueMutex = new ReentrantLock();
        Lock shipMutex = new ReentrantLock();
        Lock bridgeMutex = new ReentrantLock();
        Lock checkTicket = new ReentrantLock();
        Lock buyTicket = new ReentrantLock();
        Lock captain = new ReentrantLock();

        captains[0] = new Captain(0, ship, bridgeFree, shipFree, bridgeMutex, shipMutex, departure, captain, this);
        captains[0].start();
        Passenger.q = 0;
        Passenger.b = bridgeCapacity - 1;
        Passenger.s = 0;
        Passenger.time = 0;
        for (int i =0; i < 30; i++){
            passengers[i] = new Passenger(i, ship, bridgeFree, shipFree, bridgeMutex, shipMutex,  departure, captain, buyTicket, checkTicket, queueMutex, this);
            passengers[i].start();
        }
    }

    public void interruptThreads(){
        captains[0].interrupt();
        for (Passenger passenger : passengers) {
            passenger.interrupt();
        }
    }
}
