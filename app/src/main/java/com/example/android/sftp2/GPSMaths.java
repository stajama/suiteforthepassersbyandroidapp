package com.example.android.sftp2;


import android.util.Log;

/**
 * Created by staja on 10/20/2018.
 */

// GPSMaths() is a class with fixed properties for storing details regarding each sculpture and
// methods for calculating the piece that should currently be playing.
public class GPSMaths {

    private String TAG = GPSMaths.class.getName();

    public Sculpture standingFigures = new Sculpture("Standing Figures",
            "Headless Figures",
            "m01.mp3",
            new double[] {39.045812},
            new double[] {-94.581534},
            new double[] {38.1},
            0);

    public Sculpture rumi = new Sculpture("Rumi",
            "Love is a Madman",
            "m02.mp3",
            new double[] {39.045879},
            new double[] {-94.580090},
            new double[] {45.72},
            0);

    public Sculpture ferment = new Sculpture("Ferment",
            "Broken",
            "m04.mp3",
            new double[] {39.042758},
            new double[] {-94.579753},
            // after initial real world testing, Christina and I thought the radius for Ferment was
            // way too long, encompassing other nearby structures and preventing the Promenade upto
            // the central garden. Reduced the size from 57m to 40m. May be too much of an over-
            // correction, will check with Christina.
            new double[] {40.000},
            0);


    public Sculpture shuttlecockN = new Sculpture("North ShuttleCock",
            "Oda a La Vanguardia",
            "m07.mp3",
            new double[] {39.043357},
            new double[] {-94.581032},
            new double[] {44.196},
            0);

    public Sculpture shuttlecockS = new Sculpture("South ShuttleCock",
            "Fireflies in the Garden",
            "m05.mp3",
            new double[] {39.042691},
            new double[] {-94.581066},
            new double[] {44.196},
            0);

    public Sculpture shuttleTransition = new Sculpture("Between the Shuttlecocks",
            "Both light and shadow",
            "m06.mp3",
            new double[] {0},
            new double[] {0},
            new double[] {0},
            0);

    public Sculpture promenade = new Sculpture("not near any sculptures",
            "Promenade",
            "m09.mp3",
            new double[] {0},
            new double[] {0},
            new double[] {0},
            0);

    public Sculpture rooftop = new Sculpture("Bloch Rooftop, 'Turbo,' or 'Ferryman'",
            "Big Muddy",
            "m03.mp3",
            new double[] {39.043987, 39.043743},
            new double[] {-94.579814, -94.579809},
            new double[] {28.956, 30.48},
            0);

    public Sculpture fourmotives = new Sculpture("Henry Moore Sculptures",
            "To Cast Four Motives",
            "m08.mp3",
            new double[] {39.042582, 39.04288, 39.043271, 39.043560, 39.043824, 38.946644},
            new double[] {-94.581893, -94.581829, -94.581797, -94.581807, -94.581822, -94.592997},
            new double[] {30.48, 28.956, 28.956, 28.956, 27.432, 27.432},
            0);


    public GPSMaths() {

    }

    // calculate_position() calculate the distance between two GPS coordinates. Math comes from
    // https://www.movable-type.co.uk/scripts/latlong.html
    public double calculate_position(double currentLon, double currentLat,
                                      double targetLat, double targetLon) {
        Log.i(TAG, "calculate_position(" + Double.toString(currentLat) + ", " + Double.toString(currentLon) + " --- " + Double.toString(targetLat) + ", " + Double.toString(targetLon));
        double R = 6371e3; // earth's radius (const)
        double phi1 = currentLat * (Math.PI / 180);
        double phi2 = targetLat * (Math.PI / 180);
        double deltaphi = (targetLat - currentLat) * (Math.PI / 180);
        double deltalambda = (targetLon - currentLon) * (Math.PI / 180);

        double a = Math.sin(deltaphi / 2) * Math.sin(deltaphi / 2) +
                    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltalambda / 2) * Math.sin(deltalambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d;
    }

    // playWhat() takes in the current user GPS coordiates and returns the Sculpture that they are
    // within trigger range. If not in range of anything, returns the Promenade.
    public Sculpture playWhat(double currentLat, double currentLon) {
        if (calculate_position(currentLat, currentLon,
                shuttlecockN.getLat()[0], shuttlecockN.getLon()[0]) <= shuttlecockN.getTrigger()[0] &&
            calculate_position(currentLat, currentLon,
                shuttlecockS.getLat()[0], shuttlecockS.getLon()[0]) <= shuttlecockS.getTrigger()[0]) {
            return shuttleTransition;
        } else if (calculate_position(currentLat, currentLon,
                shuttlecockN.getLat()[0], shuttlecockN.getLon()[0]) <= shuttlecockN.getTrigger()[0]) {
            return shuttlecockN;
        } else if (calculate_position(currentLat, currentLon,
                shuttlecockS.getLat()[0], shuttlecockS.getLon()[0]) <= shuttlecockS.getTrigger()[0]) {
            return shuttlecockS;
        } else if (calculate_position(currentLat, currentLon,
                standingFigures.getLat()[0], standingFigures.getLon()[0]) <= standingFigures.getTrigger()[0]) {
            return standingFigures;
        } else if (calculate_position(currentLat, currentLon,
                rumi.getLat()[0], rumi.getLon()[0]) <= rumi.getTrigger()[0]) {
            return rumi;
        } else if (calculate_position(currentLat, currentLon,
                ferment.getLat()[0], ferment.getLon()[0]) <= ferment.getTrigger()[0]) {
            return ferment;
        } else if (calculate_position(currentLat, currentLon,
                rooftop.getLat()[0], rooftop.getLon()[0]) <= rooftop.getTrigger()[0] ||
                   calculate_position(currentLat, currentLon,
                rooftop.getLat()[1], rooftop.getLon()[1]) <= rooftop.getTrigger()[1]) {
            return rooftop;
        } else if (calculate_position(currentLat, currentLon,
                        fourmotives.getLat()[0], fourmotives.getLon()[0]) <= fourmotives.getTrigger()[0] ||
                calculate_position(currentLat, currentLon,
                        fourmotives.getLat()[1], fourmotives.getLon()[1]) <= fourmotives.getTrigger()[1] ||
                calculate_position(currentLat, currentLon,
                        fourmotives.getLat()[2], fourmotives.getLon()[2]) <= fourmotives.getTrigger()[2] ||
                calculate_position(currentLat, currentLon,
                        fourmotives.getLat()[3], fourmotives.getLon()[3]) <= fourmotives.getTrigger()[3] ||
                calculate_position(currentLat, currentLon,
                        fourmotives.getLat()[4], fourmotives.getLon()[4]) <= fourmotives.getTrigger()[4] ||
                calculate_position(currentLat, currentLon,
                    fourmotives.getLat()[5], fourmotives.getLon()[5]) <= fourmotives.getTrigger()[5]){
            return fourmotives;
        } else {
            return promenade;
        }
    }


}
