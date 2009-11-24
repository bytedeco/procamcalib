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

import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextSupport;
import java.util.Arrays;
import name.audet.samuel.javacv.ProjectorDevice;

/**
 *
 * @author Samuel Audet
 */
public class ProjectorSettings extends BeanContextSupport {

    public int getQuantity() {
        return size();
    }
    public void setQuantity(int quantity) {
        Object[] a = toArray();
        int i = a.length;
        while (i > quantity) {
            remove(a[i-1]);
            i--;
        }
        while (i < quantity) {
            ProjectorDevice.Settings c = new ProjectorDevice.Settings();
            c.setName("Projector " + String.format("%2d", i));
            c.setScreenNumber(c.getScreenNumber()+i);
            add(c);
            for (PropertyChangeListener l : pcSupport.getPropertyChangeListeners()) {
                c.addPropertyChangeListener(l);
            }
            i++;
        }
        pcSupport.firePropertyChange("quantity", a.length, quantity);
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
    public ProjectorDevice.Settings[] toTypedArray() {
        return (ProjectorDevice.Settings[])toArray(new ProjectorDevice.Settings[0]);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.addPropertyChangeListener(listener);
        for (ProjectorDevice.Settings s : toTypedArray()) {
            s.addPropertyChangeListener(listener);
        }
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.removePropertyChangeListener(listener);
        for (ProjectorDevice.Settings s : toTypedArray()) {
            s.removePropertyChangeListener(listener);
        }
    }
}
