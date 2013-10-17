/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.aria;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class AriaTextBox extends TextBox implements HasAriaLabel {

	@Override
	public void setAriaLabel(String text) {
		if (text == null || text.isEmpty())
			Roles.getTextboxRole().removeAriaLabelProperty(getElement());
		else
			Roles.getTextboxRole().setAriaLabelProperty(getElement(), text);
	}

	@Override
	public String getAriaLabel() {
		return Roles.getTextboxRole().getAriaLabelProperty(getElement());
	}
}
