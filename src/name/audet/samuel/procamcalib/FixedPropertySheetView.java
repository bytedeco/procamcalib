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

import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.explorer.propertysheet.PropertySheetView;

/**
 *
 * @author Samuel Audet
 */
public class FixedPropertySheetView extends PropertySheetView {
    static {
        Icon i = null;
        try {
            i = UIManager.getIcon("Tree.gtk_collapsedIcon");
        } catch (Exception ex) { }
        if (i == null) {
            UIManager.put("Tree.gtk_collapsedIcon", UIManager.getIcon("Tree.collapsedIcon"));
        }
    }
}
