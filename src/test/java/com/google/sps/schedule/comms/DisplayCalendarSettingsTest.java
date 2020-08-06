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

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import org.json.simple.JSONObject;
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
public class DisplayCalendarSettingsTest extends Mockito {

  private HTTP HTTP =  new HTTP();

  @Test
  public void testcurrentCalendarInvalid() throws Exception {

    assertFalse(new DisplayCalendarSettingsServlet().currentCalendarIsValid("invalidAccessToken", "calendar_id"));
    assertEquals("", new DisplayCalendarSettingsServlet().checkStudyScheduleOnCalendarList("invalidAccessToken"));
    

  }

}