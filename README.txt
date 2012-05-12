=ProCamCalib=

==Introduction==
ProCamCalib is a user-friendly tool to perform full geometric and color calibration of projector-camera systems. It supports configurations of multiple cameras and projectors, but does not (yet) do global optimization of parameters. All devices are stereo calibrated with the first camera, which is placed at the origin. Also, the calibration board needs to stay visible in all cameras. Hopefully, these restrictions will be relaxed in the future.

Additionally, given that camera-only systems are a subset of projector-camera systems, the application will happily calibrate a bunch of cameras with zero projectors, effectively implementing Fiala's method (reference below).

Please cite my Procams 2009 paper (reference below) if you find this tool useful for your own research projects. Thank you.


==Required Software==
I wrote ProCamCalib itself in Java and its binary should run on any platform where an implementation of Java SE 6 or 7 exists. The binary distribution also contains natively compiled code for Linux, Mac OS X, and Windows, needed by JavaCV. Still, additional software is required. (For answers to problems frequently encountered with OpenCV on the Windows platform, please refer to [http://code.google.com/p/javacv/wiki/Windows7AndOpenCV  Common issues with OpenCV under Windows 7].)

Please install the following before running ProCamCalib:
 * An implementation of Java SE 6 or 7
  * OpenJDK  http://openjdk.java.net/install/  or
  * Sun JDK  http://www.oracle.com/technetwork/java/javase/downloads/  or
  * IBM JDK  http://www.ibm.com/developerworks/java/jdk/  or
  * Java SE for Mac OS X  http://developer.apple.com/java/  etc.
 * OpenCV 2.4.0  http://sourceforge.net/projects/opencvlibrary/files/

And please make sure your Java and OpenCV have the same bitness: *32-bit and 64-bit modules do not mix under any circumstances*. Further, ProCamCalib runs _a lot_ faster under the "server" JVM than the "client" JVM, but because of its bigger size, not all distributions of Java come with the server one.

Additionally, for IIDC/DCAM cameras, Microsoft's Kinect stereo camera, the PS3 Eye, or other cameras supported via FFmpeg:
 * libdc1394 2.1.x (Linux and Mac OS X)  http://sourceforge.net/projects/libdc1394/files/
 * PGR FlyCapture 1.7~2.2 (Windows only)  http://www.ptgrey.com/products/pgrflycapture/
 * OpenKinect  http://openkinect.org/
 * CL Eye Platform SDK  http://codelaboratories.com/downloads/
 * FFmpeg 0.6.x or 0.7.x  http://ffmpeg.org/download.html
  * Precompiled for Windows  http://ffmpeg.zeranoe.com/builds/  Known compatible builds:
   * http://ffmpeg.zeranoe.com/builds/win32/shared/ffmpeg-0.7.1-win32-shared.7z
   * http://ffmpeg.zeranoe.com/builds/win64/shared/ffmpeg-0.7.1-win64-shared.7z


==Usage==
Under Linux, Mac OS X, and other Unix variants, execute either `procamcalib-nativelook` or `procamcalib-oceanlook`, according to the theme that works best on your system. ("Ocean" being Java's original look and feel.) The equivalent files under Windows are `procamcalib-nativelook.cmd` and `procamcalib-oceanlook.cmd`.

After launch, the user interface that appears allows the user to change the number of cameras and projectors to calibrate. There are also a lot of settings, although the defaults should be good enough for the usual cases. I do not detail them here, but most of them should be clear to people familiar with my Procams 2009 paper based on previous work by Fiala, Zhang, and many others as part of OpenCV. Here are the relevant references:

Samuel Audet and Masatoshi Okutomi. A User-Friendly Method to Geometrically Calibrate Projector-Camera Systems. The 22nd IEEE Conference on Computer Vision and Pattern Recognition (CVPR 2009) - Workshops (Procams 2009), pages 47--54. IEEE Computer Society, June 2009. http://www.ok.ctrl.titech.ac.jp/~saudet/publications/procams2009.pdf

Gary Bradski and Adrian Kaehler. Learning OpenCV: Computer Vision with the OpenCV Library. O'Reilly, 2008. http://oreilly.com/catalog/9780596516130/

Mark Fiala and Chang Shu. Self-identifying patterns for plane-based camera calibration. Machine Vision and Applications, 19(4):209-216, July 2008. http://nparc.cisti-icist.nrc-cnrc.gc.ca/npsi/ctrl?action=rtdoc&an=8913774&article=0

Zhengyoug Zhang. A Flexible New Technique for Camera Calibration. IEEE Transactions on Pattern Analysis and Machine Intelligence, 22(11):1330-1334, 2000. http://research.microsoft.com/en-us/um/people/zhang/Papers/TR98-71.pdf


Once you have modified all the desired settings, since the application may crash during the operations described below, please save them in an XML file via the "Settings" menu.

Before going any further, you will need to print out the board pattern. Export the image file by clicking on the "Save As..." button at the bottom of the main window, and print out the resulting file (written in PNG, BMP, PGM, or any other format supported by OpenCV, depending on the extension you provided to the filename).

After pasting the pattern on a flat calibration board, and making sure that the "screenNumber" settings correspond to the one of your projectors, you may start the calibration process via the "Calibration" menu. However, before starting, I recommend, if possible, to set your cameras in a mode with more than 8 bits per pixel (e.g.: 10 or 16 bits). The added dynamic range may make the calibration process easier and more accurate. The algorithm calibrates all cameras simultaneously, while calibrating projectors only one at a time, for obvious reasons. When you want ProCamCalib to take an image for calibration, keep the board as steady as possible for a few seconds until you see the camera image "flash". Please refer to my Procams 2009 paper and the demo video to understand further how to perform calibration. Also, please note that ProCamCalib may fail to detect markers properly if they are not clear or big enough. Please adjust properly the focus, exposure, resolution, etc. of your projectors and cameras.

Be aware that color calibration is enabled by default. After geometric calibration has completed, color calibration will start and display automatically an array of colors. If you do not need color calibration, make sure you disable it in the settings before starting calibration.

After a successful calibration session, the application holds in memory the "calibration data". You may examine and save this data via the "Calibration" menu. All length values are expressed in pixel units, including the sizes and spacings of the "MarkerPatterns". The program uses OpenCV to output a file in the YAML or XML format. This information can easily be parsed back in any of your programs by using the CvFileStorage facilities of OpenCV.

Feel free to contact me if you have any questions or find any problems with the software! I am sure it is far from perfect...


==Source Code==
I make all the source code available on my site at http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/ . You will also need the following to modify and build the application:
 * A C/C++ compiler
 * JavaCPP http://code.google.com/p/javacpp/
 * JavaCV  http://code.google.com/p/javacv/
 * ARToolKitPlus 2.1.1t  http://code.google.com/p/javacv/downloads/list
 * NetBeans 6.9  http://netbeans.org/downloads/  or
 * Maven 2 or 3  http://maven.apache.org/download.html

(The icons were shamelessly copied from the source code repository of NetBeans. Also licensed under the GPLv2.)

Please keep me informed of any updates or fixes you make to the code so that I may integrate them into my own version. Thank you!


==Acknowledgments==
This project was conceived at the Okutomi & Tanaka Laboratory, Tokyo Institute of Technology, where I was supported for my doctoral research project by a generous scholarship from the Ministry of Education, Culture, Sports, Science and Technology (MEXT) of the Japanese Government. I extend my gratitude further to all who have reported bugs, donated code, or made suggestions for improvements!


==Changes==
===May 12, 2012===
 * Upgraded support to OpenCV 2.4.0
 * Added `pom.xml` and assembly files for Maven support and changed the directory structure of the source code to match Maven's standard directory layout

===March 29, 2012===
 * Renamed a few more `Settings` properties to reflect better their meanings

===February 18, 2012===
 * Renamed some `Settings` properties here and there to correct typos and reflect better their meanings

===January 8, 2012===
 * Should now have an easier time automatically finding OpenCV libraries inside standard directories such as `/usr/local/lib/`, `/opt/local/lib/`, and `C:\opencv\`, even when they are not part of the system configuration or PATH
 * New `PS3EyeFrameGrabber` from Jiri Masa can now grab images using the SDK from Code Laboratories

===October 1, 2011===
 * Fixed `DC1394FrameGrabber` and `FlyCaptureFrameGrabber` to behave as expected with all Bayer/Raw/Mono/RGB/YUV cameras modes (within the limits of libdc1394 and PGR FlyCapture)

===August 21, 2011===
 * Upgraded support to OpenCV 2.3.1
 * `OpenCVFrameGrabber` now detects when CV_CAP_PROP_POS_MSEC is broken and gives up calling `cvGetCaptureProperty()`

===July 5, 2011===
 * Upgraded support to OpenCV 2.3.0
 * Fixed `OpenKinectFrameGrabber` and `FFmpegFrameGrabber`

===June 10, 2011===
 * New `OpenKinectFrameGrabber` to capture from Microsoft's Kinect stereo camera using OpenKinect
 * The Unix scripts now check for a 64-bit JVM in priority

===May 11, 2011===
 * Changed `Marker.getCenter()` back to the centroid, because it has better noise averaging properties and gives in practice more accurate results than the actual center
 * Added hack to `OpenCVFrameGrabber.start()` to wait for `cvRetrieveFrame()` to return something else than `null` under Mac OS X
 * Added to the scripts `-Dapple.awt.fullscreencapturealldisplays=false` Java option required for full-screen support under Mac OS X 
 * Removed from the scripts the default `-Dsun.java2d.opengl=True` Java option, because since NVIDIA Release 260 family of drivers, most video drivers under Linux do not have good OpenGL support anymore
 * `FFmpegFrameGrabber` now works properly on Windows with newer binaries
 * New `VideoInputFrameGrabber` to capture using DirectShow, useful under Windows 7 where OpenCV and FFmpeg can fail to capture using Video for Windows
 * `GeometricCalibrator` now reports the maximum errors in addition to the average (RMS) errors

===April 7, 2011===
 * Added a `format` property to camera settings, mostly useful for `FFmpegFrameGrabber`, where interesting values include "dv1394", "mjpeg", "video4linux2", "vfwcap", and "x11grab"
 * Added hack to make sure the temporarily extracted library files get properly deleted under Windows

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
Copyright (C) 2009-2012 Samuel Audet <saudet@ok.ctrl.titech.ac.jp>
Web site: http://www.ok.ctrl.titech.ac.jp/~saudet/procamcalib/

Licensed under the GNU General Public License version 2 (GPLv2).
Please refer to LICENSE.txt or http://www.gnu.org/licenses/ for details.

