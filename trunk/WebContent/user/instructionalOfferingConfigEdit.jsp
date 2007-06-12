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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.model.SimpleItypeConfig"%>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<SCRIPT language="javascript">
	<!--

		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmDelete1() {
			return confirmDelete('This operation may result in deletion of existing subparts/classes . Continue?');
		}

		function confirmDelete2() {
			return confirmDelete('This operation will delete existing subparts and associated classes . Continue?');
		}

		function confirmDelete(msg) {
			if (jsConfirm!=null && !jsConfirm) {
				document.forms[0].elements['click'].value='y'; 
				return true;
			} 
				
			if(confirm(msg)) {
				document.forms[0].elements['click'].value='y'; 
				return true;
			} 
			else {
				return false;
			}
		}

		function doClick(op, id) {
			document.forms[0].elements["hdnOp"].value=op;
			document.forms[0].elements["id"].value=id;
			document.forms[0].elements["click"].value="y";
			document.forms[0].submit();
		}
		
	// -->
</SCRIPT>

<% 
	String crsNbr = "";
	String subjArea = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
	if (session.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME)!=null )
		subjArea = session.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME).toString();
%>

<html:form action="/instructionalOfferingConfigEdit">
	<html:hidden property="configId" />
	<html:hidden property="instrOfferingId" />
	<html:hidden property="subjectArea" />
	<html:hidden property="courseNumber" />
	<html:hidden property="notOffered" />
	<html:hidden property="configCount" />
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="click" value = "n">
	<INPUT type="hidden" name="doit" value="Cancel">
	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<A  title="Back to Instructional Offering List (Alt+I)" 
							accesskey="I"
							class="l7" 
							href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<%=subjArea%>&courseNbr=<%=crsNbr%>#A<bean:write name="instructionalOfferingConfigEditForm" property="courseOfferingId" />">
						<B><bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingName" /></B></A>											
					</tt:section-title>						

					<logic:equal name="instructionalOfferingConfigEditForm" property="configId" value="0">
						<logic:equal name="subpartsExist" scope="request" value="true">
							<html:submit property="op" 
								styleClass="btn" accesskey="S" titleKey="title.saveConfig" 
								onclick="document.forms[0].elements['click'].value='y'; return true;">			
								<bean:message key="button.saveConfig" />
							</html:submit>						
						</logic:equal>
					</logic:equal>
					
					<logic:notEqual name="instructionalOfferingConfigEditForm" property="configId" value="0">
						<logic:equal name="subpartsExist" scope="request" value="true">
							<html:submit property="op" 
								styleClass="btn" accesskey="U" titleKey="title.updateConfig" 
								onclick="return (confirmDelete1());" >			
								<bean:message key="button.updateConfig" />
							</html:submit>						
						</logic:equal>
						<logic:greaterThan name="instructionalOfferingConfigEditForm" property="configCount" value="1">
							<html:submit property="op" 
								styleClass="btn" accesskey="D" titleKey="title.deleteConfig" 
								onclick="return (confirmDelete2());" >			
								<bean:message key="button.deleteConfig" />
							</html:submit>						
						</logic:greaterThan>
					</logic:notEqual>
	
					<bean:define id="instrOfferingId">
						<bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingId" />				
					</bean:define>
					 
					<html:button property="op" 
						styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
						onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
						<bean:message key="button.backToInstrOffrDetail" />
					</html:button>

				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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

		<TR>
			<TD>Configuration Name:</TD>
			<TD>
				<html:text property="name" size="10" maxlength="10" />
			</TD>
		</TR>

		<TR>
			<TD>Unlimited Enrollment:</TD>
			<TD>
				<html:checkbox property="unlimited" onclick="doClick('unlimitedEnrollment', '');" />
			</TD>
		</TR>

		<logic:notEqual name="instructionalOfferingConfigEditForm" property="unlimited" value="true" >
		<TR>
			<TD>Configuration Limit: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="limit" size="4" maxlength="4" />
			</TD>
		</TR>
		</logic:notEqual>

		<TR>
			<TD>Instructional Type:</TD>
			<TD>
				<html:select property="itype">					
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
				</html:select>
				&nbsp;
				<html:submit property="op" 
					styleClass="btn" accesskey="A" titleKey="title.addInstrType" 
					onclick="document.forms[0].elements['click'].value='y'" >
					<bean:message key="button.addInstrType" />
				</html:submit>
		</TR>

		<TR>
			<TD colspan="2">
			&nbsp;
			</TD>
		</TR>

	</TABLE>
	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<% int cols = 9; %>
		<%= request.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME)!=null 
			? request.getAttribute(SimpleItypeConfig.CONFIGS_ATTR_NAME)
			: "" %>		
		
		<TR>
			<TD colspan="<%=cols%>"><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
		</TR>
		<TR>
			<TD colspan="<%=cols%>" align="right">
				<logic:equal name="instructionalOfferingConfigEditForm" property="configId" value="0">
					<logic:equal name="subpartsExist" scope="request" value="true">
						<html:submit property="op" 
							styleClass="btn" accesskey="S" titleKey="title.saveConfig" 
							onclick="document.forms[0].elements['click'].value='y'; return true;">			
							<bean:message key="button.saveConfig" />
						</html:submit>						
					</logic:equal>
				</logic:equal>
				
				<logic:notEqual name="instructionalOfferingConfigEditForm" property="configId" value="0">
					<logic:equal name="subpartsExist" scope="request" value="true">
						<html:submit property="op" 
							styleClass="btn" accesskey="U" titleKey="title.updateConfig" 
							onclick="return (confirmDelete1());" >			
							<bean:message key="button.updateConfig" />
						</html:submit>						
					</logic:equal>
					<logic:greaterThan name="instructionalOfferingConfigEditForm" property="configCount" value="1">
						<html:submit property="op" 
							styleClass="btn" accesskey="D" titleKey="title.deleteConfig" 
							onclick="return (confirmDelete2());" >			
							<bean:message key="button.deleteConfig" />
						</html:submit>						
					</logic:greaterThan>
				</logic:notEqual>

				<bean:define id="instrOfferingId">
					<bean:write name="instructionalOfferingConfigEditForm" property="instrOfferingId" />				
				</bean:define>
				 
				<html:button property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>
					
			</TD>
		</TR>
		
	</TABLE>

</html:form>


<SCRIPT language="javascript">
	<!--

	function checkClick() {
		
		if(document.forms[0].elements["click"].value=="y")
			return true;
		else
			return false;
	}
	/*
	var frmvalidator  = new Validator("instructionalOfferingConfigEditForm");
	frmvalidator.addValidation("limit","maxlen=4");
 	frmvalidator.addValidation("limit","numeric");
 	frmvalidator.addValidation("limit","gt=-1");
	frmvalidator.setAddnlValidationFunction("checkClick"); 
	*/
	// -->
</SCRIPT>


		