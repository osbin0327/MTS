/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the SOLVER_PARAMETER table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLVER_PARAMETER"
 */

public abstract class BaseSolverParameter  implements Serializable {

	public static String REF = "SolverParameter";
	public static String PROP_VALUE = "value";


	// constructors
	public BaseSolverParameter () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSolverParameter (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSolverParameter (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SolverParameterDef definition) {

		this.setUniqueId(uniqueId);
		this.setDefinition(definition);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String value;

	// many to one
	private org.unitime.timetable.model.SolverParameterDef definition;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: VALUE
	 */
	public java.lang.String getValue () {
		return value;
	}

	/**
	 * Set the value related to the column: VALUE
	 * @param value the VALUE value
	 */
	public void setValue (java.lang.String value) {
		this.value = value;
	}



	/**
	 * Return the value associated with the column: SOLVER_PARAM_DEF_ID
	 */
	public org.unitime.timetable.model.SolverParameterDef getDefinition () {
		return definition;
	}

	/**
	 * Set the value related to the column: SOLVER_PARAM_DEF_ID
	 * @param definition the SOLVER_PARAM_DEF_ID value
	 */
	public void setDefinition (org.unitime.timetable.model.SolverParameterDef definition) {
		this.definition = definition;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SolverParameter)) return false;
		else {
			org.unitime.timetable.model.SolverParameter solverParameter = (org.unitime.timetable.model.SolverParameter) obj;
			if (null == this.getUniqueId() || null == solverParameter.getUniqueId()) return false;
			else return (this.getUniqueId().equals(solverParameter.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}