package com.example.statek;

import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class Captain extends Thread{
    int id;
    int lastVoyagePassengers = 0;
    Ship ship;
    Semaphore bridgeFree;
    Semaphore shipFree;
    Lock bridgeMutex;
    Lock shipMutex;
    Lock captain;
    Random random;
    Semaphore departure;
    Configuration configuration;
    public Circle circle;


    public Captain(int id, Ship ship, Semaphore bridgeFree, Semaphore shipFree, Lock bridgeMutex, Lock shipMutex, Semaphore departure, Lock captain, Configuration configuration){
        this.id = id;
        this.ship = ship;
        this.bridgeFree = bridgeFree;
        this.shipFree = shipFree;
        this.bridgeMutex = bridgeMutex;
        this.shipMutex = shipMutex;
        this.departure = departure;
        this.captain = captain;
        this.random = new Random();
        this.configuration = configuration;
    }

    public void run() {
        for (int i = 0; i < 1000; i++) {
            try {
                if (lastVoyagePassengers == 0) {
                    departure.acquire(ship.shipCapacityN);
                } else {
                    departure.acquire(lastVoyagePassengers);
                }

                    //animation of waiting time to departure
                    //captain circle (black) increases until time runs out, then its stroke turns red
                    circle = new Circle();
                    circle.setCenterX(775);
                    circle.setCenterY(40);
                    circle.setRadius(15);
                    circle.setFill(Color.BLACK);
                    ScaleTransition scaleTransition = new ScaleTransition();
                    StrokeTransition shipWait = new StrokeTransition();
                    scaleTransition.setOnFinished(e -> {
                        unblock();
                        configuration.captainAnimations.remove(scaleTransition);
                        configuration.shipAnimations.remove(shipWait);
                    });
                    scaleTransition.setDuration(Duration.millis(configuration.departureInterval));
                    scaleTransition.setNode(circle);
                    scaleTransition.setFromX(0);
                    scaleTransition.setFromY(0);
                    scaleTransition.setByX(1);
                    scaleTransition.setByY(1);
                    scaleTransition.setToX(1);
                    scaleTransition.setToY(1);
                    shipWait.setAutoReverse(true);
                    shipWait.setDuration(Duration.millis(configuration.departureInterval));
                    shipWait.setFromValue(Color.BLACK);
                    shipWait.setToValue(Color.BLACK);

                    scaleTransition.setAutoReverse(false);

                    Platform.runLater(() -> {
                        Main.animationPane.getChildren().add(circle);
                        scaleTransition.setRate(configuration.captainAnimationRate);
                        shipWait.setRate(configuration.captainAnimationRate);
                        configuration.captainAnimations.add(scaleTransition);
                        configuration.shipAnimations.add(shipWait);
                        scaleTransition.play();
                        shipWait.play();
                    });
                    block();

                    circle.setStroke(Color.RED);

                    //captain locks ship and bridge
                    captain.lock();

                    shipMutex.lock();

                    bridgeMutex.lock();
                    for (int j = 0; j < configuration.bridgeCapacity; j++) {
                        configuration.bridgeSeats[j].setStroke(Color.RED);
                    }

                    //captain checks if bridge is empty
                    if (ship.passengersOnBridge == 0) {
                        //if yes, the ship departures
                        //departure animation
                        configuration.shipDeparture();
                        Voyage();
                        Passenger.s = 0;
                        lastVoyagePassengers = ship.passengersOnShip;

                        //realeasing passengers from the ship
                        departure.release(ship.passengersOnShip);
                        ship.passengersOnShip = 0;

                        //setting elements visible
                        configuration.shipArrival();
                        this.circle.setStroke(Color.BLACK);

                        //captain unlocks bridge and ship
                        bridgeMutex.unlock();
                        shipMutex.unlock();
                        captain.unlock();

                    }

                    else {
                        //if bridge not empty then
                        //captain unlocks ship and bridge to let remaining passengers in
                        shipMutex.unlock();
                        bridgeMutex.unlock();

                        //animation of waiting until every passenger from the bridge gets in
                        while (ship.passengersOnBridge != 0) {
                            StrokeTransition shipWait2 = new StrokeTransition();
                            StrokeTransition captainWait = new StrokeTransition();
                            captainWait.setOnFinished(e -> {
                                unblock();
                                configuration.captainAnimations.remove(captainWait);
                                configuration.shipAnimations.remove(shipWait2);
                            });
                            shipWait2.setAutoReverse(true);
                            shipWait2.setDuration(Duration.millis(configuration.checkTicketTime));
                            shipWait2.setFromValue(Color.BLACK);
                            shipWait2.setToValue(Color.BLACK);
                            captainWait.setAutoReverse(true);
                            captainWait.setDuration(Duration.millis(configuration.checkTicketTime));
                            captainWait.setFromValue(Color.RED);
                            captainWait.setToValue(Color.RED);

                            Platform.runLater(() -> {
                                shipWait2.setRate(configuration.captainAnimationRate);
                                captainWait.setRate(configuration.captainAnimationRate);
                                configuration.captainAnimations.add(captainWait);
                                configuration.shipAnimations.add(shipWait2);
                                captainWait.play();
                                shipWait2.play();
                            });
                            block();
                        }
                        shipMutex.lock();

                        //departure animation
                        configuration.shipDeparture();
                        Voyage();
                        Passenger.s = 0;

                        lastVoyagePassengers = ship.passengersOnShip;

                        //releasing passengers from ship
                        departure.release(ship.passengersOnShip);
                        ship.passengersOnShip = 0;

                        //setting elements visible
                        configuration.shipArrival();
                        this.circle.setStroke(Color.BLACK);


                        //captain unlocks bridge and ship
                        shipMutex.unlock();
                        captain.unlock();
                    }
                }catch(InterruptedException e1){
                    break;
                }
            }
            System.out.println("[Captain] - End");
        }

        public void Voyage() throws InterruptedException {

            //right-rotate animation
            RotateTransition shipRotate = new RotateTransition();
            shipRotate.setOnFinished(e -> {
                unblock();
                configuration.shipAnimations.remove(shipRotate);
            });
            shipRotate.setDuration(Duration.millis(configuration.voyageLength / 10.0));
            shipRotate.setNode(configuration.Ship[0]);
            shipRotate.setAxis(Rotate.Z_AXIS);
            shipRotate.setByAngle(45);
            shipRotate.setAutoReverse(false);
            Platform.runLater(() -> {
                shipRotate.setRate(configuration.captainAnimationRate);
                configuration.shipAnimations.add(shipRotate);
                shipRotate.play();
            });
            block();

            //departure animation
            Path pathDeparture = new Path();
            pathDeparture.getElements().addAll(new MoveTo(730, 400), new LineTo(1500, 0));
            PathTransition pathTransitionDeparture = new PathTransition(Duration.millis((configuration.voyageLength/2.0)-(configuration.voyageLength/10.0)), pathDeparture, configuration.Ship[0]);
            pathTransitionDeparture.setOnFinished(e -> {
                unblock();
                configuration.shipAnimations.remove(pathTransitionDeparture);
            });

            Platform.runLater(()->{
                Main.animationPane.getChildren().add(pathDeparture);
                pathTransitionDeparture.setRate(configuration.captainAnimationRate);
                configuration.shipAnimations.add(pathTransitionDeparture);
                pathTransitionDeparture.play();
            });
            block();
            Platform.runLater(() -> {
                Main.animationPane.getChildren().remove(pathDeparture);
            });

            //arrival animation
            Path pathArrival = new Path();
            pathArrival.getElements().addAll(new MoveTo(1500, 0), new LineTo(730, 400));
            PathTransition pathTransitionArrival = new PathTransition(Duration.millis((configuration.voyageLength/2.0)-(configuration.voyageLength/10.0)), pathArrival, configuration.Ship[0]);
            pathTransitionArrival.setOnFinished(e -> {
                unblock();
                configuration.shipAnimations.remove(pathTransitionArrival);
            });

            Platform.runLater(()->{
                Main.animationPane.getChildren().add(pathArrival);
                pathTransitionArrival.setRate(configuration.captainAnimationRate);
                configuration.shipAnimations.add(pathTransitionArrival);
                pathTransitionArrival.play();
            });
            block();
            Platform.runLater(() -> {
                Main.animationPane.getChildren().remove(pathArrival);
            });

            //left-rotate animation
            RotateTransition shipRotateArrival= new RotateTransition();
            shipRotateArrival.setOnFinished(e -> {
                unblock();
                configuration.shipAnimations.remove(shipRotateArrival);
            });
            shipRotateArrival.setDuration(Duration.millis(configuration.voyageLength/10.0));
            shipRotateArrival.setNode(configuration.Ship[0]);
            shipRotateArrival.setAxis(Rotate.Z_AXIS);
            shipRotateArrival.setByAngle(-45);
            shipRotateArrival.setAutoReverse(false);
            Platform.runLater(() -> {
                shipRotateArrival.setRate(configuration.captainAnimationRate);
                configuration.shipAnimations.add(shipRotateArrival);
                shipRotateArrival.play();
            });
            block();
        }
    public synchronized void block() throws InterruptedException {
        wait();
    }

    public synchronized void unblock() {
        notify();
    }
}