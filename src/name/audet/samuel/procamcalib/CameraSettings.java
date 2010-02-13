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

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextSupport;
import java.util.Arrays;
import name.audet.samuel.javacv.CameraDevice;
import name.audet.samuel.javacv.FrameGrabber;

/**
 *
 * @author Samuel Audet
 */
public class CameraSettings extends BeanContextSupport {

    public CameraSettings() {
        // select first frame grabber that can load..
        for (int i = 0; i < FrameGrabber.list.size(); i++) {
            try {
                Class<? extends FrameGrabber> c = FrameGrabber.list.get(i);
                c.getMethod("tryLoad").invoke(null);
                frameGrabber = c;
                break;
            } catch (Exception ex) { }
        }
    }

    public int getQuantity() {
        return size();
    }
    public void setQuantity(int quantity) throws PropertyVetoException {
        quantity = Math.max(1, quantity);
        Object[] a = toArray();
        int i = a.length;
        while (i > quantity) {
            remove(a[i-1]);
            i--;
        }
        while (i < quantity) {
            CameraDevice.Settings c = new CameraDevice.Settings();
            c.setName("Camera " + String.format("%2d", i));
            c.setDeviceNumber(i);
            c.setFrameGrabber(frameGrabber);
            add(c);
            i++;
        }
        pcSupport.firePropertyChange("quantity", a.length, quantity);
    }

    double windowScale = 1.0;
    public double getWindowScale() {
        return windowScale;
    }
    public void setWindowScale(double windowScale) {
        this.windowScale = windowScale;
    }

    Class<? extends FrameGrabber> frameGrabber;
    public Class<? extends FrameGrabber> getFrameGrabber() {
        return frameGrabber;
    }
    public void setFrameGrabber(Class<? extends FrameGrabber> frameGrabber) {
        this.frameGrabber = frameGrabber;
        for (Object o : this) {
            ((CameraDevice.Settings)o).setFrameGrabber(frameGrabber);
        }
    }

    @Override public Object[] toArray() {
        Object[] a = super.toArray();
        Arrays.sort(a);
        return a;
    }
    @Override public Object[] toArray(Object[] a) {
        a = super.toArray(a);
        Arrays.sort(a);
        return a;
    }
    public CameraDevice.Settings[] toTypedArray() {
        return (CameraDevice.Settings[])toArray(new CameraDevice.Settings[0]);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.removePropertyChangeListener(listener);
    }
}
