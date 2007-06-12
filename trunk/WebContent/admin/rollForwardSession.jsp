<%-- 
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
 --%>
<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page import="org.unitime.timetable.form.RollForwardSessionForm"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
 
<html> 
	<head>
		<title>Roll Forward Session</title>
	</head>
	<body>
	<%// Get Form 
			String frmName = "rollForwardSessionForm";
			RollForwardSessionForm frm = (RollForwardSessionForm) request
					.getAttribute(frmName);
%>
<% if (frm.isAdmin()) {  %>
		<html:form action="/rollForwardSession">
		<TABLE border="0" cellspacing="5" cellpadding="5">
		<logic:messagesPresent>
		<TR>
			<TD align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		
		<tr>
			<td valign="top" nowrap ><b>Session To Roll Foward To: </b>
			<html:select style="width:200;" property="sessionToRollForwardTo">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
		<td>&nbsp;
		</td>
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDatePatterns"/> Roll Date Pattern Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollDatePatternsForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardTimePatterns"/> Roll Time Pattern Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollTimePatternsForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDepartments"/> Roll Departments Forward From Session: 
			<html:select style="width:200;" property="sessionToRollDeptsFowardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardManagers"/> Roll Manager Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollManagersForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardRoomData"/> Roll Building and Room Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollRoomDataForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardSubjectAreas"/> Roll Subject Areas Forward From Session: 
			<html:select style="width:200;" property="sessionToRollSubjectAreasForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardInstructorData"/> Roll Instructor Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollInstructorDataForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardCourseOfferings"/> Roll Course Offerings Forward From Session: 
			<html:select style="width:200;" property="sessionToRollCourseOfferingsForwardFrom">
			<html:optionsCollection property="sessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>

		<tr><td>&nbsp;<br>&nbsp;<br></td></tr>
		<tr>
			<td>
				&nbsp;&nbsp;&nbsp;
				<logic:equal name="<%=frmName%>" property="admin" value="true">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="op" accesskey="M" styleClass="btn">
						<bean:message key="button.rollForward" />
					</html:submit>
				</logic:equal>
			</TD>
		</TR>
		</TABLE>
		</html:form>
		<% } else { %>
		<b>User must be an administrator to roll foward to a session.</b>
		<% } %>
	</body>
</html>

