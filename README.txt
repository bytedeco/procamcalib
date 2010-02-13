=ProCamCalib=

==Introduction==
ProCamCalib is a user-friendly tool to perform full geometric calibration of projector-camera systems. It supports configurations of multiple cameras and projectors, but does not (yet) do global optimization of parameters. All devices are stereo calibrated with the first camera, which is placed at the origin. Also, the calibration board needs to stay visible in all cameras. Hopefully, these restrictions will be relaxed in the future.

Additionally, given that camera-only systems are a subset of projector-camera systems, the application will happily calibrate a bunch of cameras with zero projectors, effectively implementing Fiala's method (reference below).

Please cite my Procams 2009 paper (reference below) if you find this tool useful for your own research projects. Thank you.


==Required Software==
The binary distribution contains 32-bit and 64-bit x86 versions of ARToolKitPlus for both Linux and Windows. (If anyone compiles ARToolKitPlus under Mac OS X or other platforms, please send me the binary and I will include it.) I wrote ProCamCalib itself in Java and its binary should run on any platform where an implementation of Java SE 1.6 exists. Still, additional software is required.

Please install the following before running ProCamCalib:
 * An implementation of Java SE 6
  * OpenJDK 6  http://openjdk.java.net/install/  or
  * Sun JDK 6  http://java.sun.com/javase/downloads/  or
  * IBM JDK 6  http://www.ibm.com/developerworks/java/jdk/  or
  * Java SE 6 for Mac OS X  http://developer.apple.com/java/  etc.
 * OpenCV 1.1pre1 or 2.0  http://sourceforge.net/projects/opencvlibrary/files/

IMPORTANT NOTES: 
 * Be aware that ProCamCalib runs _a lot_ faster under the "server" JVM than the "client" JVM, but because of its bigger size, not all distributions of Java come with the server one.
 * The precompiled binaries of OpenCV 2.0 for Windows are incompatible with Sun JDK 6. For the casual Windows user, I recommend OpenCV 1.1pre1. A workaround might be included in a future version of JNA.

Additionnally, for IIDC/DCAM cameras only:
 * libdc1394 2.1.2 (Linux and Mac OS X) http://sourceforge.net/projects/libdc1394/files/
 * PGR FlyCapture 1 or 2 (Windows only) http://www.ptgrey.com/products/pgrflycapture/

Further, camera input via FFmpeg is also supported, but needs FFmpeg 0.5:
 * Source code  http://ffmpeg.org/download.html
 * Precompiled Windows DLLs  http://ffmpeg.arrozcru.org/autobuilds/


==Usage==
Under Linux, Mac OS X, and other Unix variants, execute either `procamcalib-nativelook` or `procamcalib-oceanlook`, according to the theme you like best. ("Ocean" being Java's original look and feel.) The equivalent files under Windows are `procamcalib-nativelook.cmd` and `procamcalib-oceanlook.cmd`.

After launch, the user interface that appears allows the user to change the number of cameras and projectors to calibrate. There are also a lot of settings, although the defaults should be good enough for the usual cases. I do not detail them here, but most of them should be clear to people familiar with my Procams 2009 paper based on previous work by Fiala, Zhang, and many others as part of OpenCV. Here are the relevant references:

Samuel Audet and Masatoshi Okutomi. A User-Friendly Method to Geometrically Calibrate Projector-Camera Systems. In Proceedings of the 2009 IEEE Computer Society Conference on Computer Vision and Pattern Recognition (CVPR '09) - Workshops (Procams 2009). IEEE Computer Society, June 2009. http://www.ok.ctrl.titech.ac.jp/~saudet/publications/procams2009.pdf

Gary Bradski and Adrian Kaehler. Learning OpenCV: Computer Vision with the OpenCV Library. O'Reilly, 2008. http://oreilly.com/catalog/9780596516130/

Mark Fiala and Chang Shu. Self-identifying patterns for plane-based camera calibration. Machine Vision and Applications, 19(4):209-216, July 2008. http://nparc.cisti-icist.nrc-cnrc.gc.ca/npsi/ctrl?action=rtdoc&an=8913774&article=0

Zhengyoug Zhang. A Flexible New Technique for Camera Calibration. IEEE Transactions on Pattern Analysis and Machine Intelligence, 22(11):1330-1334, 2000. http://research.microsoft.com/en-us/um/people/zhang/Papers/TR98-71.pdf


Once you have modified all the desired settings, since the application may crash during the operations described below, please save them in an XML file via the "Settings" menu.

Before going any further, you will need to print out the board pattern. Export the image file by clicking on the "Save As..." button at the bottom of the main window, and print out the resulting file (written in PNG, BMP, PGM, or any other format supported by OpenCV, depending on the extension you provided to the filename).

After pasting the pattern on a flat calibration board, you may start the calibration process via the "Calibration" menu. However, before starting calibration, I recommend, if possible, to set your cameras in a mode with more than 8 bits per pixel (e.g.: 10 or 16 bits). The added dynamic range makes the calibration process much easier and more accurate. The algorithm calibrates all cameras simultaneously, while calibrating projectors only one at a time, for obvious reasons. When you want ProCamCalib to take an image for calibration, keep the board as steady as possible for a few seconds until you see the camera image "flash". Please refer to my Procams 2009 paper and the demo video to understand further how to perform calibration. 

After a successful calibration session, the application holds in memory the "calibration data". You may examine and save this data via the "Calibration" menu. The program uses OpenCV to output a file in the YAML or XML format. This information can easily be parsed back in any of your programs by using the CvFileStorage facilities of OpenCV.

Feel free to contact me if you have any questions or find any problems with the software! I am sure it is far from perfect...


==Source Code==
I make all the source code available at the URL below. It is divided into three parts:
 * A version of ARToolKitPlus 2.1.1 with exported C functions added to DLL.cpp
 * JavaCV, which contains wrappers for OpenCV, ARToolKitPlus, libdc1394 2.x, PGR FlyCapture, FFmpeg, and more!
 * ProCamCalib, which implements a user-friendly calibration interface based on JavaCV

In addition to the software above, to modify and build the source code you will need:
 * Whatever native tools needed to build ARToolKitPlus
 * NetBeans 6.8  http://www.netbeans.org/downloads/
 * Java Native Access 3.2.4  https://jna.dev.java.net/

(The icons were shamelessly copied from the source code repository of NetBeans. Also licensed under the GPLv2.)

Please keep me informed of any updates or fixes you make to the code so that I may integrate them into my own version. Thank you!


==Acknowledgments==
I am currently an active member of the Okutomi & Tanaka Laboratory, Tokyo Institute of Technology, supported by a scholarship from the Ministry of Education, Culture, Sports, Science and Technology (MEXT) of the Japanese Government.


==Changes==
===February 13, 2010===
 * Added FFmpegFrameGrabber to capture images using FFmpeg 0.5
 * Fixed corruption of distortion coefficients that could occur

===December 22, 2009===
 * Sync with new JavaCV, no functional changes to ProCamCalib itself

===November 24, 2009===
 * Fixed some crashy crashy behavior
 * Added R2 correlation coefficient for color calibration as indicator of good accuracy
 * "useMarkerCenters" now uses the actual physical center of each marker instead of the centroid

===October 19, 2009===
 * Added color calibration

===October 14, 2009===
 * Upgraded JavaCV to work with OpenCV 2.0 in addition to 1.1pre1
 * Added a missing exception "catch", which allowed a failed calibration to go unnoticed

===October 2, 2009===
 * Fixed a bug in JavaCV that prevented loading distortion coefficients
 * Added a stack dump on OpenCV error for easier debugging

===August 26, 2009===
 * Added "gamma" settings for Cameras and Projectors

===August 19, 2009===
 * Sync with new source of javacv and procamcalib
 * There is no functional changes to ProCamCalib itself

===August 11, 2009===
Initial release


----
Copyright (C) 2009,2010 Samuel Audet <saudet@ok.ctrl.titech.ac.jp>
Web site: http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/

Licensed under the GNU General Public License version 2 (GPLv2).
Please refer to LICENSE.txt or http://www.gnu.org/licenses/ for details.
