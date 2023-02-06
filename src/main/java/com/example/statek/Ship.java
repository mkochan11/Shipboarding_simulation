package com.example.statek;

import java.util.LinkedList;
import java.util.Queue;
public class Ship {
    public final int shipCapacityN;
    public final int bridgeCapacityK;
    public int passengersOnBridge;
    public int passengersOnShip;
    public int passengersInQueue;
    public Queue<Passenger> Queue;
    public Queue<Passenger> Bridge;
    public Queue<Passenger> Ship;

    public Ship(int N, int K){
        this.shipCapacityN = N;
        this.bridgeCapacityK = K;
        passengersOnShip = 0;
        passengersOnBridge = 0;
        passengersInQueue = 0;
        Queue = new LinkedList<>();
        Bridge = new LinkedList<>();
        Ship = new LinkedList<>();
    }
}
