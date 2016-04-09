ProCamCalib
===========

Introduction
------------
ProCamCalib is a user-friendly tool to perform full geometric and color calibration of projector-camera systems. It supports configurations of multiple cameras and projectors, but does not (yet) do global optimization of parameters. All devices are stereo calibrated with the first camera, which is placed at the origin. Also, the calibration board needs to stay visible in all cameras. Hopefully, these restrictions will be relaxed in the future.

Additionally, given that camera-only systems are a subset of projector-camera systems, the application will happily calibrate a bunch of cameras with zero projectors, effectively implementing Fiala's method (reference below).

Please cite my Procams 2009 paper (reference below) if you find this tool useful for your own research projects. Thank you.


Downloads
---------
 * ProCamCalib 1.1 binary archive  [procamcalib-1.1-bin.zip](http://search.maven.org/remotecontent?filepath=org/bytedeco/procamcalib/1.1/procamcalib-1.1-bin.zip) (102 MB)
 * ProCamCalib 1.1 source archive  [procamcalib-1.1-src.zip](http://search.maven.org/remotecontent?filepath=org/bytedeco/procamcalib/1.1/procamcalib-1.1-src.zip) (50 KB)

The binary archive contains builds for Linux, Mac OS X, and Windows.


Required Software
-----------------
I wrote ProCamCalib itself in Java and its binary should run on any platform where an implementation of Java SE 7 or newer exists. The binary distribution also contains natively compiled code for Linux, Mac OS X, and Windows, needed by JavaCV.

Please install the following before running ProCamCalib:

 * An implementation of Java SE 7 or newer:
   * OpenJDK  http://openjdk.java.net/install/  or
   * Sun JDK  http://www.oracle.com/technetwork/java/javase/downloads/  or
   * IBM JDK  http://www.ibm.com/developerworks/java/jdk/

And be aware that ProCamCalib runs _a lot_ faster under the "server" JVM than the "client" JVM, but because of its bigger size, not all distributions of Java come with the server one.

Additionally, for IIDC/DCAM cameras, Microsoft's Kinect stereo camera, or the PS3 Eye:

 * libdc1394 2.1.x or 2.2.x  http://sourceforge.net/projects/libdc1394/files/
 * FlyCapture 2.7.x or 2.8.x  http://www.ptgrey.com/flycapture-sdk
 * libfreenect 0.5.x  https://github.com/OpenKinect/libfreenect
 * CL Eye Platform SDK  http://codelaboratories.com/downloads/


Usage
-----
Under Linux, Mac OS X, and other Unix variants, execute either `procamcalib-nativelook` or `procamcalib-oceanlook`, according to the theme that works best on your system. ("Ocean" being Java's original look and feel.) The equivalent files under Windows are `procamcalib-nativelook.cmd` and `procamcalib-oceanlook.cmd`.

After launch, the user interface that appears allows the user to change the number of cameras and projectors to calibrate. There are also a lot of settings, although the defaults should be good enough for the usual cases. I do not detail them here, but most of them should be clear to people familiar with my Procams 2009 paper based on previous work by Fiala, Zhang, and many others as part of OpenCV. Here are the relevant references:

[Samuel Audet and Masatoshi Okutomi. A User-Friendly Method to Geometrically Calibrate Projector-Camera Systems. The 22nd IEEE Conference on Computer Vision and Pattern Recognition (CVPR 2009) - Workshops (Procams 2009), pages 47--54. IEEE Computer Society, June 2009.](http://www.ok.ctrl.titech.ac.jp/res/PCS/publications/procams2009.pdf)

[Gary Bradski and Adrian Kaehler. Learning OpenCV: Computer Vision with the OpenCV Library. O'Reilly, 2008.](http://oreilly.com/catalog/9780596516130/)

[Mark Fiala and Chang Shu. Self-identifying patterns for plane-based camera calibration. Machine Vision and Applications, 19(4):209-216, July 2008.](http://nparc.cisti-icist.nrc-cnrc.gc.ca/npsi/ctrl?action=rtdoc&an=8913774&article=0)

[Zhengyoug Zhang. A Flexible New Technique for Camera Calibration. IEEE Transactions on Pattern Analysis and Machine Intelligence, 22(11):1330-1334, 2000.](http://research.microsoft.com/en-us/um/people/zhang/Papers/TR98-71.pdf)


Once you have modified all the desired settings, since the application may crash during the operations described below, please save them in an XML file via the "Settings" menu.

Before going any further, you will need to print out the board pattern. Export the image file by clicking on the "Save As..." button at the bottom of the main window, and print out the resulting file (written in PNG, BMP, PGM, or any other format supported by OpenCV, depending on the extension you provided to the filename).

After pasting the pattern on a flat calibration board, and making sure that the "screenNumber" settings correspond to the one of your projectors, you may start the calibration process via the "Calibration" menu. However, before starting, I recommend, if possible, to set your cameras in a mode with more than 8 bits per pixel (e.g.: 10 or 16 bits). The added dynamic range may make the calibration process easier and more accurate. The algorithm calibrates all cameras simultaneously, while calibrating projectors only one at a time, for obvious reasons. When you want ProCamCalib to take an image for calibration, keep the board as steady as possible for a few seconds until you see the camera image "flash". Please refer to my Procams 2009 paper and the demo video to understand further how to perform calibration. Also, please note that ProCamCalib may fail to detect markers properly if they are not clear or big enough. Please adjust properly the focus, exposure, resolution, etc. of your projectors and cameras.

Be aware that color calibration is enabled by default. After geometric calibration has completed, color calibration will start and display automatically an array of colors. If you do not need color calibration, make sure you disable it in the settings before starting calibration.

After a successful calibration session, the application holds in memory the "calibration data". You may examine and save this data via the "Calibration" menu. All length values are expressed in pixel units, including the sizes and spacings of the "MarkerPatterns". The program uses OpenCV to output a file in the YAML or XML format. This information can easily be parsed back in any of your programs by using the CvFileStorage facilities of OpenCV.

Feel free to contact me if you have any questions or find any problems with the software! I am sure it is far from perfect...


Source Code
-----------
I make all the source code available on GitHub at https://github.com/bytedeco/procamcalib . You will also need the following to modify and build the application:

 * A C/C++ compiler
 * JavaCPP 1.1  https://github.com/bytedeco/javacpp
 * JavaCV  1.1  https://github.com/bytedeco/javacv
 * OpenCV 3.0.0  http://sourceforge.net/projects/opencvlibrary/files/
 * FFmpeg 2.8.x  http://ffmpeg.org/download.html
 * ARToolKitPlus 2.3.x  https://launchpad.net/artoolkitplus
 * NetBeans 8.0  http://netbeans.org/downloads/
 * Maven 3.x  http://maven.apache.org/download.html

(The icons were shamelessly copied from the source code repository of NetBeans. Also licensed under the GPLv2.)

Please keep me informed of any updates or fixes you make to the code so that I may integrate them into my own version. Thank you!


----
Project lead: Samuel Audet [samuel.audet `at` gmail.com](mailto:samuel.audet at gmail.com)  
Developer site: https://github.com/bytedeco/procamcalib  
Discussion group: http://groups.google.com/group/javacv
