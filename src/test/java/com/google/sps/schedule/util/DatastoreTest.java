// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;

/** Check the logic when we have that parameter and when we don't. */
/** ScheduleHandlerTest
 * Check doGet --> checks if we want to generate the schedule or not
 * Check doPost --> check that the given resources have been set
 * Check processStartTimes --> check that the given format translates correctly
 * Check processDurations --> check that given format translates correctly
 *  
 */
public class DatastoreTest extends Mockito {
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    private DatastoreService d = DatastoreServiceFactory.getDatastoreService();

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    private void addUsersToDatabase() {
        Entity user1 = new Entity("User_Settings", "1");
        Entity user2 = new Entity("User_Settings", "2");
        Entity user3 = new Entity("User_Settings", "3");
        d.put(user1);
        d.put(user2);
        d.put(user3);
    }

    @Test
    public void testgetUser() {
        addUsersToDatabase();
        assertNull(new Datastore().getUser("4"));
        assertNotNull(new Datastore().getUser("2"));
    }

    @Test
    public void testaddUser() {
        addUsersToDatabase();

        Entity e = new Datastore().addUser("4");
        assertNotNull(new Datastore().getUser("4"));
    }

    @Test
    public void testmanageUser() {
        addUsersToDatabase();

        new Datastore().manageUserSettings("4");
        assertNotNull(new Datastore().getUser("4"));
    }

    @Test
    public void testapplyDefaults() {
        // Just applied
        addUsersToDatabase();
        new Datastore().applyDefaults("1");
        assertEquals("[1,2,3,4,5,6,7]", new Datastore().getUserSetting("1", "onDays")); // check default

        // Auto applied when user added
        new Datastore().manageUserSettings("4");
        assertEquals("[[9,0],[12,0],[16,0],[19,0]]", new Datastore().getUserSetting("4", "start")); // check default

        // Modified then rest
        new Datastore().updateTextProperty("2", "notes", "Test123.");
        assertEquals("Test123.", new Datastore().getUserSetting("2", "notes"));
        new Datastore().applyDefaults("2");
        assertEquals("", new Datastore().getUserSetting("2", "notes"));
    }

    @Test
    public void testupdateTextProperty() {
        // Just applied
        addUsersToDatabase();
        new Datastore().manageUserSettings("4");
        new Datastore().updateTextProperty("4", "userEventsChoice", "5");
        assertEquals("5", new Datastore().getUserSetting("4", "userEventsChoice"));
        
        // apply defaults
        new Datastore().applyDefaults("4");
        assertEquals("3", new Datastore().getUserSetting("4", "userEventsChoice"));
        
        
    }

    
}