package com.google.sps.servlets;

import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap; // to take note of order

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;

/** Preferences that we can change. */
public class UserPreferences {
    private Time TIME = new Time();

    // The number of events that we will try to set for the week.
    public static int MAX_EVENTS_LIGHT = 3;
    public static int MAX_EVENTS_MEDIUM = 4;
    public static int MAX_EVENTS_HIGH = 5;
    public static int MAX_EVENTS_EXTREME = 6;
    public static int USER_EVENTS_CHOICE = MAX_EVENTS_LIGHT;

    // Possible study event sessions
    public Map<Integer, Integer> STUDY_SESSION_START_TIME = new LinkedHashMap<Integer, Integer>();
    public Map<Integer, Integer> STUDY_SESSION_LENGTH = new LinkedHashMap<Integer, Integer>();

    // Set description if no resources could be found
    public static String DESCRIPTION = "Read for motivation: https://medium.com/dev-genius/why-you-should-stop-watching-tutorials-and-reading-docs-escaping-tutorial-purgatory-2c6b438b418 \n\n Best recommended free site: https://www.codecademy.com/\n\nBest recommended paid site: https://www.udemy.com/";
    
    // Fixer Function method of correcting events
    public static enum FixerCorrection { DELETE_EVENT, FIND_NEXT_AVAIL_TIME, FORCE_MOVE_TO_DAY, LEAVE_AS_IS };
    public static FixerCorrection FIXER_CORRECTION_CHOICE = FixerCorrection.FIND_NEXT_AVAIL_TIME;

    // Set time points of where we will try to schedule an event -- 24HR TIME
    public void applyStudySessionStartTimes() { 
        STUDY_SESSION_START_TIME.put(9, 0); // 9AM
        STUDY_SESSION_START_TIME.put(12, 0); // 12PM
        STUDY_SESSION_START_TIME.put(16, 0); // 4PM
        STUDY_SESSION_START_TIME.put(19, 0); // 7PM
    }
    

    // Set Durations Of Our Study Sessions
    public void applyDurationStartTimes() {
        // hours, minutes
        STUDY_SESSION_LENGTH.put(1, 0); // 1 hour
        STUDY_SESSION_LENGTH.put(0, 30); // 30 minutes
    }
}


