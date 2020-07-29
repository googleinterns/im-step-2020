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

/** This is our storage for the User's current settings.
 * The ScheduleGeneration takes these into account!
 *  
 * Currently, the settings.html page displays an incorrect view of the settings.
 * Due to the fact that it's statically generated.
 * 
 * @param none
 * @return none 
*/
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

    // User Current Resources
    public static List<String> resources = new ArrayList<String>();

    // The day(s) where we will try to schedule study sessions.
    public static enum  STUDY_SESSION_DAYS { WEEKDAY, WEEKEND, CUSTOM, NONE };
    public static List<Integer> STUDY_SESSION_CUSTOM_DAYS = new ArrayList<Integer>();
    public static STUDY_SESSION_DAYS STUDY_SESSION_DAYS_CHOICE = STUDY_SESSION_DAYS.NONE;

    // The SPAN of the number of days that we try to set events for.  
    // This is NOT how long our study schedule is. This is the valid day range in which we will try to evenly place events.
    public static int EVENT_LOOK_SPAN = 7;

    // How long should we repeat this event
    public static int EVENT_RECURRENCE_LENGTH = 4; // in weeks

    // Possible study event session times
    public static List<List<Integer>> STUDY_SESSION_START_TIME = new ArrayList<List<Integer>>();

    // Possible study event session durations
    public static List<List<Integer>> STUDY_SESSION_LENGTH = new ArrayList<List<Integer>>();


    // Fixer Function method of correcting events
    public static enum FixerCorrection { DELETE_EVENT, FIND_NEXT_AVAIL_TIME, NONE };
    public static FixerCorrection FIXER_CORRECTION_CHOICE = FixerCorrection.DELETE_EVENT;

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

    /** UTILITY: These 2 functions allow us to apply defaults 
     * In case the user provided nothing here 
    * 
    * @param none
    * @return none
    */
    public static void applyDefaultDuration() {
        // Durations of study session.
        STUDY_SESSION_LENGTH.clear();
        STUDY_SESSION_LENGTH.add(new ArrayList<Integer>(Arrays.asList(1, 0))); // 1 hour
        STUDY_SESSION_LENGTH.add(new ArrayList<Integer>(Arrays.asList(0, 30))); // 30 minutes
    }
    
    public static void applyDefaultStartTime() {
        // Start time of study sessions.
        STUDY_SESSION_START_TIME.clear();
        STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(9, 0))); // 9 AM
        STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(12, 0))); // 12 AM
        STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(16, 0))); // 4 PM
        STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(19, 0))); // 7 PM
    }
}


