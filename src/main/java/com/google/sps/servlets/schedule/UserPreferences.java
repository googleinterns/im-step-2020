package com.google.sps.servlets;

import java.time.ZoneId;
import java.util.Arrays;
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
    
    // The starting week and day when the schedule begins from the current day.
    // EX: START_DAY = 1; START_WEEK = 0; ---> We will start polling tomorrow!
    public static int START_DAY = 1;
    public static int START_WEEK = 0;

    // The number of events that we will try to set for the week.
    public static int MAX_EVENTS_LIGHT = 3;
    public static int MAX_EVENTS_MEDIUM = 4;
    public static int MAX_EVENTS_HIGH = 5;
    public static int MAX_EVENTS_EXTREME = 6; 
    public static int USER_EVENTS_CHOICE = MAX_EVENTS_LIGHT;

    // The day(s) where we will try to schedule study sessions.
    public static enum  STUDY_SESSION_DAYS { WEEKDAY, WEEKEND, CUSTOM, NONE };
    public static List<Integer> STUDY_SESSION_CUSTOM_DAYS = new ArrayList<Integer>();
    public static STUDY_SESSION_DAYS STUDY_SESSION_DAYS_CHOICE = STUDY_SESSION_DAYS.NONE;

    // The SPAN of the number of days that we try to set events for.  
    // This is NOT how long our study schedule is. This is the valid day range in which we will try to evenly place events.
    public static int EVENT_LOOK_SPAN = 7;

    // How long should we repeat this event
    public static int EVENT_RECURRENCE_LENGTH = 4; // in weeks

    // Possible study event sessions
    public static Map<Integer, Integer> STUDY_SESSION_START_TIME = new LinkedHashMap<Integer, Integer>() {{
        put(9, 0); // 9AM
        put(12, 0); // 12AM
        put(16, 0); // 4PM
        put(19, 0); // 7PM
    }};

    public static Map<Integer, Integer> STUDY_SESSION_LENGTH = new LinkedHashMap<Integer, Integer>() {{
        put(1, 0); // 1 hour
        put(0, 30); // 30 minutes
    }};

    // Fixer Function method of correcting events
    public static enum FixerCorrection { DELETE_EVENT, FIND_NEXT_AVAIL_TIME, FORCE_MOVE_TO_DAY, LEAVE_AS_IS };
    public static FixerCorrection FIXER_CORRECTION_CHOICE = FixerCorrection.FIND_NEXT_AVAIL_TIME;

    // Set description if no resources could be found or out of resources
    public static String DESCRIPTION = new StringBuilder(
       "Need some motivation: https://medium.com/dev-genius/why-you-should-stop-watching-tutorials-and-reading-docs-escaping-tutorial-purgatory-2c6b438b418\n\n" +
       "Looking for extra free material?: https://www.codecademy.com/\nhttps://www.khanacademy.org/\n\n" + 
       "Eager to pay for material?: https://www.udemy.com/\n\n" +
       "Interview Prep?: https://leetcode.com/problemset/all/\nhttps://www.algoexpert.io/product").toString();

    public static List<Integer> getPossibleDays() {
        List<Integer> days = new ArrayList<Integer>();
        switch(STUDY_SESSION_DAYS_CHOICE) {
            case CUSTOM:
                days = STUDY_SESSION_CUSTOM_DAYS;
                break;
            case WEEKDAY:
                days = new ArrayList(Arrays.asList(1, 2, 3, 4, 5)); // Mon (1) - Fri (5)
                break;
            case WEEKEND:
                days = new ArrayList(Arrays.asList(6, 7)); // Sat (6) - Sun (7)
                break;
            default:
                days = new ArrayList<Integer>(); 
        }
        return days;
    }
}


