
### January 16, 2018 version 1.4
 * Upgrade support to OpenCV 3.4.0 and FFmpeg 3.4.x

### December 7, 2016 version 1.3
 * Work around stability issues on some systems ([issue #4](https://github.com/bytedeco/procamcalib/issues/4))
 * Add support for the `linux-armhf` and `linux-ppc64le` platforms, libdc1394 on Windows, and librealsense on Linux x86
 * Upgrade support to FFmpeg 3.2.x
 * Fix Javadoc links for externally referenced classes

### May 15, 2016 version 1.2
 * Upgrade support to OpenCV 3.1.0 and FFmpeg 3.0.x
 * Lower Maven prerequisite in the `pom.xml` file to 3.0 ([issue bytedeco/javacpp#93](https://github.com/bytedeco/javacpp/issues/93))

### October 25, 2015 version 1.1
 * Upgrade support to FFmpeg 2.8.x
 * Upgrade all Maven dependencies and plugins to latest versions, thus bumping minimum requirements to Java SE 7 and Maven 3.0

### July 11, 2015 version 1.0
 * Upgrade support to OpenCV 3.0.0 and FFmpeg 2.7.x

### April 4, 2015 version 0.11
 * Adjust code to support latest changes in JavaCV
 * Upgrade support to OpenCV 2.4.11 and FFmpeg 2.6.x

### December 23, 2014 version 0.10
 * Upgrade support to OpenCV 2.4.10, FFmpeg 2.5.x, and FlyCapture 2.7

### July 27, 2014 version 0.9
 * Upgrade support to FFmpeg 2.3.x
 * Remove `platform` property from `pom.xml`, replaced with the `platform.dependency` one in JavaCPP Presets ([javacv issue #10](https://github.com/bytedeco/javacv/issues/10))

### April 28, 2014 version 0.8
 * Move from Google Code to GitHub as main source code repository
 * Upgrade support to OpenCV 2.4.9 and FFmpeg 2.2.x
 * Upgrade to NetBeans 8.0 and work around conflict between `opencv_highgui` and `com.sun.java.swing.plaf.gtk.GTKLookAndFeel`
 * Rename the `com.googlecode.javacv.procamcalib` package to `org.bytedeco.procamcalib`
 * Removed old NetBeans project files that cause a conflict when trying to open as a Maven project (issue javacv:210)

### January 6, 2014 version 0.7
 * Upgraded support to OpenCV 2.4.8 and FFmpeg 2.1.x
 * `VideoInputFrameGrabber` now uses 640x480 as default image size to prevent "videoInput.getPixels() Error: Could not get pixels."

### September 15, 2013 version 0.6
 * Upgraded support to OpenCV 2.4.6.x and FFmpeg 2.0.x
 * Upgraded to NetBeans 7.3.1
 * Upgraded to ARToolKitPlus 2.3.0 (issue javacv:234)
 * Fixed drawing issues with `MarkerDetector.draw()`

### April 7, 2013 version 0.5
 * Upgraded support to OpenCV 2.4.5 and FFmpeg 1.2

### March 3, 2013 version 0.4
 * Upgraded support to OpenCV 2.4.4 and FFmpeg 1.1

### November 4, 2012 version 0.3
 * Upgraded support to OpenCV 2.4.3 and FFmpeg 1.0

### July 21, 2012 version 0.2
 * Upgraded support to OpenCV 2.4.2 and FFmpeg 0.11

### May 27, 2012 version 0.1
 * Started using version numbers, friendly to tools like Maven, and placing packages in a sort of [Maven repository](http://maven2.javacv.googlecode.com/git/)

### May 12, 2012
 * Upgraded support to OpenCV 2.4.0
 * Added `pom.xml` and assembly files for Maven support and changed the directory structure of the source code to match Maven's standard directory layout

### March 29, 2012
 * Renamed a few more `Settings` properties to reflect better their meanings

### February 18, 2012
 * Renamed some `Settings` properties here and there to correct typos and reflect better their meanings

### January 8, 2012
 * Should now have an easier time automatically finding OpenCV libraries inside standard directories such as `/usr/local/lib/`, `/opt/local/lib/`, and `C:\opencv\`, even when they are not part of the system configuration or PATH
 * New `PS3EyeFrameGrabber` from Jiri Masa can now grab images using the SDK from Code Laboratories

### October 1, 2011
 * Fixed `DC1394FrameGrabber` and `FlyCaptureFrameGrabber` to behave as expected with all Bayer/Raw/Mono/RGB/YUV cameras modes (within the limits of libdc1394 and PGR FlyCapture)

### August 21, 2011
 * Upgraded support to OpenCV 2.3.1
 * `OpenCVFrameGrabber` now detects when CV_CAP_PROP_POS_MSEC is broken and gives up calling `cvGetCaptureProperty()`

### July 5, 2011
 * Upgraded support to OpenCV 2.3.0
 * Fixed `OpenKinectFrameGrabber` and `FFmpegFrameGrabber`

### June 10, 2011
 * New `OpenKinectFrameGrabber` to capture from Microsoft's Kinect stereo camera using OpenKinect
 * The Unix scripts now check for a 64-bit JVM in priority

### May 11, 2011
 * Changed `Marker.getCenter()` back to the centroid, because it has better noise averaging properties and gives in practice more accurate results than the actual center
 * Added hack to `OpenCVFrameGrabber.start()` to wait for `cvRetrieveFrame()` to return something else than `null` under Mac OS X
 * Added to the scripts `-Dapple.awt.fullscreencapturealldisplays=false` Java option required for full-screen support under Mac OS X 
 * Removed from the scripts the default `-Dsun.java2d.opengl=True` Java option, because since NVIDIA Release 260 family of drivers, most video drivers under Linux do not have good OpenGL support anymore
 * `FFmpegFrameGrabber` now works properly on Windows with newer binaries
 * New `VideoInputFrameGrabber` to capture using DirectShow, useful under Windows 7 where OpenCV and FFmpeg can fail to capture using Video for Windows
 * `GeometricCalibrator` now reports the maximum errors in addition to the average (RMS) errors

### April 7, 2011
 * Added a `format` property to camera settings, mostly useful for `FFmpegFrameGrabber`, where interesting values include "dv1394", "mjpeg", "video4linux2", "vfwcap", and "x11grab"
 * Added hack to make sure the temporarily extracted library files get properly deleted under Windows

### February 19, 2011
 * Upgraded to the latest version of JavaCV based on JavaCPP instead of JNA, featuring better performance
 * Tried to fix image format conversion inside `FlyCaptureFrameGrabber`, but this is going to require more careful debugging

### November 4, 2010
 * Renamed the package namespace to `com.googlecode.javacv.procamcalib`, which makes more sense now that JavaCV has been well anchored at Google Code for more than a year, piggybacking on the unique and easy-to-remember domain name, but this means you will need to manually edit any old XML `settings.pcc` files and rename the namespace of the classes inside
 * `CanvasFrame` now redraws its `Canvas` after the user resizes the `Frame`
 * Added check to `DC1394FrameGrabber` so that a "Failed to initialize libdc1394" does not crash the JVM
 * `FrameGrabber` now selects the default grabber a bit better
 * Included Mac OS X 10.6.4 binaries for ARToolKitPlus compiled by Christian Parsons

### July 30, 2010
 * Fixed crash that would occur in `CanvasFrame` for some video drivers

### May 30, 2010
 * Fixed loading problem with the `frameGrabber` setting
 * Fixed speed setting problem with the `FlyCaptureFrameGrabber`

### April 16, 2010
 * Modified a few things to get better default behavior of gamma correction
 * Camera setting `triggerFlushSize` now defaults to 5 (only affects `OpenCVFrameGrabber` and `FFmpegFrameGrabber`)
 * Fixed missing updates when changing some settings

### April 8, 2010
 * Added support for OpenCV 2.1

### April 5, 2010
 * Some bugs fixed for FFmpeg

### March 21, 2010
 * Added to camera settings a `deviceFile` field that can bring up a file dialog
 * Added `triggerFlushSize` to indicate the number of buffers to flush for cheap camera that keep old images in memory indefinitely
 * The size and spacing of markers inside MarkerPatterns can now be adjusted separately in X and Y
 * Fixed distortion problem with color calibration when running with OpenCV 1.1pre1

### February 13, 2010
 * Added `FFmpegFrameGrabber` to capture images using FFmpeg 0.5
 * Fixed corruption of distortion coefficients that could occur

### December 22, 2009
 * Sync with new JavaCV, no functional changes to ProCamCalib itself

### November 24, 2009
 * Fixed some crashy crashy behavior
 * Added R2 correlation coefficient for color calibration as indicator of good accuracy
 * `useMarkerCenters` now uses the actual physical center of each marker instead of the centroid

### October 19, 2009
 * Added color calibration

### October 14, 2009
 * Upgraded JavaCV to work with OpenCV 2.0 in addition to 1.1pre1
 * Added a missing exception "catch", which allowed a failed calibration to go unnoticed

### October 2, 2009
 * Fixed a bug in JavaCV that prevented loading distortion coefficients
 * Added a stack dump on OpenCV error for easier debugging

### August 26, 2009
 * Added `gamma` settings for Cameras and Projectors

### August 19, 2009
 * Sync with new source of javacv and procamcalib
 * There is no functional changes to ProCamCalib itself

### August 11, 2009
Initial release


Acknowledgments
---------------
This project was conceived at the [Okutomi & Tanaka Laboratory](http://www.ok.ctrl.titech.ac.jp/), Tokyo Institute of Technology, where I was supported for my doctoral research program by a generous scholarship from the Ministry of Education, Culture, Sports, Science and Technology (MEXT) of the Japanese Government. I extend my gratitude further to all who have reported bugs, donated code, or made suggestions for improvements (details above)!
