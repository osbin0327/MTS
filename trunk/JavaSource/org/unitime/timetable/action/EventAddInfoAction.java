/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

package org.unitime.timetable.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.form.EventAddInfoForm;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("/eventAddInfo")
public class EventAddInfoAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		EventAddInfoForm myForm = (EventAddInfoForm) form;

		//Verification of user being logged in
		sessionContext.checkPermissionAnyAuthority(Right.Events);
		
		myForm.setMainContactLookup(sessionContext.hasPermissionAnyAuthority(Right.EventLookupContact));
		if (!myForm.getMainContactLookup()) {
		    myForm.setMainContactExternalId(sessionContext.getUser().getExternalUserId());
		    TimetableManager m = TimetableManager.findByExternalId(sessionContext.getUser().getExternalUserId());
		    EventContact c = EventContact.findByExternalUniqueId(sessionContext.getUser().getExternalUserId());
		    if (c!=null) {
                if (myForm.getMainContactFirstName()==null || myForm.getMainContactFirstName().length()==0)
                    myForm.setMainContactFirstName(c.getFirstName());
                if (myForm.getMainContactMiddleName()==null || myForm.getMainContactMiddleName().length()==0)
                    myForm.setMainContactMiddleName(c.getMiddleName());
                if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
                    myForm.setMainContactLastName(c.getLastName());
                if (myForm.getMainContactEmail()==null || myForm.getMainContactEmail().length()==0)
                    myForm.setMainContactEmail(c.getEmailAddress());
                if (myForm.getMainContactPhone()==null || myForm.getMainContactPhone().length()==0)
                    myForm.setMainContactPhone(c.getPhone());
		    } else if (m!=null) {
		        if (myForm.getMainContactFirstName()==null || myForm.getMainContactFirstName().length()==0)
		            myForm.setMainContactFirstName(m.getFirstName());
                if (myForm.getMainContactMiddleName()==null || myForm.getMainContactMiddleName().length()==0)
                    myForm.setMainContactMiddleName(m.getMiddleName());
		        if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
		            myForm.setMainContactLastName(m.getLastName());
		        if (myForm.getMainContactEmail()==null || myForm.getMainContactEmail().length()==0)
		            myForm.setMainContactEmail(m.getEmailAddress());
		    } else {
		        if (myForm.getMainContactLastName()==null || myForm.getMainContactLastName().length()==0)
		            myForm.setMainContactLastName(sessionContext.getUser().getName());
		    }
		}

//Operations		
		String iOp = myForm.getOp();
		if (iOp!=null) {
			
			if ("Change Selection".equals(iOp)) {
				myForm.save(request.getSession());
				request.setAttribute("back", "eventAddInfo");
				return mapping.findForward("back");
			}

			if ("Submit".equals(iOp)) {
	        	ActionMessages errors = myForm.validate(mapping, request);
	        	if (!errors.isEmpty()) {
	        		saveErrors(request, errors);
	        	} else {
	        		myForm.submit(request, sessionContext);
	        		myForm.cleanSessionAttributes(request.getSession());
	        		response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
	        		return null;
	        	}
			}
			
			if ("Update".equals(iOp)) {
	        	ActionMessages errors = myForm.validate(mapping, request);
	        	if (!errors.isEmpty()) {
	        		saveErrors(request, errors);
	        	} else {
	        		myForm.update(request, sessionContext);
	        		myForm.cleanSessionAttributes(request.getSession());
	        		response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
	        		return null;
	        	}
			}
			
			if ("Cancel Event".equals(iOp)) {
        		myForm.cleanSessionAttributes(request.getSession());			
				return mapping.findForward(myForm.getStartTime()>=0?"eventList":"eventGrid");
			}
		
			if ("Cancel".equals(iOp)) {
				myForm.cleanSessionAttributes(request.getSession());
				response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getEventId()));
				return null;
			}
		}
		
		return mapping.findForward("show");
	}
	
}