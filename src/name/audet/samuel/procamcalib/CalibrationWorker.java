/*
 * Copyright (C) 2009,2010 Samuel Audet
 *
 * This file is part of ProCamCalib.
 *
 * ProCamCalib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * ProCamCalib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProCamCalib.  If not, see <http://www.gnu.org/licenses/>.
 */

package name.audet.samuel.procamcalib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import name.audet.samuel.javacv.CameraDevice;
import name.audet.samuel.javacv.CanvasFrame;
import name.audet.samuel.javacv.ColorCalibrator;
import name.audet.samuel.javacv.FrameGrabber;
import name.audet.samuel.javacv.FrameGrabber.ColorMode;
import name.audet.samuel.javacv.GeometricCalibrator;
import name.audet.samuel.javacv.MarkedPlane;
import name.audet.samuel.javacv.Marker;
import name.audet.samuel.javacv.MarkerDetector;
import name.audet.samuel.javacv.Parallel;
import name.audet.samuel.javacv.Parallel.Looper;
import name.audet.samuel.javacv.ProCamColorCalibrator;
import name.audet.samuel.javacv.ProCamGeometricCalibrator;
import name.audet.samuel.javacv.ProjectiveDevice;
import name.audet.samuel.javacv.ProjectorDevice;

import static name.audet.samuel.javacv.jna.cxcore.*;
import static name.audet.samuel.javacv.jna.cv.*;

/**
 *
 * @author Samuel Audet
 */
public class CalibrationWorker extends SwingWorker {

    Marker[][] markers;
    MarkedPlane boardPlane;
    CameraSettings cameraSettings = null;
    ProjectorSettings projectorSettings = null;
    Marker.ArraySettings markerSettings = null;
    MarkerDetector.Settings markerDetectorSettings = null;
    GeometricSettings geometricCalibratorSettings = null;
    ColorSettings colorCalibratorSettings = null;

    CameraDevice[] cameraDevices = null;
    CanvasFrame[] cameraCanvasFrames = null;
    FrameGrabber[] frameGrabbers = null;
    ProjectorDevice[] projectorDevices = null;
    CanvasFrame[] projectorCanvasFrames = null;
    MarkedPlane[] projectorPlanes = null;

    ProCamGeometricCalibrator[] proCamGeometricCalibrators = null;
    GeometricCalibrator[] geometricCalibrators = null;

    ProCamColorCalibrator[][] proCamColorCalibrators = null;

    public static class GeometricSettings extends ProCamGeometricCalibrator.Settings {
        boolean enabled = true;
        boolean useMarkerCenters = true;
        int imagesInTotal = 10;
        long timeIntervalMin = 2000;

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            pcs.firePropertyChange("enabled", this.enabled, this.enabled = enabled);
        }

        public boolean isUseMarkerCenters() {
            return useMarkerCenters;
        }
        public void setUseMarkerCenters(boolean useMarkerCenters) {
            this.useMarkerCenters = useMarkerCenters;
        }

        public int getImagesInTotal() {
            return imagesInTotal;
        }
        public void setImagesInTotal(int imagesInTotal) {
            imagesInTotal = Math.max(3, imagesInTotal);
            this.imagesInTotal = imagesInTotal;
        }

        public long getTimeIntervalMin() {
            return timeIntervalMin;
        }
        public void setTimeIntervalMin(long timeIntervalMin) {
            this.timeIntervalMin = timeIntervalMin;
        }
    }

    public static class ColorSettings extends ProCamColorCalibrator.Settings {
        boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            pcs.firePropertyChange("enabled", this.enabled, this.enabled = enabled);
        }
    }

    public void init() throws Exception {
        // create arrays and canvas frames on the Event Dispatcher Thread...
        CameraDevice.Settings[] cs = cameraSettings.toTypedArray();
        if (cameraDevices == null) {
            cameraDevices = new CameraDevice[cs.length];
        } else {
            cameraDevices = Arrays.copyOf(cameraDevices, cs.length);
        }
        cameraCanvasFrames = new CanvasFrame[cs.length];
        frameGrabbers = new FrameGrabber[cs.length];
        for (int i = 0; i < cs.length; i++) {
            if (cameraDevices[i] == null) {
                cameraDevices[i] = new CameraDevice(cs[i]);
            } else {
                cameraDevices[i].setSettings(cs[i]);
            }
            if (cameraSettings.getWindowScale() > 0) {
                cameraCanvasFrames[i] = new CanvasFrame(false, cs[i].getName());
            }
        }

        ProjectorDevice.Settings[] ps = projectorSettings.toTypedArray();
        if (projectorDevices == null) {
            projectorDevices = new ProjectorDevice[ps.length];
        } else {
            projectorDevices = Arrays.copyOf(projectorDevices, ps.length);
        }
        projectorCanvasFrames = new CanvasFrame[ps.length];
        projectorPlanes = new MarkedPlane[ps.length];
        for (int i = 0; i < ps.length; i++) {
            if (projectorDevices[i] == null) {
                projectorDevices[i] = new ProjectorDevice(ps[i]);
            } else {
                projectorDevices[i].setSettings(ps[i]);
            }
            projectorCanvasFrames[i] = projectorDevices[i].createCanvasFrame();
            projectorCanvasFrames[i].showColor(Color.BLACK);
            Dimension dim = projectorCanvasFrames[i].getSize();
            projectorPlanes[i] = new MarkedPlane(dim.width, dim.height, markers[1], true,
                    cvScalarAll(ps[0].getBrightnessForeground()*255),
                    cvScalarAll(ps[0].getBrightnessBackground()*255), 4);
        }
    }

    // synchronized with done()...
    @Override protected synchronized Object doInBackground() throws Exception {
        try {
            final double scale = cameraSettings.getWindowScale();
            // access frame grabbers from _this_ thread *ONLY*...
            for (int i = 0; i < cameraDevices.length; i++) {
                frameGrabbers[i] = cameraDevices[i].createFrameGrabber();
                if (projectorDevices.length > 0) {
                    // we only need trigger mode if we have projectors to wait after...
                    frameGrabbers[i].setTriggerMode(true);
                }
                if (geometricCalibratorSettings.enabled) {
                    frameGrabbers[i].setColorMode(ColorMode.GRAYSCALE);
                } else if (colorCalibratorSettings.enabled) {
                    frameGrabbers[i].setColorMode(ColorMode.BGR);
                }
                frameGrabbers[i].start();

                if (scale <= 0) {
                    continue;
                }

                if (frameGrabbers[i].isTriggerMode()) {
                    frameGrabbers[i].trigger();
                }
                final IplImage image = frameGrabbers[i].grab();
                //final IplImage image = IplImage.create(640, 480, IPL_DEPTH_8U, 1);
                final CanvasFrame c = cameraCanvasFrames[i];
                final String name = cameraDevices[i].getSettings().getName();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        c.setCanvasSize((int)Math.round(image.width *scale),
                                        (int)Math.round(image.height*scale));
                        c.setTitle(name + " (" + image.width + " x " + image.height + "  " +
                                   (image.depth&~IPL_DEPTH_SIGN) + " bpp) - ProCamCalib");
                    }
                });
            }
            if (scale > 0) {
                CanvasFrame.tile(cameraCanvasFrames);
            }
            // start calibration algorithm here
            FrameGrabber.Array frameGrabberArray = frameGrabbers[0].createArray(frameGrabbers);
            if (geometricCalibratorSettings.enabled && !isCancelled()) {
                for (FrameGrabber f : frameGrabbers) {
                    if (f.getColorMode() != ColorMode.GRAYSCALE) {
                        f.stop();
                        f.setColorMode(ColorMode.GRAYSCALE);
                        f.start();
                    }
                }
                calibrateGeometry(frameGrabberArray);
            }
            if (colorCalibratorSettings.enabled && !isCancelled()) {
                for (FrameGrabber f : frameGrabbers) {
                    if (f.getColorMode() != ColorMode.BGR) {
                        f.stop();
                        f.setColorMode(ColorMode.BGR);
                        f.start();
                    }
                }
                calibrateColor(frameGrabberArray);
            }
        } catch (Throwable ex) {
            if (!isCancelled()) {
                Logger.getLogger(CalibrationWorker.class.getName()).log(Level.SEVERE,
                        "Could not perform calibration", ex);
                cancel(false);
            }
        }

        for (int i = 0; i < frameGrabbers.length; i++) {
            if (frameGrabbers[i] != null) {
                try {
                    frameGrabbers[i].release();
                } catch (Exception ex) {
                    Logger.getLogger(CalibrationWorker.class.getName()).log(Level.SEVERE,
                            "Could not release frame grabber resources", ex);
                }
                frameGrabbers[i] = null;
            }
        }

        return null;
    }

    // synchronized with doInBackground()...
    @Override protected synchronized void done() {
        // dispose of canvas frames on the Event Dispatcher Thread...
        if (cameraCanvasFrames != null) {
            for (int i = 0; i < cameraCanvasFrames.length; i++) {
                if (cameraCanvasFrames[i] != null) {
                    cameraCanvasFrames[i].dispose();
                    cameraCanvasFrames[i] = null;
                }
            }
        }
        if (projectorCanvasFrames != null) {
            for (int i = 0; i < projectorCanvasFrames.length; i++) {
                if (projectorCanvasFrames[i] != null) {
                    projectorCanvasFrames[i].dispose();
                    projectorCanvasFrames[i] = null;
                }
            }
        }
    }

    private void calibrateGeometry(FrameGrabber.Array frameGrabberArray) throws Exception {
        // create camera calibrators...
        geometricCalibrators = new GeometricCalibrator[cameraDevices.length];
        proCamGeometricCalibrators = new ProCamGeometricCalibrator[projectorDevices.length];
        for (int i = 0; i < geometricCalibrators.length; i++) {
            geometricCalibrators[i] = new GeometricCalibrator(geometricCalibratorSettings,
                    markerDetectorSettings, boardPlane, cameraDevices[i]);
        }
        // create projector calibrators...
        for (int i = 0; i < proCamGeometricCalibrators.length; i++) {
            GeometricCalibrator g = new GeometricCalibrator(geometricCalibratorSettings,
                    markerDetectorSettings, projectorPlanes[i], projectorDevices[i]);
            proCamGeometricCalibrators[i] = new ProCamGeometricCalibrator(
                    geometricCalibratorSettings, markerDetectorSettings,
                    boardPlane, projectorPlanes[i], geometricCalibrators, g);
        }
        // define the device at origin to be the first camera...
        final int cameraAtOrigin = 0;

        int currentProjector = 0;
        final boolean[] hasDetectedMarkers = new boolean[frameGrabberArray.size()];
        final IplImage[] colorImages = new IplImage[frameGrabberArray.size()];

        boolean done = false;
        long lastAddedTime = -1;
        while (!done && !isCancelled()) {
            // display projector pattern
            if (currentProjector < projectorCanvasFrames.length) {
                projectorCanvasFrames[currentProjector].showImage(
                        proCamGeometricCalibrators[currentProjector].getProjectorImage().
                        // gamma correction
                        getBufferedImage(1.0/projectorDevices[currentProjector].getSettings().getGamma()));
                projectorCanvasFrames[currentProjector].waitLatency();
            }

            // capture images from cameras
            frameGrabberArray.trigger();
            final IplImage[] grabbedImages = frameGrabberArray.grab();
            assert (grabbedImages.length == cameraDevices.length);
            final int curProj = currentProjector;

            for (int i = 0; i < grabbedImages.length; i++) {
                if (grabbedImages[i] == null) {
                    throw new Exception("Image grabbed from " + cameraDevices[i].getSettings().getName() + " is null, unexcepted end of stream?");
                }
            }

//            for (int i = 0; i < grabbedImages.length && !isCancelled(); i++) {
            Parallel.loop(0, grabbedImages.length, new Looper() {
            public void loop(int from, int to, int looperID) {
            for (int i = from; i < to && !isCancelled(); i++) {
                // gamma "uncorrection", linearization
                double gamma = cameraDevices[i].getSettings().getGamma();
                if (gamma != 1.0) {
                    grabbedImages[i].applyGamma(gamma);
                }

                // convert camera image to color so we can draw in color
                if (colorImages[i] == null) {
                    colorImages[i] = IplImage.create(grabbedImages[i].width, grabbedImages[i].height, IPL_DEPTH_8U, 3);
                }
                switch (grabbedImages[i].depth&~IPL_DEPTH_SIGN) {
                    case 8:
                        cvCvtColor(grabbedImages[i], colorImages[i], CV_GRAY2RGB);
                        break;
                    case 16:
                        ByteBuffer colorBuf = colorImages[i].getByteBuffer();
                        ShortBuffer shortBuf = grabbedImages[i].getByteBuffer().asShortBuffer();
                        for (int j = 0; j < colorBuf.limit()/3; j++) {
                            byte msb = (byte)((int)(shortBuf.get()>>8) & 0xFF);
                            colorBuf.put(msb);
                            colorBuf.put(msb);
                            colorBuf.put(msb);
                        }
                        break;
                    default: assert(false);
                }
                // process camera images.. detect markers..
                if (curProj < projectorCanvasFrames.length) {
                    hasDetectedMarkers[i] = proCamGeometricCalibrators[curProj].processCameraImage(grabbedImages[i], i) != null;
                    proCamGeometricCalibrators[curProj].drawMarkers(colorImages[i], i);
                } else {
                    hasDetectedMarkers[i] = geometricCalibrators[i].processImage(grabbedImages[i]) != null;
                    geometricCalibrators[i].drawMarkers(colorImages[i]);
                }
                // show camera images with detected markers drawn
                if (cameraCanvasFrames[i] != null) {
                    cameraCanvasFrames[i].showImage(colorImages[i], cameraSettings.getWindowScale());
                    //cameraCanvasFrames[i].showImage(geometricCalibrators[i].getMarkerDetector().getBinarized());
                }
            }}});

            // check if we have any missing markers from all camera images
            boolean missing = false;
            for (int i = 0; i < hasDetectedMarkers.length; i++) {
                if (!hasDetectedMarkers[i]) {
                    missing = true;
                    break;
                }
            }

            // if we have waited long enough, and all calibrators want to add
            // the markers, then add them
            long time = System.currentTimeMillis();
            if (!missing && time-lastAddedTime > geometricCalibratorSettings.timeIntervalMin) {
                lastAddedTime = time;
                for (int i = 0; i < cameraCanvasFrames.length; i++) {
                    // the calibrators have decided to save these markers, make a little flash effect
                    if (cameraCanvasFrames[i] != null) {
                        cameraCanvasFrames[i].showColor(Color.WHITE);
                    }

                    if (currentProjector < projectorCanvasFrames.length) {
                        // the cameras are calibrated through the current projector...
                        proCamGeometricCalibrators[currentProjector].addMarkers(i);
                        if (proCamGeometricCalibrators[currentProjector].getImageCount()
                                >= geometricCalibratorSettings.imagesInTotal) {
                            projectorCanvasFrames[currentProjector].showColor(Color.BLACK);
                            done = true;
                        }
                    } else {
                        geometricCalibrators[i].addMarkers();
                        if (geometricCalibrators[i].getImageCount()
                                >= geometricCalibratorSettings.imagesInTotal) {
                            done = true;
                        }
                    }
                }
                if (done && currentProjector+1 < proCamGeometricCalibrators.length) {
                    currentProjector++;
                    done = false;
                }
                Thread.sleep(200);
            }
        }

        // once we have accumulated enough markers, proceed to calibration
        if (!isCancelled()) {
            GeometricCalibrator calibratorAtOrigin = geometricCalibrators[cameraAtOrigin];
            for (int i = 0; i < geometricCalibrators.length; i++) {
                geometricCalibrators[i].calibrate(geometricCalibratorSettings.useMarkerCenters);
                if (geometricCalibrators[i] != calibratorAtOrigin) {
                    calibratorAtOrigin.calibrateStereo(
                            geometricCalibratorSettings.useMarkerCenters, geometricCalibrators[i]);
                }
            }
            for (int i = 0; i < proCamGeometricCalibrators.length; i++) {
                proCamGeometricCalibrators[i].calibrate(
                        geometricCalibratorSettings.useMarkerCenters, false, cameraAtOrigin);
            }
        }
    }

    private void calibrateColor(FrameGrabber.Array frameGrabberArray) throws Exception {
        assert(frameGrabberArray.size() == cameraDevices.length);
        if (projectorCanvasFrames.length <= 0) {
            // the calibrator does not work without projectors...
            return;
        }

        // create color calibrators...
        proCamColorCalibrators = new ProCamColorCalibrator[cameraDevices.length][];
        for (int i = 0; i < cameraDevices.length; i++) {
            proCamColorCalibrators[i] = new ProCamColorCalibrator[projectorDevices.length];
            for (int j = 0; j < projectorDevices.length; j++) {
                if (cameraDevices[i].cameraMatrix == null || projectorDevices[j].cameraMatrix == null) {
                    throw new Exception("Color calibration requires prior geometric calibration.");
                }
                proCamColorCalibrators[i][j] = new ProCamColorCalibrator(colorCalibratorSettings,
                        markerDetectorSettings, boardPlane, cameraDevices[i], projectorDevices[j]);
            }
        }

        int currentProjector = 0;
        final boolean[] hasDetectedMarkers = new boolean[frameGrabberArray.size()];
        int count = 0,
            totalColorCount = proCamColorCalibrators[0][currentProjector].getProjectorColors().length;
        boolean done = false;
        while (!done && !isCancelled()) {
            // display projector color
            if (currentProjector < projectorCanvasFrames.length) {
                // projector color in here is gamma corrected
                Color c = proCamColorCalibrators[0][currentProjector].getProjectorColor();
                //System.out.println(c);
                projectorCanvasFrames[currentProjector].showColor(c);
                projectorCanvasFrames[currentProjector].waitLatency();
            }

            // capture images from cameras
            frameGrabberArray.trigger();
            final IplImage[] grabbedImages = frameGrabberArray.grab();
            final int curProj = currentProjector;
            assert (grabbedImages.length == cameraDevices.length);

            for (int i = 0; i < grabbedImages.length; i++) {
                if (grabbedImages[i] == null) {
                    throw new Exception("Image grabbed from " + cameraDevices[i].getSettings().getName() + " is null, unexcepted end of stream?");
                }
            }

            //for (int i = 0; i < grabbedImages.length; i++) {
            Parallel.loop(0, grabbedImages.length, new Looper() {
            public void loop(int from, int to, int looperID) {
            for (int i = from; i < to && !isCancelled(); i++) {
                hasDetectedMarkers[i] = proCamColorCalibrators[i][curProj].processCameraImage(grabbedImages[i]);
            }}});

            // check if we have any missing markers from all camera images
            boolean missing = false;
            for (int i = 0; i < hasDetectedMarkers.length; i++) {
                if (!hasDetectedMarkers[i]) {
                    missing = true;
                    break;
                }
            }

            // if all calibrators want to add the colors, then add them
            if (!missing) {
                for (int i = 0; i < cameraCanvasFrames.length; i++) {
                    // the calibrators have decided to save these markers, 
                    // show the extracted region
                    if (cameraCanvasFrames[i] != null) {
                        // create image for display...
                        IplImage mask = proCamColorCalibrators[i][currentProjector].getMaskImage();
                        IplImage undist = proCamColorCalibrators[i][currentProjector].getUndistortedCameraImage();
                        cvNot(mask, mask);
                        cvSet(undist, cvScalarAll(undist.getMaxIntensity()), mask);
                        cameraCanvasFrames[i].showImage(undist, cameraSettings.getWindowScale());
                    }

                    // add the extracted color
                    proCamColorCalibrators[i][currentProjector].addCameraColor();
                }
                count++;
                if (count >= totalColorCount) {
                    projectorCanvasFrames[currentProjector].showColor(Color.BLACK);
                    done = true;
                }
                if (done && currentProjector+1 < proCamColorCalibrators.length) {
                    currentProjector++;
                    count = 0;
                    totalColorCount = proCamColorCalibrators[0][currentProjector].getProjectorColors().length;
                    done = false;
                }
            } else {
                // show camera images without extracted regions
                for (int i = 0; i < cameraCanvasFrames.length; i++) {
                    if (cameraCanvasFrames[i] != null) {
                        IplImage undist = proCamColorCalibrators[i][currentProjector].getUndistortedCameraImage();
                        cameraCanvasFrames[i].showImage(undist, cameraSettings.getWindowScale());
                    }
                }
            }
        }

        // once we have accumulated enough colors, proceed to calibration
        if (!isCancelled()) {
            Color[] referenceColors = new Color[totalColorCount*projectorDevices.length],
                             colors = new Color[totalColorCount*projectorDevices.length];
            int k = 0;
            double gamma = cameraDevices[0].getSettings().getGamma();
            // calibrate all projectors with first camera
            for (int j = 0; j < projectorDevices.length; j++) {
                for (Color c : proCamColorCalibrators[0][j].getCameraColors()) {
                    float[] cc = c.getRGBColorComponents(null);
                    referenceColors[k++] = new Color((float)Math.pow(cc[0], gamma),
                            (float)Math.pow(cc[1], gamma), (float)Math.pow(cc[2], gamma));
                }
                //System.arraycopy(proCamColorCalibrators[0][j].getCameraColors(), 0,
                //        referenceColors, totalColorCount*j, totalColorCount);
                proCamColorCalibrators[0][j].calibrate();
            }
            // calibrate all the other cameras using reference colors from first camera
            for (int i = 1; i < cameraDevices.length; i++) {
                for (int j = 0; j < projectorDevices.length; j++) {
                    System.arraycopy(proCamColorCalibrators[i][j].getCameraColors(), 0,
                            colors, totalColorCount*j, totalColorCount);
                }
                ColorCalibrator calibrator = new ColorCalibrator(cameraDevices[i]);
                calibrator.calibrate(referenceColors, colors);
            }
        }

    }

    public void readParameters(File file) {
        String f = file.getAbsolutePath();
        cameraDevices    = CameraDevice   .read(f);
        projectorDevices = ProjectorDevice.read(f);
    }
    public void writeParameters(File file) {
        ProjectiveDevice.write(file.getAbsolutePath(), cameraDevices, projectorDevices);
    }
}
