/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
RunLog.java
Class storing log details: date, distance and time
 */

package com.example.alvindrakes.my_fitness_tracker;

public class TrackerLog {
    private String distance;
    private long time;

    public TrackerLog() {
    }

    public TrackerLog(String disstance, long time) {
        this.distance = disstance;
        this.time = time;
    }


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}