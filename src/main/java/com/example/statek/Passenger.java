package com.example.statek;
import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class Passenger extends Thread {
    int id;
    Ship ship;
    Semaphore bridgeFree;
    Semaphore shipFree;
    Lock bridgeMutex;
    Lock shipMutex;
    Lock captain;
    Random random;
    Semaphore departure;
    Lock buyTicket;
    Lock checkTicket;
    Lock queueMutex;

    Configuration configuration;
    static int time;
    static int q; //shared index for queue
    static int b; //shared index for bridge
    static int s; //shared index for ship
    public int thisQ;
    public int thisB;
    public int thisS;
    public Circle circle;
    static Color[] colours = new Color[]{Color.BLUE, Color.BROWN, Color.GREEN, Color.YELLOW, Color.RED, Color.ORANGE, Color.WHITE, Color.LIMEGREEN, Color.PINK, Color.LIGHTBLUE, Color.LIGHTCORAL, Color.BLUEVIOLET};

    public Passenger(int id, Ship ship, Semaphore bridgeFree, Semaphore shipFree, Lock bridgeMutex, Lock shipMutex, Semaphore departure, Lock captain, Lock buyTicket, Lock checkTicket, Lock queueMutex, Configuration configuration) {
        this.id = id;
        this.ship = ship;
        this.bridgeFree = bridgeFree;
        this.shipFree = shipFree;
        this.bridgeMutex = bridgeMutex;
        this.shipMutex = shipMutex;
        this.configuration = configuration;
        random = new Random();
        this.departure = departure;
        this.captain = captain;
        this.buyTicket = buyTicket;
        this.checkTicket = checkTicket;
        this.queueMutex = queueMutex;
    }

    public void run() {
        for (int j = 0; j<1000; j++) {
            try {
                //passenger loading animation
                this.circle = new Circle();
                circle.setCenterX(configuration.queueCircleCoords[50].x);
                circle.setCenterY(configuration.queueCircleCoords[50].y);
                circle.setRadius(configuration.manSize);
                int colour = id % colours.length;
                circle.setFill(colours[colour]);

                ScaleTransition scaleTransitionSleep = new ScaleTransition();
                scaleTransitionSleep.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(scaleTransitionSleep);
                });
                scaleTransitionSleep.setDuration(Duration.millis(((long) this.id * (configuration.passengerMaxTime+50)) + (random.nextInt(configuration.passengerMaxTime - configuration.passengerMinTime) + configuration.passengerMinTime)));
                scaleTransitionSleep.setNode(this.circle);
                scaleTransitionSleep.setFromX(0);
                scaleTransitionSleep.setFromY(0);
                scaleTransitionSleep.setByX(0);
                scaleTransitionSleep.setByY(0);
                scaleTransitionSleep.setToX(1);
                scaleTransitionSleep.setToY(1);
                scaleTransitionSleep.setAutoReverse(false);

                Platform.runLater(() -> {
                    Main.animationPane.getChildren().add(circle);
                    scaleTransitionSleep.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(scaleTransitionSleep);
                    scaleTransitionSleep.play();
                });
                block();

                queueMutex.lock();

                //passenger queueing animation
                this.thisQ = q;
                Path path = new Path();
                MoveTo moveTo = new MoveTo();
                moveTo.setX(configuration.queueCircleCoords[50].x);
                moveTo.setY(configuration.queueCircleCoords[50].y);
                LineTo lineTo = new LineTo();
                lineTo.setX(configuration.queueCircleCoords[thisQ].x);
                lineTo.setY(configuration.queueCircleCoords[thisQ].y);
                path.getElements().addAll(moveTo, lineTo);
                PathTransition pathTransition = new PathTransition(Duration.millis(1500), path, circle);
                pathTransition.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(pathTransition);
                });

                Platform.runLater(()->{
                    Main.animationPane.getChildren().add(path);
                    pathTransition.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(pathTransition);
                    pathTransition.play();
                });
                block();

                ship.Queue.add(this);

                q = (q + 1) % 100;
                Platform.runLater(() -> {
                    Main.animationPane.getChildren().remove(path);
                });

                ship.passengersInQueue++;
                queueMutex.unlock();

                //trying to buy ticket (only available when first in queue)
                buyTicket.lock();

                //ticket buy animation
                ScaleTransition scaleTransitionBuyTicket = new ScaleTransition();
                scaleTransitionBuyTicket.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(scaleTransitionBuyTicket);
                });
                scaleTransitionBuyTicket.setDuration(Duration.millis(configuration.buyTicketTime));
                scaleTransitionBuyTicket.setNode(this.circle);
                scaleTransitionBuyTicket.setFromX(0);
                scaleTransitionBuyTicket.setFromY(0);
                scaleTransitionBuyTicket.setByX(0);
                scaleTransitionBuyTicket.setByY(0);
                scaleTransitionBuyTicket.setToX(1);
                scaleTransitionBuyTicket.setToY(1);

                scaleTransitionBuyTicket.setAutoReverse(false);

                Platform.runLater(() -> {
                    scaleTransitionBuyTicket.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(scaleTransitionBuyTicket);
                    scaleTransitionBuyTicket.play();
                });
                block();

                //checks if there is a place on the ship and bridge
                shipFree.acquire();
                bridgeFree.acquire();

                //checks if captain is active
                captain.lock();
                captain.unlock();

                //tries to lock queue
                queueMutex.lock();

                //tries to lock bridge
                bridgeMutex.lock();

                //moving to bridge animation
                thisB = b;
                Path pathBridge = new Path();
                MoveTo moveToBridge = new MoveTo();
                moveToBridge.setX(configuration.queueCircleCoords[thisQ].x);
                moveToBridge.setY(configuration.queueCircleCoords[thisQ].y);
                LineTo lineToBridge = new LineTo();
                lineToBridge.setX(configuration.bridgeCircleCoords[thisB].x);
                lineToBridge.setY(configuration.bridgeCircleCoords[thisB].y);
                pathBridge.getElements().addAll(moveToBridge, lineToBridge);
                PathTransition pathTransitionBridge = new PathTransition(Duration.millis(configuration.passingTime), pathBridge, circle);
                pathTransitionBridge.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(pathTransitionBridge);
                });

                Platform.runLater(()->{
                    Main.animationPane.getChildren().add(pathBridge);
                    pathTransitionBridge.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(pathTransitionBridge);
                    pathTransitionBridge.play();
                });
                block();

                ship.Queue.poll();
                ship.Bridge.add(this);
                b = (b - 1) % configuration.bridgeCapacity;
                q = (q - 1) % 100;
                Platform.runLater(() -> {
                    Main.animationPane.getChildren().remove(pathBridge);
                });

                ship.passengersOnBridge++;
                ship.passengersInQueue--;

                //unlocks bridge and lets next passenger to buy ticket
                bridgeMutex.unlock();
                buyTicket.unlock();

                //animation of moving passengers in queue
                if (ship.passengersInQueue > 0) {
                    for (Passenger i: ship.Queue) {
                        Path pathMoveQueue = new Path();
                        MoveTo moveQueue = new MoveTo();
                        moveQueue.setX(configuration.queueCircleCoords[i.thisQ].x);
                        moveQueue.setY(configuration.queueCircleCoords[i.thisQ].y);
                        LineTo lineToForward = new LineTo();
                        i.thisQ = i.thisQ - 1;
                        lineToForward.setX(configuration.queueCircleCoords[i.thisQ].x);
                        lineToForward.setY(configuration.queueCircleCoords[i.thisQ].y);
                        pathMoveQueue.getElements().addAll(moveQueue, lineToForward);
                        PathTransition pathTransitionMoveQueue = new PathTransition(Duration.millis(500.0/ship.Queue.size()), pathMoveQueue, i.circle);
                        pathTransitionMoveQueue.setOnFinished(e -> {
                            unblock();
                            configuration.passengerAnimations.remove(pathTransitionMoveQueue);
                        });

                        Platform.runLater(() -> {
                            Main.animationPane.getChildren().add(pathMoveQueue);
                            pathTransitionMoveQueue.setRate(configuration.passengerAnimationRate);
                            configuration.passengerAnimations.add(pathTransitionMoveQueue);
                            pathTransitionMoveQueue.play();
                        });
                        block();

                        Platform.runLater(() -> {
                            Main.animationPane.getChildren().remove(pathMoveQueue);
                        });
                    }
                }

                queueMutex.unlock();

                //tries to get ticket checked (only available for the closest to the ship passenger)
                checkTicket.lock();

                //check ticket animation
                ScaleTransition scaleTransitionCheckTicket = new ScaleTransition();
                scaleTransitionCheckTicket.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(scaleTransitionCheckTicket);
                });
                scaleTransitionCheckTicket.setDuration(Duration.millis(configuration.checkTicketTime));
                scaleTransitionCheckTicket.setNode(this.circle);
                scaleTransitionCheckTicket.setFromX(0);
                scaleTransitionCheckTicket.setFromY(0);
                scaleTransitionCheckTicket.setByX(0);
                scaleTransitionCheckTicket.setByY(0);
                scaleTransitionCheckTicket.setToX(1);
                scaleTransitionCheckTicket.setToY(1);

                scaleTransitionCheckTicket.setAutoReverse(false);

                Platform.runLater(() -> {
                    scaleTransitionCheckTicket.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(scaleTransitionCheckTicket);
                    scaleTransitionCheckTicket.play();
                });
                block();

                //tries to lock ship and bridge
                shipMutex.lock();
                bridgeMutex.lock();

                //animation of moving on the ship
                thisS = s;
                Path pathShip = new Path();
                MoveTo moveToShip = new MoveTo();
                moveToShip.setX(configuration.bridgeCircleCoords[thisB].x);
                moveToShip.setY(configuration.bridgeCircleCoords[thisB].y);
                LineTo lineToShip = new LineTo();
                lineToShip.setX(configuration.shipCircleCoords[thisS].x);
                lineToShip.setY(configuration.shipCircleCoords[thisS].y);
                pathShip.getElements().addAll(moveToShip, lineToShip);
                PathTransition pathTransitionShip = new PathTransition(Duration.millis(configuration.passingTime), pathShip, circle);
                pathTransitionShip.setOnFinished(e -> {
                    unblock();
                    configuration.passengerAnimations.remove(pathTransitionShip);
                });

                Platform.runLater(()->{
                    Main.animationPane.getChildren().add(pathShip);
                    pathTransitionShip.setRate(configuration.passengerAnimationRate);
                    configuration.passengerAnimations.add(pathTransitionShip);
                    pathTransitionShip.play();
                });
                block();
                ship.Ship.add(this);
                s = (s + 1) % configuration.shipCapacity;
                Platform.runLater(() -> {
                    Main.animationPane.getChildren().remove(pathShip);
                });


                ship.Bridge.poll();
                b = (b + 1) % configuration.bridgeCapacity;
                ship.passengersOnShip++;
                ship.passengersOnBridge--;

                //unlocks ship and releases place on the bridge
                shipMutex.unlock();
                bridgeFree.release();

                //animation of moving passengers on the bridge
                if (ship.passengersOnBridge > 0) {
                    for (Passenger i: ship.Bridge) {
                        Path pathMoveBridge = new Path();
                        MoveTo moveBridge = new MoveTo();
                        moveBridge.setX(configuration.bridgeCircleCoords[i.thisB].x);
                        moveBridge.setY(configuration.bridgeCircleCoords[i.thisB].y);
                        LineTo lineToForward = new LineTo();
                        i.thisB = i.thisB + 1;
                        lineToForward.setX(configuration.bridgeCircleCoords[i.thisB].x);
                        lineToForward.setY(configuration.bridgeCircleCoords[i.thisB].y);
                        pathMoveBridge.getElements().addAll(moveBridge, lineToForward);
                        PathTransition pathTransitionMoveBridge = new PathTransition(Duration.millis(250), pathMoveBridge, i.circle);
                        pathTransitionMoveBridge.setOnFinished(e -> {
                            unblock();
                            configuration.passengerAnimations.remove(pathTransitionMoveBridge);
                        });

                        Platform.runLater(() -> {
                            Main.animationPane.getChildren().add(pathMoveBridge);
                            pathTransitionMoveBridge.setRate(configuration.passengerAnimationRate);
                            configuration.passengerAnimations.add(pathTransitionMoveBridge);
                            pathTransitionMoveBridge.play();
                        });
                        block();

                        Platform.runLater(() -> {
                            Main.animationPane.getChildren().remove(pathMoveBridge);
                        });
                    }
                }

                //unlocks bridge and ticket-check
                bridgeMutex.unlock();
                checkTicket.unlock();

                //takes a seat on the ship
                departure.acquire();
                departure.release();

                //releases a sit after signal from captain
                shipFree.release();


            }catch (InterruptedException e1){
                break;
            }
        }
        System.out.println("[Passenger "+id+"] - End");
    }

    public synchronized void block() throws InterruptedException {
        wait();
    }

    public synchronized void unblock() {
        notify();
    }
}