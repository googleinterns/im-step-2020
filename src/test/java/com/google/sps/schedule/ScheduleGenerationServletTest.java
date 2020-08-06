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
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;

/** Check the logic when we have that parameter and when we don't. */
/** ScheduleGenerationTest
 * Check checkdateisavalid --> check that certain days are valid based on preferences
 * Check getStartInformationForEvent --> multiple checks for a given day
 * - No time available, Some time available, 
 * 
 *  
 */
public class ScheduleGenerationServletTest extends Mockito {

  @Test
  public void testcheckDateIsValid() throws Exception {

    List<Integer> validDays = Arrays.asList(1,2,3,4,5);

    // Creates a new Schedule Generation
    Boolean actual = new ScheduleGenerationServlet().checkDateIsValid(validDays, 1);
    
    assertEquals(true, actual);

    // Creates a new Schedule Generation
    actual = new ScheduleGenerationServlet().checkDateIsValid(validDays, 7);
    
    assertEquals(false, actual);
  }

}