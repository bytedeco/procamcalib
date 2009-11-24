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

import java.beans.PropertyEditorSupport;
import name.audet.samuel.javacv.FrameGrabber;

/**
 *
 * @author Samuel Audet
 */
public class FrameGrabberPropertyEditor extends PropertyEditorSupport {
    @Override public String getAsText() {
        Class c = (Class)getValue();
        return c.getSimpleName();
    }
    @Override public void setAsText(String s) {
        for (int i = 0; i < FrameGrabber.list.size(); i++) {
            Class c = FrameGrabber.list.get(i);
            if (s.equals(c.getSimpleName())) {
                setValue(c);
            }
        }
    }
    @Override public String[] getTags() {
        String[] s = new String[FrameGrabber.list.size()];
        for (int i = 0; i < FrameGrabber.list.size(); i++) {
            s[i] = FrameGrabber.list.get(i).getSimpleName();
        }
        return s;
    }
}
