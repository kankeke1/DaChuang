/*
 * Author : Christopher Henard (christopher.henard@uni.lu)
 * Date : 20/08/2013
 * Copyright 2013 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
 * All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pledge.gui.controllers;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import pledge.gui.views.ViewPLEDGE;

/**
 *
 * @author Christopher Henard
 */
public class ControllerCloseEditConstraints extends AbstractAction{

    private ViewPLEDGE viewPLEDGE;

    public ControllerCloseEditConstraints(ViewPLEDGE viewPLEDGE) {
        this.viewPLEDGE = viewPLEDGE;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        viewPLEDGE.displayViewEditConstraints(false);
    }
    
}
