package com.example.android.sftp2;

/**
 * Created by staja on 10/20/2018.
 */

// a class representing each sculpture. Used as data structure design to simplify calls to
// complete data sets.
public class Sculpture {

    private String name;
    private double[] lat;
    private double[] lon;
    private String title;
    private double[] trigger;
    private int startTime;
    private String piece;

    public Sculpture(String xname, String xtitle, String xpiece, double[] xlat, double[] xlon,
                     double[] xtrigger, int xstartTime) {
        name = xname;
        title = xtitle;
        piece = xpiece;
        lat = xlat;
        lon = xlon;
        trigger = xtrigger;
        startTime = xstartTime;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getPiece() { return piece; }
    public double[] getLat() { return lat; }
    public double[] getLon() { return lon; }
    public double[] getTrigger() { return trigger; }
    public int getStartTime() {return startTime; }
    public void setStartTime(int value) { startTime = value; }
}
