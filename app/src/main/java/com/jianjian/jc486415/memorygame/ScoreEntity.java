package com.jianjian.jc486415.memorygame;

import java.io.Serializable;

public class ScoreEntity implements Comparable<ScoreEntity>, Serializable{

    private String name;
    private float score;
    private double lat;
    private double lng;

    public ScoreEntity(String name, float score, double lat, double lng) {
        this.name = name;
        this.score = score;
        this.lat = lat;
        this.lng = lng;
    }

    public ScoreEntity(String name, float score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "ScoreEntity{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", latitude=" + lat +
                ", longitude=" + lng +
                '}';
    }

    @Override
    public int compareTo(ScoreEntity compareScore) {
        /* For Ascending order*/
        return (int)(compareScore.getScore()-this.score);
    }
}
