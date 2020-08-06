package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Datastore {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Checks to make sure we have user settings in database
    public Entity getUser(String primary_id) {
        Key key = KeyFactory.createKey("User_Settings", primary_id);
 
        Entity user = null;
        try {
            user = datastore.get(key);
        } catch (Exception e) {
            System.out.println("Did not find user " + e);
        }
        return user;
    }
    
    // Adds a user to database
    public Entity addUser(String primary_id) {
        Entity e = new Entity("User_Settings", primary_id);
        datastore.put(e);
        applyDefaults(primary_id);

        return e;
    }

    // Adds a user in case we don't have them in database
    public void manageUserSettings(String primary_id) {
        if (getUser(primary_id) == null) addUser(primary_id);
    }

    // Applies default settings to user
    public void applyDefaults(String primary_id) {
        Key key = KeyFactory.createKey("User_Settings", primary_id);
 
        try {
            Entity user = datastore.get(key);

            user.setProperty("startDay", "1");
            user.setProperty("startWeek", "0");

            // Study schedule intensity
            user.setProperty("userEventsChoice", "3");

            // Description
            user.setProperty("description", new StringBuilder(
                "Need some motivation: https://medium.com/dev-genius/why-you-should-stop-watching-tutorials-and-reading-docs-escaping-tutorial-purgatory-2c6b438b418\n\n" +
                "Looking for extra free material?: https://www.codecademy.com/\nhttps://www.khanacademy.org/\n\n" + 
                "Eager to pay for material?: https://www.udemy.com/\n\n" +
                "Interview Prep?: https://leetcode.com/problemset/all/").toString());
            
            // Possible study event session start times
            List<List<Integer>> STUDY_SESSION_START_TIME = new ArrayList<List<Integer>>();
            STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(9, 0))); // 9 AM
            STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(12, 0))); // 12 AM
            STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(16, 0))); // 4 PM
            STUDY_SESSION_START_TIME.add(new ArrayList<Integer>(Arrays.asList(19, 0))); // 7 PM
            Gson g = new Gson();
            String json = g.toJson(STUDY_SESSION_START_TIME);
            user.setProperty("start", json);
            

            // Possible study event session durations
            List<List<Integer>> STUDY_SESSION_LENGTH = new ArrayList<List<Integer>>();
            STUDY_SESSION_LENGTH.add(new ArrayList<Integer>(Arrays.asList(1, 0))); // 1 hour
            STUDY_SESSION_LENGTH.add(new ArrayList<Integer>(Arrays.asList(0, 30))); // 30 minutes
            json = g.toJson(STUDY_SESSION_LENGTH);
            user.setProperty("length", json);

            user.setProperty("onDays", "[1,2,3,4,5,6,7]");
            user.setProperty("resources", "");
            user.setProperty("eventLookSpan", "7");
            user.setProperty("eventRecurrenceLength", "4");
            user.setProperty("fixerCorrectionChoice", "1");
            user.setProperty("deleteOverlappingEvents", "false");

            user.setProperty("notes", "");
            datastore.put(user);
        } catch (Exception e) {
            System.out.println("Unable to apply default properties because of: " + e);
        }
    }

    // Get specific user setting from database.
    // Returns a JSON String! Use Gson to convert back to your specified object.
    public String getUserSetting(String primary_id, String setting) {
        Key key = KeyFactory.createKey("User_Settings", primary_id);
 
        String prop = "";
        try {
            Entity e = datastore.get(key);
            prop = (String) e.getProperty(setting);
            
        } catch (Exception e) {
            System.out.println("Unable to get property because of: " + e);
        }
        return prop;
    }

    // The properties that this can UPDATE include: currentYouTubeToken, description, notes + nested arrays
    public void updateTextProperty(String primary_id, String setting, String text) {
        Key key = KeyFactory.createKey("User_Settings", primary_id);
        try {
            Entity e = datastore.get(key);
            e.setProperty(setting, text);
            datastore.put(e);
        } catch (Exception e) {
            System.out.println("Unable to update TEXT, " + setting +  " property because of: " + e);
        }
    }

    

    
}