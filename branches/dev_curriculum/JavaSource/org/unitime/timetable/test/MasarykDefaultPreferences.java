/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.ExactTimeMinsDAO;
import org.unitime.timetable.model.dao._RootDAO;

public class MasarykDefaultPreferences {
    protected static Logger sLog = Logger.getLogger(MasarykDefaultPreferences.class);
    
	public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "FF"),
                    ApplicationProperties.getProperty("year","2011"),
                    ApplicationProperties.getProperty("term","Jaro")
                    );
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            MakeAssignmentsForClassEvents makePattern = new MakeAssignmentsForClassEvents(session, hibSession);
            
            for (ExactTimeMins x: ExactTimeMinsDAO.getInstance().findAll(hibSession)) {
            	x.setNrSlots(x.getMinsPerMtgMax() / 5);
            	x.setBreakTime(5);
            	hibSession.saveOrUpdate(x);
            }
            
            RoomGroup poc = null, mult = null; //, bez = null;
            for (RoomGroup rg: (Collection<RoomGroup>)RoomGroup.getAllGlobalRoomGroups()) {
            	if (rg.getAbbv().equals("POČ"))
            		poc = rg;
            	else if (rg.getAbbv().equals("MULT"))
            		mult = rg;
            	/*
            	else if (rg.getAbbv().equals("BĚŽ"))
            		bez = rg;
            	*/
            }
            
            for (Department d: session.getDepartments()) {
            	d.getDistributionPreferences().clear();
            	hibSession.saveOrUpdate(d);
            }
            
            // Hashtable<String, Set<Class_>> meetWith = new Hashtable<String, Set<Class_>>();
            
			DistributionType sameDaysType = (DistributionType)hibSession.createQuery(
			"select d from DistributionType d where d.reference = :type").setString("type", "SAME_DAYS").uniqueResult();

            
            for (SchedulingSubpart ss: (List<SchedulingSubpart>)hibSession.createQuery(
            		"select distinct s from SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
            		"co.subjectArea.department.session.uniqueId = :sessionId")
            		.setLong("sessionId", session.getUniqueId()).list()) {
            	
            	boolean hasPreferences = false;
            	if (!ss.getPreferences().isEmpty()) hasPreferences = true;
            	for (Class_ c: ss.getClasses()) {
            		if (c.getPreferences().size() > c.getPreferences(TimePref.class).size()) hasPreferences = true;
            		else for (Iterator i = c.getPreferences(TimePref.class).iterator(); !hasPreferences && i.hasNext(); ) {
            			TimePref t = (TimePref)i.next();
            			TimePatternModel m = t.getTimePatternModel();
            			if (!m.isExactTime() && !m.isDefault()) {
            				hasPreferences = true;
            			}
            		}
            	}
            	if (hasPreferences) {
            		continue;
            	}
        		sLog.info("Setting " + ss.getSchedulingSubpartLabel() + " ...");

        		if (ss.getInstrOfferingConfig().isUnlimitedEnrollment()) {
        			ss.getInstrOfferingConfig().setUnlimitedEnrollment(false);
        			ss.getInstrOfferingConfig().setLimit(0);
            		hibSession.saveOrUpdate(ss);
        		}
        		
        		if (ss.getChildSubparts().isEmpty() && ss.getParentSubpart() != null) {
        			boolean sameDay = false;
        			classes: for (Class_ c: ss.getClasses()) {
        				int dayCode = 0;
        				while (c != null) {
        					Assignment a = c.getCommittedAssignment();
        					if (a != null) {
        						if ((dayCode & a.getDays()) != 0) { sameDay = true; break classes; }
        						dayCode |= a.getDays();
        					}
        					c = c.getParentClass();
        				}
        			}
                	DistributionPref dp = new DistributionPref();
                	dp.setDistributionType(sameDaysType);
    				dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(sameDay ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
    				dp.setDistributionObjects(new HashSet<DistributionObject>());
    				dp.setGrouping(DistributionPref.sGroupingProgressive);
    				dp.setOwner(ss.getManagingDept());
    				SchedulingSubpart x = ss;
    				int index = 1;
    				while (x != null) {
        				DistributionObject o = new DistributionObject();
        				o.setDistributionPref(dp);
        				o.setPrefGroup(x);
        				o.setSequenceNumber(index++);
        				dp.getDistributionObjects().add(o);
    					x = x.getParentSubpart();
    				}
    				hibSession.saveOrUpdate(dp);
        		}
        		
            	for (Class_ c: ss.getClasses()) {
            		Meeting m = c.getEvent().getMeetings().iterator().next();
            		int minPerMeeting = 5 + 5 * (m.getStopPeriod() - m.getStartPeriod());
            		if ((minPerMeeting + 5) % 50 == 0) minPerMeeting += 5;
            		if ((minPerMeeting - 5) % 50 == 0) minPerMeeting -= 5;
            		if (ss.getMinutesPerWk() != minPerMeeting) {
                		System.out.println(c.getClassLabel(hibSession) + " has " + ss.getMinutesPerWk() + " minutes per meeting (should have " + minPerMeeting + ").");
                		if (c.getSectionNumber() == 1) {
                			ss.setMinutesPerWk(minPerMeeting);
                			hibSession.saveOrUpdate(ss);
                		}
            		}
            	}

            	for (Class_ c: ss.getClasses()) {
            		Assignment a = c.getCommittedAssignment();
            		if (a == null) continue;
            		
            		if (c.effectiveDatePattern().getName().startsWith("import")) {
            			c.setDatePattern(makePattern.getDatePattern(c.getEvent()));
            		}
            		
            		/*
            		for (Location location: a.getRooms()) {
            			if (!(location instanceof Room)) continue;
                		String code = location.getUniqueId() + ":" + a.getDatePattern().getUniqueId() + ":" + a.getTimePattern().getUniqueId() + ":" + a.getDays() + ":" + a.getStartSlot();
                		Set<Class_> classes = meetWith.get(code);
                		if (classes == null) {
                			classes = new HashSet<Class_>();
                			meetWith.put(code, classes);
                		}
                		classes.add(c);
            		}
            		*/
            		
            		// Reset room ratio
            		c.setRoomRatio(1f);
            		// Reset preferences
            		c.getPreferences().clear();
            		// Strongly preferred room
            		TimePattern pattern = null; 
            		patterns: for (TimePattern p: TimePattern.findByMinPerWeek(session.getUniqueId(), false, false, false, c.getSchedulingSubpart().getMinutesPerWk(), null)) {
            			for (TimePatternDays d: p.getDays())
            				if (a.getDays().equals(d.getDayCode()))
                				for (TimePatternTime t: p.getTimes()) {
                					if (t.getStartSlot().equals(a.getStartSlot())) {
                						pattern = p;
                						break patterns;
                					}
                				}
            		}
            		if (pattern == null) {
            			// Exact time
            			pattern = TimePattern.findExactTime(session.getUniqueId());
            			TimePatternModel m = pattern.getTimePatternModel();
            	    	m.setExactDays(a.getDays());
            	    	m.setExactStartSlot(a.getStartSlot());
                		TimePref tp = new TimePref();
                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                		tp.setTimePatternModel(m);
                		tp.setOwner(c);
                		c.getPreferences().add(tp);
            		} else {
            			TimePatternModel m = pattern.getTimePatternModel();
            			for (int d = 0; d < m.getNrDays(); d++)
            				for (int t = 0; t < m.getNrTimes(); t++) {
            					if (a.getTimeLocation().getStartSlot() == m.getStartSlot(t) &&
            						a.getTimeLocation().getDayCode() == m.getDayCode(d)) {
            						for (int tt = Math.max(0, t - 1); tt < Math.min(m.getNrTimes(), t + 2); tt++)
            							for (int dd = 0; dd < m.getNrDays(); dd++)
                    						m.setPreference(dd, tt, PreferenceLevel.sPreferred);
            						m.setPreference(d, t, PreferenceLevel.sStronglyPreferred);
            						if (d == m.getNrDays() - 1) {
            							for (int dd = 0; dd < m.getNrDays() - 1; dd++)
            	            				for (int tt = 0; tt < m.getNrTimes(); tt++)
            	            					m.setPreference(dd, tt, PreferenceLevel.sProhibited);
            						} else {
        	            				for (int tt = 0; tt < m.getNrTimes(); tt++)
        	            					m.setPreference(m.getNrDays() - 1, tt, PreferenceLevel.sProhibited);
            						}
            					}
            				}
                		TimePref tp = new TimePref();
                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                		tp.setTimePatternModel(m);
                		tp.setOwner(c);
                		c.getPreferences().add(tp);
            		}
            		// Room preferences
            		boolean reqMult = false;
            		if ("MM".equals(c.getSchedulePrintNote())) {
            			reqMult = true;
    					RoomGroupPref gp = new RoomGroupPref();
    					gp.setOwner(c);
    					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
    					gp.setRoomGroup(mult);
        				c.getPreferences().add(gp);
            		}
            		for (Location l: a.getRooms()) {
            			if (l instanceof NonUniversityLocation) {
            				RoomPref rp = new RoomPref();
            				rp.setOwner(c);
            				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            				rp.setRoom(l);
            				c.getPreferences().add(rp);
            			} else {
            				RoomPref rp = new RoomPref();
            				rp.setOwner(c);
            				if (l.getRoomGroups().isEmpty()) {
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            				} else if (l.getCapacity() == 0) {
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
            				} else {            					
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
            				}
            				rp.setRoom(l);
            				c.getPreferences().add(rp);
            				BuildingPref bp = new BuildingPref();
            				bp.setOwner(c);
            				bp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
            				bp.setBuilding(((Room)l).getBuilding());
            				c.getPreferences().add(bp);
            				if (l.getCapacity() > 0 && l.getCapacity() < c.getClassLimit()) {
            					c.setRoomRatio(Math.round(100 * l.getCapacity() / c.getClassLimit()) / 100f);
            				} else {
            					c.setRoomRatio(0f);
            				}
            			}
            			for (RoomGroup rg: l.getRoomGroups()) {
            				if (rg.isGlobal() && rg.getAbbv().equals("MULT")) {
            					if (reqMult) continue; // already have required MULT
            					RoomGroupPref gp = new RoomGroupPref();
            					gp.setOwner(c);
            					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
            					gp.setRoomGroup(rg);
                				c.getPreferences().add(gp);
            					RoomGroupPref gp2 = new RoomGroupPref();
            					gp2.setOwner(c);
            					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
            					gp2.setRoomGroup(poc);
                				c.getPreferences().add(gp2);
            				} else if (rg.isGlobal() && rg.getAbbv().equals("POČ")) {
            					RoomGroupPref gp = new RoomGroupPref();
            					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            					gp.setOwner(c);
            					gp.setRoomGroup(rg);
                				c.getPreferences().add(gp);
            				} else if (rg.isGlobal() && rg.getAbbv().equals("BĚŽ")) {
            					RoomGroupPref gp = new RoomGroupPref();
            					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
            					gp.setOwner(c);
            					gp.setRoomGroup(rg);
                				c.getPreferences().add(gp);
            					RoomGroupPref gp2 = new RoomGroupPref();
            					gp2.setOwner(c);
            					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
            					gp2.setRoomGroup(poc);
                				c.getPreferences().add(gp2);
            				}
            			}
            		}
            		c.setNbrRooms(a.getRooms().size());
            		
            		hibSession.saveOrUpdate(c);
            	}
        		hibSession.flush();
            }
            hibSession.flush();
            
            
            /*
			DistributionType meetWithType = (DistributionType)hibSession.createQuery(
				"select d from DistributionType d where d.reference = :type").setString("type", "MEET_WITH").uniqueResult();

            for (Set<Class_> classes: meetWith.values()) {
            	if (classes.size() <= 1) continue;
            	sLog.info("Adding meet with between: " + classes);
            	DistributionPref dp = new DistributionPref();
            	dp.setDistributionType(meetWithType);
				dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				dp.setDistributionObjects(new HashSet<DistributionObject>());
				dp.setGrouping(DistributionPref.sGroupingNone);
				int index = 1;
            	for (Class_ c: classes) {
            		if (index == 1)
            			dp.setOwner(c.getManagingDept());
    				DistributionObject o = new DistributionObject();
    				o.setDistributionPref(dp);
    				o.setPrefGroup(c);
    				o.setSequenceNumber(index++);
    				dp.getDistributionObjects().add(o);
            	}
				hibSession.saveOrUpdate(dp);
            }
            
            hibSession.flush();
            */
            
            sLog.info("All done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}