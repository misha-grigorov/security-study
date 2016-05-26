package com.dataart.security.auth;

public class LoginAttempt {
    private int counter;
    private long timeStamp;

    public LoginAttempt() {
        increaseAndGetCounter();
    }

    public LoginAttempt(int counter) {
        this.counter = counter;
        timeStamp = System.currentTimeMillis();
    }

    public void resetCounter() {
        counter = 0;
        timeStamp = System.currentTimeMillis();
    }

    public int increaseAndGetCounter() {
        ++counter;
        timeStamp = System.currentTimeMillis();

        return counter;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getCounter() {
        return counter;
    }
}
