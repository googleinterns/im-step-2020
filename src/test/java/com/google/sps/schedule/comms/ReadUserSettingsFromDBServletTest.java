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
public class ReadUserSettingsFromDBServletTest extends Mockito {
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
        new Datastore().manageUserSettings("1");
        new Datastore().manageUserSettings("2");
        new Datastore().manageUserSettings("3");
    }

    @Test
    public void testReadSettingsFromDB() {
        // Mocks request and response  
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Create a writer that sets the response to something we can check
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = null;
        try {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        } catch (Exception e) {
            System.out.println("Error in reading user settings from DB test because of " + e);
        }
        addUsersToDatabase();

        new ReadUserSettingsFromDBServlet().getSettingsFromDB("1", response);

        // Make sure we flush the servlet response
        writer.flush();
        // 
        assertTrue(stringWriter.toString().contains("false"));
    }

    @Test
    public void modifiedUserSettingsToDefault() {
        // Mocks request and response  
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Create a writer that sets the response to something we can check
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = null;
        try {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        } catch (Exception e) {
            System.out.println("Error in reading user settings from DB test because of " + e);
        }

        addUsersToDatabase();
        Datastore DB = new Datastore();
        DB.updateTextProperty("1", "onDays", "[1,2,3]");
        DB.updateTextProperty("1", "eventLookSpan", "10");
        DB.updateTextProperty("1", "length", "[[2,0],[1,15],[1,0]");

        new ReadUserSettingsFromDBServlet().getSettingsFromDB("1", response);

        // Make sure we flush the servlet response
        writer.flush();
    
        assertTrue(stringWriter.toString().contains("[1,2,3]"));
        assertTrue(stringWriter.toString().contains("[[2,0],[1,15],[1,0]"));
        assertTrue(stringWriter.toString().contains("10"));

        DB.applyDefaults("1");

        new ReadUserSettingsFromDBServlet().getSettingsFromDB("1", response);

        // Make sure we flush the servlet response
        writer.flush();
        
        assertTrue(stringWriter.toString().contains("[1,2,3,4,5,6,7]"));
        assertTrue(stringWriter.toString().contains("[[1,0],[0,30]]"));
        assertTrue(stringWriter.toString().contains("7"));

    }


}