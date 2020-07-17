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

var clicked = false;
var calendarAttributes = [];

function getVideoResults() {
  var i;
  let directUrls = []
  if (!clicked) {
      fetch('/videoResults').then(response => response.json()).then((data) => {
      const dataListElement = document.getElementById('videoResults');
      dataListElement.innerHTML = '';
      console.log(dataListElement.innerHTML);
      if (dataListElement.innerHTML == '') {
        for (i=0; i<data.length; i++) {
          // Even URLs are embedded, Odd are Direct URLs
          if (i % 2 == 0) {
            console.log("Embed", data[i]);
            // Send embedded links to iframes on index.html
            dataListElement.appendChild(
              createIFrame(data[i]));
          }
          else {
            console.log("Direct", data[i]);
            // Add direct URLs to an array
            directUrls.push(data[i]);
          }
        }
      }
      sendURLsToEvents(directUrls);
    });
  }
  clicked = true;
}

async function sendURLsToEvents(urls) {
  // Send direct URL array to Payton's Servlet
  try {
    const response = await fetch('/schedule-handler', {
       method:'POST', 
       headers: {
         'Content-Type': 'application/json',
         'Accept': 'application/json'
        },
       body: JSON.stringify(urls)
    });
    // Make or Break on if it works
    const data = await response.json();
    if (data === true) {
      // Success
      console.log("Direct URLs sent to Payton's Servlet");
    }
  }
  catch (e) {
    // Log some errors
    console.log("fetch failed: ", e);
  }
}

function createIFrame(text) {
  const iFrameElement = document.createElement('iframe');
  iFrameElement.src = text;
  return iFrameElement;
}

function getCalendarAttributes() {
    fetch('/display-calendar-settings').then(response => response.json()).then((calendarAttrJSON) => {

        var main_id = calendarAttrJSON.main;
        var study_id = calendarAttrJSON.study;
        var timezone = calendarAttrJSON.timezone;

        console.log("**CALENDAR 1 TEST**: main_id="+calendarAttrJSON.main);
        console.log("**CALENDAR 1 TEST**: main_id="+calendarAttrJSON.study);
        console.log("**CALENDAR 1 TEST**: main_id="+calendarAttrJSON.timezone);

        calendarAttributes.push(main_id);
        calendarAttributes.push(study_id);
        calendarAttributes.push(timezone);

        
        console.log("~~in array: " + calendarAttributes[0]);
        console.log("hello???????????????");

    });
}

// Main function handling Calendar display
function displayCalendar() {
    getCalendarAttributes();

    var main_id = calendarAttributes[0];
    var study_id = calendarAttributes[1];
    var timezone = calendarAttributes[2];

    console.log("~~in array: " + calendarAttributes[0]);
    console.log("~~in array: " + calendarAttributes[1]);
    console.log("~~in array: " + calendarAttributes[2]);

    var srcFirst = getSrcFirstString("timezone");
    var srcMainCalID = getSrcMainCalString("main_id");
    var srcStudyCalID = getSrcStudyCalString("study_id");
    var srcLast = getSrcLastString();

    var src = srcFirst + srcMainCalID + srcStudyCalID + srcLast;

    const calendarElement = document.getElementById('calendar-display');
    calendarElement.innerHTML = '';
    calendarElement.appendChild(createCalendarIFrame(src));

}

// Create & return iframe object specific to calendar display
function createCalendarIFrame(src) {
    const iFrameElement = document.createElement('iframe');

    var decoded = src.replace(/&amp;/g, '&');

    iFrameElement.src = decoded;
    iFrameElement.style = "border:solid 1px #777";
    iFrameElement.width = "800";
    iFrameElement.height = "600";
    iFrameElement.frameborder = "0";
    iFrameElement.scrolling = "no";
    return iFrameElement;
}

// Returns the Beginning section of the calendar src link. 
// For this part of the link, recieve user's calendar timezone
// and parse it into the string
function getSrcFirstString(timezone) {
    //timezone = "America/Los_Angeles";

    // Parse the timezone string to insert into src
    var n = timezone.search("/");
    var country = timezone.substring(0, n);
    var city = timezone.substring(n+1);

    console.log("Pasring the timezones!... timezone="+timezone+" country="+country+" city="+city);

    var first = "https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23ffffff&amp;ctz=" + country + "%2F" + city + "&amp;";
    return first;
}

// Returns the section of the calendar src link referring to
// the user's main calendar.
// For this part of the link, recieve user's main calendar ID
// and parse it into the string
function getSrcMainCalString(mainID) {
    //var mainID = "src=maricarol%40google.com&amp;"
    //var mainID = "src=bWFyaWNhcm9sQGdvb2dsZS5jb20&amp;";
    var mainID = "maricarol@google.com";

    var id = mainID.replace("@", "%40");
    var mainString = "src="+id+"&amp;";

    console.log("IMPORTANT TEST.......REPLACE STRING: "+mainString);

    return mainString;
}

// Returns the section of the calendar src link referring to
// the user's study calendar.
// For this part of the link, recieve user's study calendar ID
// created by Schedule Handler and parse it into the string
function getSrcStudyCalString(studyID) {
    //var studyID = "src=c_g2o2196s4i3bteuk250r4mg528%40group.calendar.google.com&amp";
    //var studyID = "src=Y19uOHFnZHRkNzZvZDJiZHVkcnU1cG1idWdrMEBncm91cC5jYWxlbmRhci5nb29nbGUuY29t&amp";

    var studyID = "c_cb8r480sletjmcd8ojvc50fbbo@group.calendar.google.com";

    var id = studyID.replace("@", "%40");
    var studyString = "src="+id+"&amp;";

    console.log("IMPORTANT TEST.......REPLACE STRING: "+studyString);

    return studyString;
}

// Returns the End section of the calendar src link. 
// For this part of the link, assume user wants no color
// customization to the calendar display. This will change in
// future updates
function getSrcLastString() {
    var last = "color=%23616161&amp;color=%23cc94c1";
    return last;
}

/*
<iframe src="https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23616161&amp;ctz=America%2FLos_Angeles&amp;
src=bWFyaWNhcm9sQGdvb2dsZS5jb20&amp;src=Y19jYjhyNDgwc2xldGptY2Q4b2p2YzUwZmJib0Bncm91cC5jYWxlbmRhci5nb29nbGUuY29t&amp;
color=%23616161&amp;color=%23cc94c1" style="border:solid 1px #777" width="1000" height="600" frameborder="0" scrolling="no"></iframe>
*/

/*color=%23039BE5&amp;color=%237986CB*/