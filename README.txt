=ProCamCalib=

==Introduction==
ProCamCalib is a user-friendly tool to perform full geometric and color calibration of projector-camera systems. It supports configurations of multiple cameras and projectors, but does not (yet) do global optimization of parameters. All devices are stereo calibrated with the first camera, which is placed at the origin. Also, the calibration board needs to stay visible in all cameras. Hopefully, these restrictions will be relaxed in the future.

Additionally, given that camera-only systems are a subset of projector-camera systems, the application will happily calibrate a bunch of cameras with zero projectors, effectively implementing Fiala's method (reference below).

Please cite my Procams 2009 paper (reference below) if you find this tool useful for your own research projects. Thank you.


==Required Software==
I wrote ProCamCalib itself in Java and its binary should run on any platform where an implementation of Java SE 1.6 exists. The binary distribution also contains natively compiled code for Linux, Mac OS X, and Windows, needed by JavaCV. Still, additional software is required.

Please install the following before running ProCamCalib:
 * An implementation of Java SE 6
  * OpenJDK 6  http://openjdk.java.net/install/  or
  * Sun JDK 6  http://www.oracle.com/technetwork/java/javase/downloads/  or
  * IBM JDK 6  http://www.ibm.com/developerworks/java/jdk/  or
  * Java SE 6 for Mac OS X  http://developer.apple.com/java/  etc.
 * OpenCV 2.2  http://sourceforge.net/projects/opencvlibrary/files/

*IMPORTANT NOTE*: 
 * ProCamCalib runs _a lot_ faster under the "server" JVM than the "client" JVM, but because of its bigger size, not all distributions of Java come with the server one.

Additionally, for IIDC/DCAM cameras only:
 * libdc1394 2.1.x (Linux and Mac OS X)  http://sourceforge.net/projects/libdc1394/files/
 * PGR FlyCapture 1.7~2.1 (Windows only)  http://www.ptgrey.com/products/pgrflycapture/

Further, camera input via FFmpeg is also supported, but needs FFmpeg 0.6 or more recent:
 * Source code  http://ffmpeg.org/download.html
 * Precompiled Windows DLLs  http://ffmpeg.arrozcru.org/autobuilds/


==Usage==
Under Linux, Mac OS X, and other Unix variants, execute either `procamcalib-nativelook` or `procamcalib-oceanlook`, according to the theme you like best. ("Ocean" being Java's original look and feel.) The equivalent files under Windows are `procamcalib-nativelook.cmd` and `procamcalib-oceanlook.cmd`.

After launch, the user interface that appears allows the user to change the number of cameras and projectors to calibrate. There are also a lot of settings, although the defaults should be good enough for the usual cases. I do not detail them here, but most of them should be clear to people familiar with my Procams 2009 paper based on previous work by Fiala, Zhang, and many others as part of OpenCV. Here are the relevant references:

Samuel Audet and Masatoshi Okutomi. A User-Friendly Method to Geometrically Calibrate Projector-Camera Systems. The 22nd IEEE Conference on Computer Vision and Pattern Recognition (CVPR 2009) - Workshops (Procams 2009). IEEE Computer Society, June 2009. http://www.ok.ctrl.titech.ac.jp/~saudet/publications/procams2009.pdf

Gary Bradski and Adrian Kaehler. Learning OpenCV: Computer Vision with the OpenCV Library. O'Reilly, 2008. http://oreilly.com/catalog/9780596516130/

Mark Fiala and Chang Shu. Self-identifying patterns for plane-based camera calibration. Machine Vision and Applications, 19(4):209-216, July 2008. http://nparc.cisti-icist.nrc-cnrc.gc.ca/npsi/ctrl?action=rtdoc&an=8913774&article=0

Zhengyoug Zhang. A Flexible New Technique for Camera Calibration. IEEE Transactions on Pattern Analysis and Machine Intelligence, 22(11):1330-1334, 2000. http://research.microsoft.com/en-us/um/people/zhang/Papers/TR98-71.pdf


Once you have modified all the desired settings, since the application may crash during the operations described below, please save them in an XML file via the "Settings" menu.

Before going any further, you will need to print out the board pattern. Export the image file by clicking on the "Save As..." button at the bottom of the main window, and print out the resulting file (written in PNG, BMP, PGM, or any other format supported by OpenCV, depending on the extension you provided to the filename).

After pasting the pattern on a flat calibration board, you may start the calibration process via the "Calibration" menu. However, before starting calibration, I recommend, if possible, to set your cameras in a mode with more than 8 bits per pixel (e.g.: 10 or 16 bits). The added dynamic range may make the calibration process easier and more accurate. The algorithm calibrates all cameras simultaneously, while calibrating projectors only one at a time, for obvious reasons. When you want ProCamCalib to take an image for calibration, keep the board as steady as possible for a few seconds until you see the camera image "flash". Please refer to my Procams 2009 paper and the demo video to understand further how to perform calibration. 

Be aware that color calibration is enabled by default. After geometric calibration, color calibration will automatically start and display an array of colors. If you do not need color calibration, make sure you disable it in the settings before starting calibration.

After a successful calibration session, the application holds in memory the "calibration data". You may examine and save this data via the "Calibration" menu. The program uses OpenCV to output a file in the YAML or XML format. This information can easily be parsed back in any of your programs by using the CvFileStorage facilities of OpenCV.

Feel free to contact me if you have any questions or find any problems with the software! I am sure it is far from perfect...


==Source Code==
I make all the source code available on my site at http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/ . You will also need the following to modify and build the application:
 * A C/C++ compiler
 * JavaCPP http://code.google.com/p/javacpp/
 * JavaCV  http://code.google.com/p/javacv/
 * ARToolKitPlus 2.1.1t  http://code.google.com/p/javacv/downloads/list
 * NetBeans 6.9  http://netbeans.org/downloads/

(The icons were shamelessly copied from the source code repository of NetBeans. Also licensed under the GPLv2.)

Please keep me informed of any updates or fixes you make to the code so that I may integrate them into my own version. Thank you!


==Acknowledgments==
I am currently an active member of the Okutomi & Tanaka Laboratory, Tokyo Institute of Technology, supported by a scholarship from the Ministry of Education, Culture, Sports, Science and Technology (MEXT) of the Japanese Government.


==Changes==
===February 19, 2011===
 * Upgraded to the latest version of JavaCV based on JavaCPP instead of JNA, featuring better performance
 * Tried to fix image format conversion inside `FlyCaptureFrameGrabber`, but this is going to require more careful debugging

===November 4, 2010===
 * Renamed the package namespace to `com.googlecode.javacv.procamcalib`, which makes more sense now that JavaCV has been well anchored at Google Code for more than a year, piggybacking on the unique and easy-to-remember domain name, but this means you will need to manually edit any old XML `settings.pcc` files and rename the namespace of the classes inside
 * `CanvasFrame` now redraws its `Canvas` after the user resizes the `Frame`
 * Added check to `DC1394FrameGrabber` so that a "Failed to initialize libdc1394" does not crash the JVM
 * `FrameGrabber` now selects the default grabber a bit better
 * Included Mac OS X 10.6.4 binaries for ARToolKitPlus compiled by Christian Parsons

===July 30, 2010===
 * Fixed crash that would occur in `CanvasFrame` for some video drivers

===May 30, 2010===
 * Fixed loading problem with the `frameGrabber` setting
 * Fixed speed setting problem with the `FlyCaptureFrameGrabber`

===April 16, 2010===
 * Modified a few things to get better default behavior of gamma correction
 * Camera setting `triggerFlushSize` now defaults to 5 (only affects `OpenCVFrameGrabber` and `FFmpegFrameGrabber`)
 * Fixed missing updates when changing some settings

===April 8, 2010===
 * Added support for OpenCV 2.1

===April 5, 2010===
 * Some bugs fixed for FFmpeg

===March 21, 2010===
 * Added to camera settings a `deviceFile` field that can bring up a file dialog
 * Added `triggerFlushSize` to indicate the number of buffers to flush for cheap camera that keep old images in memory indefinitely
 * The size and spacing of markers inside MarkerPatterns can now be adjusted separately in X and Y
 * Fixed distortion problem with color calibration when running with OpenCV 1.1pre1

===February 13, 2010===
 * Added `FFmpegFrameGrabber` to capture images using FFmpeg 0.5
 * Fixed corruption of distortion coefficients that could occur

===December 22, 2009===
 * Sync with new JavaCV, no functional changes to ProCamCalib itself

===November 24, 2009===
 * Fixed some crashy crashy behavior
 * Added R2 correlation coefficient for color calibration as indicator of good accuracy
 * `useMarkerCenters` now uses the actual physical center of each marker instead of the centroid

===October 19, 2009===
 * Added color calibration

===October 14, 2009===
 * Upgraded JavaCV to work with OpenCV 2.0 in addition to 1.1pre1
 * Added a missing exception "catch", which allowed a failed calibration to go unnoticed

===October 2, 2009===
 * Fixed a bug in JavaCV that prevented loading distortion coefficients
 * Added a stack dump on OpenCV error for easier debugging

===August 26, 2009===
 * Added `gamma` settings for Cameras and Projectors

===August 19, 2009===
 * Sync with new source of javacv and procamcalib
 * There is no functional changes to ProCamCalib itself

===August 11, 2009===
Initial release


----
Copyright (C) 2009,2010,2011 Samuel Audet <saudet@ok.ctrl.titech.ac.jp>
Web site: http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/

Licensed under the GNU General Public License version 2 (GPLv2).
Please refer to LICENSE.txt or http://www.gnu.org/licenses/ for details.

