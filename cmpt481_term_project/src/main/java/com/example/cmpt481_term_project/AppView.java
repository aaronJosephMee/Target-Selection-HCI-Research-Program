/*
NAMES: Aaron Mee, Siddharth Padakanti, Andy Giang, Jesse Paterson
NSIDS: ajm403, kca647, iaz102, xgm608
STUDENT NUMBER: 11173570, 11311844, 11326516, 11310937
COURSE: CMPT481 - Term Project
*/
package com.example.cmpt481_term_project;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;


public class AppView extends StackPane implements AppModelListener {
    GraphicsContext gc;
    Canvas myCanvas;
    AppModel model;
    AppController controller;

    /**
     * Creates the basic app view
     */
    public AppView() {
        myCanvas = new Canvas(1500, 900);
        gc = myCanvas.getGraphicsContext2D();

        this.getChildren().add(myCanvas);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
    }

    /**
     * Draws to the canvas
     */
    private void draw() {
        // Draw the actual model based on the current app mode

        switch (model.getCurrentMode()) {
            case MECH_SELECT -> {
                // clear canvas
                gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
                gc.fillText("Please select a mechanism [1-4].", myCanvas.getWidth() / 2, 50);
            }
            case PRE_TRIAL -> {
                // clear canvas
                gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
                if (model.getCurrentMechanism() == AppModel.Mechanism.SYS_DEF) {
                    gc.fillText("Click 20 targets randomly on the screen (you can even repeat the same targets).", myCanvas.getWidth() / 2, 50);
                    gc.fillText("Once done, you will be presented with highlighted targets to click.", myCanvas.getWidth() / 2, 80);
                    gc.fillText("Click on the targets, press ENTER to start.", myCanvas.getWidth() / 2, 110);
                }
                else {
                    gc.fillText("Click on the targets, press ENTER to start.", myCanvas.getWidth() / 2, 50);
                }
            }
            case TRIAL -> {

                switch(model.getTrialMode()) {
                    case REAL_UI -> {
                        // draw the REAL UI and populate the targets
                        gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());

                        // draw image
                        gc.drawImage(model.getUIImage(), 0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

                        // draw rectangular targets
                        for (Target t : model.getTargets()) {
                            t.drawTarget(gc);
                        }

                        if (model.isWarpsVisible()) {

                            // draw grid if in "GRID" mechanism state
                            if (model.getCurrentMechanism() == AppModel.Mechanism.GRID) {
                                drawGrid((int) getHeight()/100,(int) getWidth()/100);
                            }
                            // draw warp locations
                            int warpNumber = 1;
                            for (WarpLocation w : model.getWarps()) {
                                w.drawWarpLocations(gc, warpNumber);
                                warpNumber++;
                            }
                        }
                        // Draw warp trail
                        model.getWarpTrail().draw(gc);
                    }
                    case RANDOM_TARGETS -> {
                        // clear canvas
                        gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
                        gc.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 10));
                        // draw targets
                        int targetNumber = 1;
                        for (Target t : model.getTargets()) {
                            t.drawTarget(gc);
                            targetNumber++;
                        }


                        if (model.isWarpsVisible()) {

                            // draw grid if in "GRID" mechanism state
                            if (model.getCurrentMechanism() == AppModel.Mechanism.GRID) {
                                drawGrid((int) getHeight()/100,(int) getWidth()/100);
                            }
                            // draw warp locations
                            int warpNumber = 1;
                            for (WarpLocation w : model.getWarps()) {
                                w.drawWarpLocations(gc, warpNumber);
                                warpNumber++;
                            }
                        }
                        // Draw warp trail
                        model.getWarpTrail().draw(gc);
                    }
                    case CLUSTER_TARGETS -> {

                    }
                }
            }
            case DONE -> {
                // clear canvas
                gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
                gc.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
                gc.fillText("Trial Complete, press any key to exit.", myCanvas.getWidth() / 2, 50);
            }
        }
    }

    /**
     * Creates a visible grid based on the inputted parameters and the general size of the canvas
     *
     * @param y - The amount of squares on the Y-axis of the grid
     * @param x - The amount of squares on the X-axis of the grid
     */
    public void drawGrid(int y, int x) {
        double xPos = getHeight()/ y;
        double yPos = getWidth()/x;
        gc.setFill(Color.rgb(0, 255, 50, 0.5));
        while (xPos < getHeight() || yPos < getWidth()) {
            if (xPos < getHeight()) {
                gc.fillRect(0, xPos, getWidth(), 5);
                xPos += getHeight()/ y;
            }
            if (yPos < getWidth()) {
                gc.fillRect(yPos, 0, 5, getHeight());
                yPos += getWidth()/x;
            }
        }
    }

    /**
     * Sets the AppView model
     *
     * @param newModel - The model to set
     */
    public void setModel(AppModel newModel) {
        model = newModel;
    }

    /**
     * Handles model changes - redraws canvas
     */
    @Override
    public void modelChanged() {
        draw();
    }

    /**
     * Sets the AppView controller
     *
     * @param controller - Controller to set
     */
    public void setController(AppController controller) {
        this.controller = controller;

        // Canvas is set from controller to help with mouse warping
        this.controller.setCanvas(myCanvas);

        myCanvas.setOnMousePressed(controller::handlePress);
        myCanvas.setOnMouseReleased(controller::handleReleased);
        myCanvas.setOnKeyReleased(controller::handleKeyPressed);
        myCanvas.setOnKeyReleased(controller::handleKeyReleased);
        myCanvas.setOnMouseMoved(controller::handleMouseMoved);
    }

}
