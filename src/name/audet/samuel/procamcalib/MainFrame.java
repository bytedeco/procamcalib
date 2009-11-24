/*
 * Copyright (C) 2009 Samuel Audet 
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

import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultEditorKit;
import name.audet.samuel.javacv.CameraDevice;
import name.audet.samuel.javacv.FrameGrabber;
import name.audet.samuel.javacv.JavaCvErrorCallback;
import name.audet.samuel.javacv.MarkedPlane;
import name.audet.samuel.javacv.Marker;
import name.audet.samuel.javacv.MarkerDetector;
import name.audet.samuel.javacv.ProjectorDevice;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import static name.audet.samuel.javacv.jna.cxcore.*;
import static name.audet.samuel.javacv.jna.cv.*;
import static name.audet.samuel.javacv.jna.highgui.*;

/**
 *
 * @author Samuel Audet
 *
 * Libraries needed from NetBeans' directory:
 *   boot.jar
 *   core.jar
 *   org-openide-actions.jar
 *   org-openide-awt.jar
 *   org-openide-dialogs.jar
 *   org-openide-explorer.jar
 *   org-openide-filesystems.jar
 *   org-openide-modules.jar
 *   org-openide-nodes.jar
 *   org-openide-util.jar
 *   org-netbeans-swing-plaf.jar
 */
public class MainFrame extends javax.swing.JFrame implements
        ExplorerManager.Provider, Lookup.Provider, PropertyChangeListener {

    /** Creates new form MainFrame */
    public MainFrame(String[] args) throws Exception {
        // same as before...
        manager = new ExplorerManager();
        ActionMap map = getRootPane().getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false

        // ...but add e.g.:
        InputMap keys = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");

        // ...and initialization of lookup variable
        lookup = ExplorerUtils.createLookup(manager, map);


        settingsFile = args.length > 0 ? new File(args[0]) : null;
        try {
            Logger.getLogger("").addHandler(globalLoggingHandler);
            cvErrorCallback.redirectError();

            initComponents();
            loadSettings(settingsFile);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                    "Could not load settings from \"" + settingsFile + "\"", ex);
            loadSettings(null);
        } catch (UnsatisfiedLinkError e) {
            throw new Exception(e);
        }

        beanTreeView.requestFocusInWindow();
    }

    Handler globalLoggingHandler = new Handler() {
        {
            setFormatter(new SimpleFormatter());
        }
        @Override public void publish(final LogRecord record) {
            final String title;
            final int messageType;
            if (record.getLevel().equals(Level.SEVERE)) {
                title = "SEVERE Logging Message";
                messageType = JOptionPane.ERROR_MESSAGE;
            } else if (record.getLevel().equals(Level.WARNING)) {
                title = "WARNING Logging Message";
                messageType = JOptionPane.WARNING_MESSAGE;
            } else if (record.getLevel().equals(Level.INFO)) {
                title = "INFO Logging Message";
                messageType = JOptionPane.INFORMATION_MESSAGE;
            } else {
                title = "Tracing Logging Message";
                messageType = JOptionPane.PLAIN_MESSAGE;
            }
            String[] messageLines = getFormatter().format(record).split("\r\n|\r|\n");
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(5, messageLines.length); i++) {
                messageBuilder.append(messageLines[i] + '\n');
            }
            if (messageLines.length > 5) {
                messageBuilder.append("...");
            }
            final String message = messageBuilder.toString();

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            message, title, messageType);
                }
            });
        }
        @Override public void flush() { }
        @Override public void close() throws SecurityException { }
    };

    JavaCvErrorCallback cvErrorCallback = new JavaCvErrorCallback(true, this) {
        @Override public int callback(int status, String func_name, String err_msg,
                String file_name, int line, Pointer userdata) {
            super.callback(status, func_name, err_msg, file_name, line, userdata);
            if (calibrationWorker != null) {
                calibrationWorker.cancel(false);
            }
            return 0; // please don't terminate
        }
    };

    Marker[][] markers = null;
    MarkedPlane boardPlane = null;
    CameraSettings cameraSettings = null;
    ProjectorSettings projectorSettings = null;
    Marker.ArraySettings markerSettings = null;
    MarkerDetector.Settings markerDetectorSettings = null;
    CalibrationWorker.GeometricSettings geometricCalibratorSettings = null;
    CalibrationWorker.ColorSettings colorCalibratorSettings = null;
    final File DEFAULT_SETTINGS_FILE = new File("settings.xml");
    final File DEFAULT_CALIBRATION_FILE = new File("calibration.yaml");
    File settingsFile = null, calibrationFile = null;

    private ExplorerManager manager;
    private Lookup lookup;

    // ...method as before and getLookup
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public Lookup getLookup() {
        return lookup;
    }
    // ...methods as before, but replace componentActivated and
    // componentDeactivated with e.g.:
    @Override public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
    @Override public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updatePatterns(evt);

        ProjectorDevice.Settings[] ps = projectorSettings.toTypedArray();
        if (ps.length <= 0 && colorCalibratorSettings.isEnabled()) {
            JOptionPane.showMessageDialog(this,
                    "Color calibration requires a projector.",
                    "Cannot enable color calibration",
                    JOptionPane.WARNING_MESSAGE);
            colorCalibratorSettings.setEnabled(false);
            return;
        }
    }

    boolean ignoreNextUpdate = false;
    void updatePatterns(PropertyChangeEvent evt) {
        ProjectorDevice.Settings[] ps = projectorSettings.toTypedArray();
        if (evt != null) {
            Object o = evt.getSource();
            if (o != projectorSettings && o != markerSettings &&
                    (ps.length == 0 || o != ps[0])) {
                return;
            }
        }

        if (ignoreNextUpdate) {
            ignoreNextUpdate = false;
            return;
        }

        if (ps.length > 0 && !markerSettings.isCheckered()) {
            JOptionPane.showMessageDialog(this,
                    "Projector calibration requires checkered patterns.",
                    "Cannot unset \"checkered\" property",
                    JOptionPane.WARNING_MESSAGE);
            if (evt == null || evt.getPropertyName().equals("checkered")) {
                ignoreNextUpdate = true;
            }
            markerSettings.setCheckered(true);
            return;
        }

        double margin = Math.max(0.0, (markerSettings.getSpacing()-markerSettings.getSize())/2);
        markers = Marker.createArray(markerSettings, margin, margin);
        double width = (markerSettings.getColumns()-1)*markerSettings.getSpacing() + markerSettings.getSize() + 2*margin;
        double height = (markerSettings.getRows()-1)*markerSettings.getSpacing() + markerSettings.getSize() + 2*margin;
        boardPlane = new MarkedPlane((int)Math.ceil(width), (int)Math.ceil(height), markers[0], 1);
        IplImage image = boardPlane.getImage();

        int iconHeight = Toolkit.getDefaultToolkit().getScreenSize().height/10;
        IplImage smallImage = IplImage.create(image.width*iconHeight/image.height, iconHeight, IPL_DEPTH_8U, 1);
        cvResize(image, smallImage, CV_INTER_AREA);
        boardPatternLabel.setText("Board (" + boardPlane.getWidth() + " x " + boardPlane.getHeight() + ")");
        boardPatternLabel.setIcon(new ImageIcon(smallImage.getBufferedImage()));

        projectorPatternLabel.setText("No Projector");
        projectorPatternLabel.setIcon(null);
        projectorPatternLabel.setEnabled(false);
        for (int i = 0; i < ps.length; i++) {
            int w = ps[i].getImageWidth();
            int h = ps[i].getImageHeight();
            if (w > 0 && h > 0) {
                MarkedPlane proj = new MarkedPlane(w, h, markers[1], true,
                        cvScalarAll(ps[i].getBrightnessForeground()*255),
                        cvScalarAll(ps[i].getBrightnessBackground()*255), 4);
                image = proj.getImage();
                smallImage = IplImage.create(image.width*iconHeight/image.height, iconHeight, IPL_DEPTH_8U, 1);
                cvResize(image, smallImage, CV_INTER_AREA);
                projectorPatternLabel.setText(ps[i].getName() + " (" + proj.getWidth() + " x " + proj.getHeight() + ")");
                projectorPatternLabel.setIcon(new ImageIcon(smallImage.getBufferedImage()));
                projectorPatternLabel.setEnabled(true);
                break;
            }
        }
    }

    void buildSettingsView() throws IntrospectionException {
        HashMap<String, Class<? extends PropertyEditor>> editors =
                new HashMap<String, Class<? extends PropertyEditor>>();
        editors.put("frameGrabber", FrameGrabberPropertyEditor.class);

        // hide settings we do not need from the user...
        editors.put("triggerMode", null);
        editors.put("colorMode", null);
        editors.put("timeout", null);
        editors.put("numBuffers", null);

        if (cameraSettings == null) {
            cameraSettings = new CameraSettings();
            cameraSettings.setQuantity(1);
        }
        cameraSettings.addPropertyChangeListener(this);
        BeanNode cameraNode = new CleanBeanNode<CameraSettings>
                (cameraSettings, editors, "Cameras");

        if (projectorSettings == null) {
            projectorSettings = new ProjectorSettings();
            projectorSettings.setQuantity(1);
        }
        projectorSettings.addPropertyChangeListener(this);
        BeanNode projectorNode = new CleanBeanNode<ProjectorSettings>
                (projectorSettings, editors, "Projectors");

        if (markerSettings == null) {
            markerSettings = new Marker.ArraySettings();
        }
        markerSettings.addPropertyChangeListener(this);
        BeanNode markerNode = new CleanBeanNode<Marker.ArraySettings>
                (markerSettings, "MarkerPatterns");

        if (markerDetectorSettings == null) {
            markerDetectorSettings = new MarkerDetector.Settings();
        }
        BeanNode detectorNode = new CleanBeanNode<MarkerDetector.Settings>
                (markerDetectorSettings, "MarkerDetector");

        if (geometricCalibratorSettings == null) {
            geometricCalibratorSettings = new CalibrationWorker.GeometricSettings();
        }
        BeanNode geometricCalibratorNode = new CleanBeanNode<CalibrationWorker.GeometricSettings>
                (geometricCalibratorSettings, "GeometricCalibrator");

        if (colorCalibratorSettings == null) {
            colorCalibratorSettings = new CalibrationWorker.ColorSettings();
        }
        colorCalibratorSettings.addPropertyChangeListener(this);
        BeanNode colorCalibratorNode = new CleanBeanNode<CalibrationWorker.ColorSettings>
                (colorCalibratorSettings, "ColorCalibrator");

        Children children = new Children.Array();
        children.add(new Node[] { cameraNode, projectorNode, markerNode, detectorNode, 
                geometricCalibratorNode, colorCalibratorNode });

        Node root = new AbstractNode(children);
        root.setName("Settings");
        manager.setRootContext(root);
    }

    void loadSettings(File file) throws IOException, IntrospectionException {
        if (file == null) {
            cameraSettings = null;
            projectorSettings = null;
            markerSettings = null;
            markerDetectorSettings = null;
            geometricCalibratorSettings = null;
            colorCalibratorSettings = null;
            calibrationFile = null;
            calibrationWorker = null;
        } else {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
            cameraSettings = (CameraSettings)decoder.readObject();
            projectorSettings = (ProjectorSettings)decoder.readObject();
            markerSettings = (Marker.ArraySettings)decoder.readObject();
            markerDetectorSettings = (MarkerDetector.Settings)decoder.readObject();
            geometricCalibratorSettings = (CalibrationWorker.GeometricSettings)decoder.readObject();
            colorCalibratorSettings = (CalibrationWorker.ColorSettings)decoder.readObject();
            try {
                String s = (String)decoder.readObject();
                calibrationFile = s == null ? null : new File(s);
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) { }
            decoder.close();
        }

        settingsFile = file;
        if (settingsFile == null) {
            setTitle("ProCamCalib");
        } else {
            setTitle(settingsFile.getName() + " - ProCamCalib");
        }

        buildSettingsView();
        updatePatterns(null);

        if (calibrationWorker == null) {
            statusLabel.setText("No calibration data.");
        }
    }

    void saveSettings(File file) throws IOException {
        settingsFile = file;
        if (settingsFile == null) {
            setTitle("ProCamCalib");
        } else {
            setTitle(settingsFile.getName() + " - ProCamCalib");
        }

        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(settingsFile)));
        encoder.writeObject(cameraSettings);
        encoder.writeObject(projectorSettings);
        encoder.writeObject(markerSettings);
        encoder.writeObject(markerDetectorSettings);
        encoder.writeObject(geometricCalibratorSettings);
        encoder.writeObject(colorCalibratorSettings);
        if (calibrationFile != null) {
            encoder.writeObject(calibrationFile.getAbsolutePath());
        }
        encoder.close();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        splitPane = new javax.swing.JSplitPane();
        beanTreeView = new org.openide.explorer.view.BeanTreeView();
        propertySheetView = new name.audet.samuel.procamcalib.FixedPropertySheetView();
        toolBar = new javax.swing.JToolBar();
        settingsLoadDefaultsButton = new javax.swing.JButton();
        settingsLoadButton = new javax.swing.JButton();
        settingsSaveButton = new javax.swing.JButton();
        toolBarSeparator1 = new javax.swing.JToolBar.Separator();
        calibrationStartButton = new javax.swing.JButton();
        calibrationStopButton = new javax.swing.JButton();
        calibrationExamineButton = new javax.swing.JButton();
        calibrationLoadButton = new javax.swing.JButton();
        calibrationSaveButton = new javax.swing.JButton();
        patternsPanel = new javax.swing.JPanel();
        boardPatternLabel = new javax.swing.JLabel();
        projectorPatternLabel = new javax.swing.JLabel();
        saveAsBoardPatternButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        settingsMenu = new javax.swing.JMenu();
        settingsLoadDefaultsMenuItem = new javax.swing.JMenuItem();
        settingsLoadMenuItem = new javax.swing.JMenuItem();
        settingsSaveMenuItem = new javax.swing.JMenuItem();
        settingsSaveAsMenuItem = new javax.swing.JMenuItem();
        calibrationMenu = new javax.swing.JMenu();
        calibrationStartMenuItem = new javax.swing.JMenuItem();
        calibrationStopMenuItem = new javax.swing.JMenuItem();
        calibrationExamineMenuItem = new javax.swing.JMenuItem();
        menuSeparator1 = new javax.swing.JSeparator();
        calibrationLoadMenuItem = new javax.swing.JMenuItem();
        calibrationSaveMenuItem = new javax.swing.JMenuItem();
        calibrationSaveAsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        readmeMenuItem = new javax.swing.JMenuItem();
        menuSeparator2 = new javax.swing.JSeparator();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ProCamCalib");

        splitPane.setResizeWeight(0.5);

        beanTreeView.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        splitPane.setLeftComponent(beanTreeView);

        propertySheetView.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        propertySheetView.setDescriptionAreaVisible(false);
        try {
            propertySheetView.setSortingMode(PropertySheetView.SORTED_BY_NAMES);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }
        splitPane.setRightComponent(propertySheetView);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        settingsLoadDefaultsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/cleanCurrentProject.gif"))); // NOI18N
        settingsLoadDefaultsButton.setToolTipText("Load Defaults");
        settingsLoadDefaultsButton.setFocusable(false);
        settingsLoadDefaultsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsLoadDefaultsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        settingsLoadDefaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsLoadDefaultsButtonActionPerformed(evt);
            }
        });
        toolBar.add(settingsLoadDefaultsButton);

        settingsLoadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/openProject.png"))); // NOI18N
        settingsLoadButton.setToolTipText("Load Settings");
        settingsLoadButton.setFocusable(false);
        settingsLoadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsLoadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        settingsLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsLoadButtonActionPerformed(evt);
            }
        });
        toolBar.add(settingsLoadButton);

        settingsSaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/save.png"))); // NOI18N
        settingsSaveButton.setToolTipText("Save Settings");
        settingsSaveButton.setFocusable(false);
        settingsSaveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsSaveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        settingsSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsSaveButtonActionPerformed(evt);
            }
        });
        toolBar.add(settingsSaveButton);
        toolBar.add(toolBarSeparator1);

        calibrationStartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/runProject.png"))); // NOI18N
        calibrationStartButton.setToolTipText("Start Calibration");
        calibrationStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationStartButtonActionPerformed(evt);
            }
        });
        toolBar.add(calibrationStartButton);

        calibrationStopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/stop.png"))); // NOI18N
        calibrationStopButton.setToolTipText("Stop Calibration");
        calibrationStopButton.setEnabled(false);
        calibrationStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationStopButtonActionPerformed(evt);
            }
        });
        toolBar.add(calibrationStopButton);

        calibrationExamineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/findusages.png"))); // NOI18N
        calibrationExamineButton.setToolTipText("Examine Calibration");
        calibrationExamineButton.setFocusable(false);
        calibrationExamineButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calibrationExamineButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calibrationExamineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationExamineButtonActionPerformed(evt);
            }
        });
        toolBar.add(calibrationExamineButton);

        calibrationLoadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/openFile.png"))); // NOI18N
        calibrationLoadButton.setToolTipText("Load Calibration");
        calibrationLoadButton.setFocusable(false);
        calibrationLoadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calibrationLoadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calibrationLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationLoadButtonActionPerformed(evt);
            }
        });
        toolBar.add(calibrationLoadButton);

        calibrationSaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/name/audet/samuel/procamcalib/icons/save_results.png"))); // NOI18N
        calibrationSaveButton.setToolTipText("Save Calibration");
        calibrationSaveButton.setFocusable(false);
        calibrationSaveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calibrationSaveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calibrationSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationSaveButtonActionPerformed(evt);
            }
        });
        toolBar.add(calibrationSaveButton);

        patternsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Patterns"));
        patternsPanel.setLayout(new java.awt.GridBagLayout());

        boardPatternLabel.setText("Board");
        boardPatternLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        boardPatternLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
        patternsPanel.add(boardPatternLabel, gridBagConstraints);

        projectorPatternLabel.setText("Projector");
        projectorPatternLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        projectorPatternLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
        patternsPanel.add(projectorPatternLabel, gridBagConstraints);

        saveAsBoardPatternButton.setMnemonic('S');
        saveAsBoardPatternButton.setText("Save As...");
        saveAsBoardPatternButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsBoardPatternButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        patternsPanel.add(saveAsBoardPatternButton, gridBagConstraints);

        statusLabel.setText("Status");
        statusLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));

        settingsMenu.setMnemonic('E');
        settingsMenu.setText("Settings");

        settingsLoadDefaultsMenuItem.setMnemonic('D');
        settingsLoadDefaultsMenuItem.setText("Load Defaults");
        settingsLoadDefaultsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsLoadDefaultsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(settingsLoadDefaultsMenuItem);

        settingsLoadMenuItem.setMnemonic('L');
        settingsLoadMenuItem.setText("Load...");
        settingsLoadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsLoadMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(settingsLoadMenuItem);

        settingsSaveMenuItem.setMnemonic('S');
        settingsSaveMenuItem.setText("Save");
        settingsSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsSaveMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(settingsSaveMenuItem);

        settingsSaveAsMenuItem.setMnemonic('A');
        settingsSaveAsMenuItem.setText("Save As...");
        settingsSaveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsSaveAsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(settingsSaveAsMenuItem);

        menuBar.add(settingsMenu);

        calibrationMenu.setMnemonic('C');
        calibrationMenu.setText("Calibration");

        calibrationStartMenuItem.setMnemonic('T');
        calibrationStartMenuItem.setText("Start");
        calibrationStartMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationStartMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationStartMenuItem);

        calibrationStopMenuItem.setMnemonic('O');
        calibrationStopMenuItem.setText("Stop");
        calibrationStopMenuItem.setEnabled(false);
        calibrationStopMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationStopMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationStopMenuItem);

        calibrationExamineMenuItem.setMnemonic('E');
        calibrationExamineMenuItem.setText("Examine");
        calibrationExamineMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationExamineMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationExamineMenuItem);
        calibrationMenu.add(menuSeparator1);

        calibrationLoadMenuItem.setMnemonic('L');
        calibrationLoadMenuItem.setText("Load...");
        calibrationLoadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationLoadMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationLoadMenuItem);

        calibrationSaveMenuItem.setMnemonic('S');
        calibrationSaveMenuItem.setText("Save");
        calibrationSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationSaveMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationSaveMenuItem);

        calibrationSaveAsMenuItem.setMnemonic('A');
        calibrationSaveAsMenuItem.setText("Save As...");
        calibrationSaveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrationSaveAsMenuItemActionPerformed(evt);
            }
        });
        calibrationMenu.add(calibrationSaveAsMenuItem);

        menuBar.add(calibrationMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        readmeMenuItem.setMnemonic('R');
        readmeMenuItem.setText("README");
        readmeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readmeMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(readmeMenuItem);
        helpMenu.add(menuSeparator2);

        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(splitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(patternsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(patternsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void settingsLoadDefaultsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsLoadDefaultsMenuItemActionPerformed
        if (evt != null) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Load defaults settings and lose calibration data?", "Confirm Reset",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        try {
            loadSettings(null);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_settingsLoadDefaultsMenuItemActionPerformed

    private void settingsLoadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsLoadMenuItemActionPerformed
        JFileChooser fc = new JFileChooser();
        if (settingsFile != null) {
            fc.setSelectedFile(settingsFile);
        } else {
            fc.setSelectedFile(DEFAULT_SETTINGS_FILE);
        }
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                loadSettings(file);
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Could not load settings from \"" + file + "\"", ex);
            }
        }

    }//GEN-LAST:event_settingsLoadMenuItemActionPerformed

    private void settingsSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsSaveMenuItemActionPerformed
        if (settingsFile == null) {
            settingsSaveAsMenuItemActionPerformed(evt);
        } else {
            if (settingsFile.exists()) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing file \"" + settingsFile + "\"?", "Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) {
                    settingsSaveAsMenuItemActionPerformed(evt);
                    return;
                }
            }

            try {
                saveSettings(settingsFile);
            } catch (IOException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Could not save settings to \"" + settingsFile + "\"", ex);
            }
        }
    }//GEN-LAST:event_settingsSaveMenuItemActionPerformed

    private void settingsSaveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsSaveAsMenuItemActionPerformed
        JFileChooser fc = new JFileChooser();
        if (settingsFile != null) {
            fc.setSelectedFile(settingsFile);
        } else {
            fc.setSelectedFile(DEFAULT_SETTINGS_FILE);
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            settingsFile = fc.getSelectedFile();
            settingsSaveMenuItemActionPerformed(evt);
        }

    }//GEN-LAST:event_settingsSaveAsMenuItemActionPerformed

    class MyCalibrationWorker extends CalibrationWorker {
        public MyCalibrationWorker(MyCalibrationWorker brother) {
            super();
            if (brother != null) {
                this.cameraDevices = brother.cameraDevices;
                this.projectorDevices = brother.projectorDevices;
            }
        }

        @Override protected void done() {
            super.done();

            settingsMenu.setEnabled(true);
//            calibrationMenu.setEnabled(true);
            calibrationStartMenuItem.setEnabled(true);
            calibrationStopMenuItem.setEnabled(false);
            calibrationExamineMenuItem.setEnabled(true);
            calibrationLoadMenuItem.setEnabled(true);
            calibrationSaveMenuItem.setEnabled(true);
            calibrationSaveAsMenuItem.setEnabled(true);
            settingsLoadDefaultsButton.setEnabled(true);
            settingsLoadButton.setEnabled(true);
            settingsSaveButton.setEnabled(true);
            calibrationExamineButton.setEnabled(true);
            calibrationStartButton.setEnabled(true);
            calibrationStopButton.setEnabled(false);
            calibrationLoadButton.setEnabled(true);
            calibrationSaveButton.setEnabled(true);
            beanTreeView.setEnabled(true);
            propertySheetView.setEnabled(true);
            saveAsBoardPatternButton.setEnabled(true);

            if (isCancelled()) {
                statusLabel.setText("Calibration cancelled.");
                calibrationWorker = null;
            } else {
                statusLabel.setText("Calibration done!");
            }
        }
    }
    MyCalibrationWorker calibrationWorker = null;

    private void calibrationStartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationStartMenuItemActionPerformed
        if (calibrationWorker == null || calibrationWorker.getState() != StateValue.STARTED) {
            calibrationWorker = new MyCalibrationWorker(calibrationWorker);

            settingsMenu.setEnabled(false);
//            calibrationMenu.setEnabled(false);
            calibrationStartMenuItem.setEnabled(false);
            calibrationStopMenuItem.setEnabled(true);
            calibrationExamineMenuItem.setEnabled(false);
            calibrationLoadMenuItem.setEnabled(false);
            calibrationSaveMenuItem.setEnabled(false);
            calibrationSaveAsMenuItem.setEnabled(false);
            settingsLoadDefaultsButton.setEnabled(false);
            settingsLoadButton.setEnabled(false);
            settingsSaveButton.setEnabled(false);
            calibrationExamineButton.setEnabled(false);
            calibrationStartButton.setEnabled(false);
            calibrationStopButton.setEnabled(true);
            calibrationLoadButton.setEnabled(false);
            calibrationSaveButton.setEnabled(false);
            beanTreeView.setEnabled(false);
            propertySheetView.setEnabled(false);
            saveAsBoardPatternButton.setEnabled(false);

            try {
                // PropertySheetView doesn't actually disable, so let's
                // select the dummy root node...
                manager.setSelectedNodes(new Node[]{manager.getRootContext()});
            } catch (Exception ex) { }

            calibrationWorker.markers = markers;
            calibrationWorker.boardPlane = boardPlane;
            calibrationWorker.cameraSettings = cameraSettings;
            calibrationWorker.projectorSettings = projectorSettings;
            calibrationWorker.markerSettings = markerSettings;
            calibrationWorker.markerDetectorSettings = markerDetectorSettings;
            calibrationWorker.geometricCalibratorSettings = geometricCalibratorSettings;
            calibrationWorker.colorCalibratorSettings = colorCalibratorSettings;

            try {
                calibrationWorker.init();
                calibrationWorker.execute();

                statusLabel.setText("Calibrating...");
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Could not initialize calibration worker thread.", ex);
                calibrationWorker.cancel(false);
            }
        } else {
            assert(false);
        }
    }//GEN-LAST:event_calibrationStartMenuItemActionPerformed

    private void calibrationStopMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationStopMenuItemActionPerformed
        if (calibrationWorker != null) {
            calibrationWorker.cancel(false);
        } else {
            assert(false);
        }
    }//GEN-LAST:event_calibrationStopMenuItemActionPerformed

    private void calibrationExamineMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationExamineMenuItemActionPerformed
        if (calibrationWorker == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no calibration data to examine.",
                    "No calibration data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextArea textArea = new JTextArea();
        Font font = textArea.getFont();
        textArea.setFont(new Font("Monospaced", font.getStyle(), font.getSize()));
        textArea.setEditable(false);

        String text = "";
        for (CameraDevice d : calibrationWorker.cameraDevices) {
            if (text.length() > 0) {
                text += "\n\n\n";
            }
            text += d;
        }
        for (ProjectorDevice d : calibrationWorker.projectorDevices) {
            text += "\n\n\n" + d;
        }

        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.setColumns(80);

        // stuff it in a scrollpane with a controlled size.
        JScrollPane scrollPane = new JScrollPane(textArea);
        Dimension dim = textArea.getPreferredSize();
        dim.height = dim.width*50/80;
        scrollPane.setPreferredSize(dim);

        // pass the scrollpane to the joptionpane.
        JDialog dialog = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE).
                createDialog(this, "Calibration Data");
        dialog.setResizable(true);
        dialog.setModalityType(ModalityType.MODELESS);
        dialog.setVisible(true);
    }//GEN-LAST:event_calibrationExamineMenuItemActionPerformed

    private void calibrationLoadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationLoadMenuItemActionPerformed
        JFileChooser fc = new JFileChooser();
        if (calibrationFile != null) {
            fc.setSelectedFile(calibrationFile);
        } else {
            fc.setSelectedFile(DEFAULT_CALIBRATION_FILE);
        }
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                if (!file.exists()) {
                    throw new Exception("File does not exist.");
                }

                if (calibrationWorker == null) {
                    calibrationWorker = new MyCalibrationWorker(null);
                }
                calibrationWorker.readParameters(file);
                calibrationFile = file;
                statusLabel.setText("Calibration data loaded.");
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Could not load calibration data from \"" + file + "\"", ex);
            }
        }

    }//GEN-LAST:event_calibrationLoadMenuItemActionPerformed

    private void calibrationSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationSaveMenuItemActionPerformed
        if (calibrationWorker == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no calibration data to save.",
                    "No calibration data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (calibrationFile == null) {
            calibrationSaveAsMenuItemActionPerformed(evt);
        } else {
            if (calibrationFile.exists()) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing file \"" + calibrationFile + "\"?", "Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) {
                    calibrationSaveAsMenuItemActionPerformed(evt);
                    return;
                }
            }
            calibrationWorker.writeParameters(calibrationFile);
            statusLabel.setText("Calibration data saved.");
        }

    }//GEN-LAST:event_calibrationSaveMenuItemActionPerformed

    private void calibrationSaveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationSaveAsMenuItemActionPerformed
        if (calibrationWorker == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no calibration data to save.",
                    "No calibration data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        if (calibrationFile != null) {
            fc.setSelectedFile(calibrationFile);
        } else {
            fc.setSelectedFile(DEFAULT_CALIBRATION_FILE);
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            calibrationFile = fc.getSelectedFile();
            calibrationSaveMenuItemActionPerformed(evt);
        }
    }//GEN-LAST:event_calibrationSaveAsMenuItemActionPerformed

    private void readmeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readmeMenuItemActionPerformed
        try {
            JTextArea textArea = new JTextArea();
            Font font = textArea.getFont();
            textArea.setFont(new Font("Monospaced", font.getStyle(), font.getSize()));
            textArea.setEditable(false);

            String text = "";
            BufferedReader r = new BufferedReader(new FileReader(
                    myDirectory + File.separator + "README.txt"));
            String line;
            while ((line = r.readLine()) != null) {
                text += line + '\n';
            }

            textArea.setText(text);
            textArea.setCaretPosition(0);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setColumns(80);

            // stuff it in a scrollpane with a controlled size.
            JScrollPane scrollPane = new JScrollPane(textArea);
            Dimension dim = textArea.getPreferredSize();
            dim.height = dim.width*50/80;
            scrollPane.setPreferredSize(dim);

            // pass the scrollpane to the joptionpane.
            JDialog dialog = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE).
                    createDialog(this, "README");
            dialog.setResizable(true);
            dialog.setModalityType(ModalityType.MODELESS);
            dialog.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_readmeMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        String timestamp = "unknown";
        try {
            URL u = MainFrame.class.getClassLoader().getResource("name/audet/samuel/procamcalib");
            JarURLConnection c = (JarURLConnection)u.openConnection();
            timestamp = c.getManifest().getMainAttributes().getValue("Time-Stamp");
        } catch (Exception e) { }

        JTextPane textPane = new JTextPane();
	textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setText(
                "<font face=sans-serif><strong><font size=+2>ProCamCalib</font></strong><br>" +
                "build timestamp " + timestamp + "<br>" +
                "Copyright (C) 2009 Samuel Audet &lt;<a href=\"mailto:saudet@ok.ctrl.titech.ac.jp%28Samuel%20Audet%29\">saudet@ok.ctrl.titech.ac.jp</a>&gt;<br>" +
                "Web site: <a href=\"http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/\">http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/</a><br>" +
                "<br>" +
                "Licensed under the GNU General Public License version 2 (GPLv2).<br>" +
                "Please refer to LICENSE.txt or <a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a> for details."
                );
        textPane.setCaretPosition(0);
        Dimension dim = textPane.getPreferredSize();
        dim.height = dim.width*3/4;
        textPane.setPreferredSize(dim);

        textPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch(Exception ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                                "Could not launch browser to \"" + e.getURL()+ "\"", ex);
                    }
                }
            }
        });

        // pass the scrollpane to the joptionpane.
        JDialog dialog = new JOptionPane(textPane, JOptionPane.PLAIN_MESSAGE).
                createDialog(this, "About");

        if (UIManager.getLookAndFeel().getClass().getName()
                .equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
            // under GTK, frameBackground is white, but rootPane color is OK...
            // but under Windows, the rootPane's color is funny...
            Color c = dialog.getRootPane().getBackground();
            textPane.setBackground(new Color(c.getRGB()));
        } else {
            Color frameBackground = this.getBackground();
            textPane.setBackground(frameBackground);
        }
        dialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void settingsLoadDefaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsLoadDefaultsButtonActionPerformed
        settingsLoadDefaultsMenuItemActionPerformed(evt);
    }//GEN-LAST:event_settingsLoadDefaultsButtonActionPerformed

    private void settingsLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsLoadButtonActionPerformed
        settingsLoadMenuItemActionPerformed(evt);
    }//GEN-LAST:event_settingsLoadButtonActionPerformed

    private void settingsSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsSaveButtonActionPerformed
        settingsSaveMenuItemActionPerformed(evt);
    }//GEN-LAST:event_settingsSaveButtonActionPerformed

    private void calibrationStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationStartButtonActionPerformed
        calibrationStartMenuItemActionPerformed(evt);
    }//GEN-LAST:event_calibrationStartButtonActionPerformed

    private void calibrationStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationStopButtonActionPerformed
        calibrationStopMenuItemActionPerformed(evt);
    }//GEN-LAST:event_calibrationStopButtonActionPerformed

    private void calibrationExamineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationExamineButtonActionPerformed
        calibrationExamineMenuItemActionPerformed(evt);
    }//GEN-LAST:event_calibrationExamineButtonActionPerformed

    private void calibrationLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationLoadButtonActionPerformed
        calibrationLoadMenuItemActionPerformed(evt);
    }//GEN-LAST:event_calibrationLoadButtonActionPerformed

    private void calibrationSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrationSaveButtonActionPerformed
        calibrationSaveMenuItemActionPerformed(evt);
    }//GEN-LAST:event_calibrationSaveButtonActionPerformed

    private void saveAsBoardPatternButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsBoardPatternButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("board.png"));
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing file \"" + file + "\"?", "Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            IplImage image = boardPlane.getImage();
            if (cvSaveImage(file.getAbsolutePath(), image) == 0) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                        "Could not save board pattern into \"" + file + "\"");
            }
        }
    }//GEN-LAST:event_saveAsBoardPatternButtonActionPerformed

    static File myDirectory;

    /**
    * @param args the command line arguments
    */
    public static void main(final String args[]) {
        // try to init all frame grabbers here, because bad things
        // happen if loading errors occur while we're in the GUI thread...
        FrameGrabber.init();

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    myDirectory = new File(MainFrame.class.getProtectionDomain().
                            getCodeSource().getLocation().toURI());
                    if (!myDirectory.isDirectory()) {
                        myDirectory = myDirectory.getParentFile();
                    }
                    String path = myDirectory.getAbsolutePath();
                    if (Platform.isLinux()) {
                        if (Platform.is64Bit()) {
                            NativeLibrary.addSearchPath("ARToolKitPlus", path + "/lib/linux-amd64/");
                        } else {
                            NativeLibrary.addSearchPath("ARToolKitPlus", path + "/lib/linux-i686/");
                        }
                    } else if (Platform.isWindows()) {
                        if (Platform.is64Bit()) {
                            NativeLibrary.addSearchPath("ARToolKitPlus", path + "/lib/windows-amd64/");
                        } else {
                            NativeLibrary.addSearchPath("ARToolKitPlus", path + "/lib/windows-i686/");
                        }
                    }

                    String lafClassName = UIManager.getSystemLookAndFeelClassName();
                    ArrayList<String> otherArgs = new ArrayList<String>();
                    for (int i = 0; i < args.length; i++) {
                        if (args[i].equals("--laf") && i+1 < args.length) {
                            lafClassName = args[i+1];
                            i++;
                        } else {
                            otherArgs.add(args[i]);
                        }
                    }
                    // "Ocean Look" would be javax.swing.plaf.metal.MetalLookAndFeel
                    org.netbeans.swing.plaf.Startup.run(Class.forName(lafClassName), 0, null);

                    //Make sure we have nice window decorations.
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);

                    MainFrame w = new MainFrame(otherArgs.toArray(new String[0]));
                    w.setLocationByPlatform(true);
                    w.setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                            "Could not start ProCamCalib", ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private org.openide.explorer.view.BeanTreeView beanTreeView;
    private javax.swing.JLabel boardPatternLabel;
    private javax.swing.JButton calibrationExamineButton;
    private javax.swing.JMenuItem calibrationExamineMenuItem;
    private javax.swing.JButton calibrationLoadButton;
    private javax.swing.JMenuItem calibrationLoadMenuItem;
    private javax.swing.JMenu calibrationMenu;
    private javax.swing.JMenuItem calibrationSaveAsMenuItem;
    private javax.swing.JButton calibrationSaveButton;
    private javax.swing.JMenuItem calibrationSaveMenuItem;
    private javax.swing.JButton calibrationStartButton;
    private javax.swing.JMenuItem calibrationStartMenuItem;
    private javax.swing.JButton calibrationStopButton;
    private javax.swing.JMenuItem calibrationStopMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JSeparator menuSeparator1;
    private javax.swing.JSeparator menuSeparator2;
    private javax.swing.JPanel patternsPanel;
    private javax.swing.JLabel projectorPatternLabel;
    private name.audet.samuel.procamcalib.FixedPropertySheetView propertySheetView;
    private javax.swing.JMenuItem readmeMenuItem;
    private javax.swing.JButton saveAsBoardPatternButton;
    private javax.swing.JButton settingsLoadButton;
    private javax.swing.JButton settingsLoadDefaultsButton;
    private javax.swing.JMenuItem settingsLoadDefaultsMenuItem;
    private javax.swing.JMenuItem settingsLoadMenuItem;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JMenuItem settingsSaveAsMenuItem;
    private javax.swing.JButton settingsSaveButton;
    private javax.swing.JMenuItem settingsSaveMenuItem;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar.Separator toolBarSeparator1;
    // End of variables declaration//GEN-END:variables

}
