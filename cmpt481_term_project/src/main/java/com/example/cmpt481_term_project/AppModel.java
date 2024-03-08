/*
NAMES: Aaron Mee, Siddharth Padakanti, Andy Giang, Jesse Paterson
NSIDS: ajm403, kca647, iaz102, xgm608
STUDENT NUMBER: 11173570, 11311844, 11326516, 11310937
COURSE: CMPT481 - Term Project
*/
package com.example.cmpt481_term_project;

import javafx.scene.input.KeyCode;
import javafx.scene.shape.Line;

import java.util.*;

public class AppModel {
    private List<AppModelListener> subscribers;
    private List<Target> targets;
    private List<WarpLocation> warps;
    private WarpTrail warpTrail;
    private boolean showWarps;
    private int height;
    private int width;
    private int numTargets;
    private int numTrials;
    private int targetRadius;
    private int currTarget;
    private double mouseX;
    private double mouseY;



    private double flickX, flickY;
    private final double minFlickDistance = 30.0;
    private boolean trackingFlick;




    public enum AppMode {MECH_SELECT, PRE_TRIAL, TRIAL, DONE}

    private AppMode currentMode;

    public enum Mechanism {GRID, USR_KEY, SYS_DEF, FLICK}



    private Mechanism currentMechanism;
    Random random = new Random();
    Timer fadeTimer;
    TimerTask fadeTask;


    /**
     * Creates new app model
     */
    public AppModel(int w, int h) {
        subscribers = new ArrayList<>();
        targets = new ArrayList<>();
        warps = new ArrayList<>();
        warpTrail = new WarpTrail(0.0, 0.0, 0.0, 0.0);

        this.width = w;
        this.height = h;
        this.targetRadius = 30;
        this.numTargets = 50;
        this.numTrials = 10;
        this.showWarps = false;

        this.mouseX = 0;
        this.mouseY = 0;

        this.currentMode = AppMode.MECH_SELECT;

        // Create timer and timertask for fading out mouse trail
        fadeTimer = new Timer();

        // flick stuff
        flickX = 0.0;
        flickY = 0.0;
        trackingFlick = false;
    }

    /**
     * Method that starts a fade timer that repeats at a specified rate
     */
    public void startTrailFadeTimer() {
        fadeTimer.cancel();
        fadeTimer.purge();
        fadeTimer = new Timer();

        warpTrail.reset();
        fadeTask = new TimerTask()
        {
            public void run()
            {
                // Reduce thickness and opacity until it disappears
                if (warpTrail.getOpacity() > 0) {
                    warpTrail.fadeStep();
                    notifySubscribers();
                } else {
                    this.cancel();
                }
            }


        };
        fadeTimer.scheduleAtFixedRate(fadeTask, 0L, 50L);
    }

    public void saveFlickStartCoords() {
        // Save mouse coords where hotkey was first pressed
        this.flickX = this.mouseX;
        this.flickY = this.mouseY;
    }

    public boolean reachedMinFlickDistance() {
        // Calculate distance from flick start to current mouse position
        return calculateDistance(flickX, flickY, mouseX, mouseY) <= minFlickDistance;
    }

    public void setFlickTracking(boolean b) {
        this.trackingFlick = b;
    }
    public boolean trackingFlick() {
        return trackingFlick;
    }

    public int getClosestFlickTarget(double x2, double y2, double x3, double y3) {
        int closestWarp = -1;
        double dist = Double.MAX_VALUE;
        for (int i = 0; i < warps.size(); i++) {
            if (calculateDistanceToLine(x2, y2, x3,y3, warps.get(i).getX(), warps.get(i).getY()) < dist) {
                closestWarp = i + 1;
                dist = calculateDistanceToLine(x2, y2, x3,y3, warps.get(i).getX(), warps.get(i).getY());
            }
        }
        return closestWarp;
    }

    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        // distance = sqrt((x2 - x1)^2 + (y2 - y1)^2)
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        return distance;
    }

    public static double calculateDistanceToLine(double x1, double y1, double x2, double y2, double x0, double y0) {
        // Using the formula for the distance from a point to a line
        double numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1);
        double denominator = Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

        double distance = numerator / denominator;
        return distance;
    }

    public double getFlickX() {
        return flickX;
    }

    public double getFlickY() {
        return flickY;
    }

    public Mechanism getCurrentMechanism() {
        return currentMechanism;
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public WarpTrail getWarpTrail() {
        return warpTrail;
    }

    public void setWarpTrail(double endPointX, double endPointY, double mouseX, double mouseY) {
        warpTrail.setCoords(endPointX, endPointY, mouseX, mouseY);
        notifySubscribers();
    }

    /**
     * Adds a new target
     *
     * @param newTarget - the new target
     */
    public void addTarget(Target newTarget) {
        targets.add(newTarget);
        notifySubscribers();
    }

    /**
     * Adds a new warp location
     *
     * @param newWarp - the new warp location
     */
    public void addWarp(WarpLocation newWarp) {
        warps.add(newWarp);
        notifySubscribers();
    }

    /**
     * Toggles the showing of warp locations
     */
    public void toggleWarps() {
        this.showWarps = !this.showWarps;
        notifySubscribers();
    }

    /**
     * Method for getting list of warps
     * @return - Returns the WarpLocation ArrayList
     */
    public List<WarpLocation> getWarps() {
        return warps;
    }

    /**
     * Returns if warps are toggled to be visible
     * @return
     */
    public boolean isWarpsVisible() {
        return showWarps;
    }

    /**
     * Adds a subscriber to the model
     *
     * @param sub the subscriber
     */
    public void addSubscriber(AppModelListener sub) {
        subscribers.add(sub);
    }

    /**
     * Notifies all subscribers
     */
    private void notifySubscribers() {
        subscribers.forEach(AppModelListener::modelChanged);
    }

    /**
     * Gets the list of targets
     *
     * @return - THe list of targets
     */
    public List<Target> getTargets() {
        return targets;
    }

    /**
     * Checks if a specific target has been hit
     *
     * @param x - the x coordinate of the point to check
     * @param y - the y coordinate of the point to check
     * @return - true if hit, false otherwise
     */
    public boolean hitTarget(int x, int y) {
        Target targetHit = targets.get(this.currTarget);
        return targetHit.contains(x, y);
    }

    /**
     * Returns the current model mode
     */
    public AppMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Records a click event during trials
     */
    public void recordClick(double x, double y) {
        int oldTarget = currTarget;
        // Method for recording a click during trial mode
        if (hitTarget((int) x, (int) y)) {
            // Deselect old target
            targets.get(currTarget).deselect();
            // Select new target
            while (currTarget == oldTarget) {
                this.currTarget = random.nextInt(numTargets);
            }
            targets.get(currTarget).select();
            numTrials--;
            if (numTrials == 0) {
                nextMode();
            }
            notifySubscribers();
        }
    }

    /**
     * Sets the models mechanism
     */
    public void setMechanism(KeyCode k) {
        switch (k) {
            case DIGIT1 -> {
                this.currentMechanism = Mechanism.GRID;
            }
            case DIGIT2 -> {
                this.currentMechanism = Mechanism.USR_KEY;
            }
            case DIGIT3 -> {
                this.currentMechanism = Mechanism.SYS_DEF;

            }
            case DIGIT4 -> {
                this.currentMechanism = Mechanism.FLICK;
            }
        }
    }

    /**
     * Advances the current model mode
     */
    public void nextMode() {
        switch (this.currentMode) {
            case MECH_SELECT -> {
                this.currentMode = AppMode.PRE_TRIAL;
            }
            case PRE_TRIAL -> {
                this.currentMode = AppMode.TRIAL;
                generateRandomTargets();
            }
            case TRIAL -> {
                this.currentMode = AppMode.DONE;
            }
            case DONE -> {
                System.exit(0);
            }
        }
        notifySubscribers();
    }

    /**
     * Generates a random selection of targets with no overlaps
     */
    public void generateRandomTargets() {
        random = new Random();
        int maxX = width - targetRadius;
        int maxY = height - targetRadius;
        int min = targetRadius * 2;

        for (int i = 0; i < numTargets; i++) {
            // create targets and give it random coords with no overlaps
            while (true) {
                boolean overlap = false;
                int targetX = random.nextInt(maxX - min + 1) + min;
                int targetY = random.nextInt(maxY - min + 1) + min;
                for (Target t : targets) {
                    if (Math.sqrt(Math.pow(targetX - t.getX(), 2) + Math.pow(targetY - t.getY(), 2)) < min) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    Target newTarget = new Target(targetX, targetY, targetRadius);
                    this.addTarget(newTarget);
                    break;
                }
            }
        }

        this.currTarget = random.nextInt(numTargets);
        targets.get(currTarget).select();

        notifySubscribers();
    }

}
