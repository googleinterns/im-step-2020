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

// Main function handling Calendar display
// Fetches User Calendar: timezone, main calendar ID, and study calendar ID
// Stores data to be used by displayCalendar()
async function displayCalendarHandler() {
    try {
        const response = await fetch('/display-calendar-settings');
        const data = await response.json();
        console.log(data);

        displayCalendar(data.main, data.study, data.timezone);
    }
    catch (e) {
        console.log("Calendar IDs fetch failed");
    }
}

// Called by displayCalendarHandler()
// Puts together src string containing calendar id's to send to the
// calendar iframe builder, then appends calendar iframe to HTML
function displayCalendar(main_id, study_id, timezone) {
    var src = getCalendarSrcString(main_id, study_id, timezone);

    const calendarElement = document.getElementById('calendar-display');
    calendarElement.innerHTML = '';
    calendarElement.appendChild(createCalendarIFrame(src));

}

// Returns complete calendar src link needed to build calendar iframe
function getCalendarSrcString(main_id, study_id, timezone) {
    var src = getSrcFirstString(timezone) + getSrcMainCalString(main_id) + getSrcStudyCalString(study_id) + getSrcLastString();
    return src;
}

// Returns the Beginning section of the calendar src link. 
// For this part of the link, recieve user's calendar timezone
// and parse it into the string
function getSrcFirstString(timezone) {
    var n = timezone.search("/");
    var country = timezone.substring(0, n);
    var city = timezone.substring(n+1);
    var first = "https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23ffffff&amp;ctz=" + country + "%2F" + city + "&amp;";
    return first;
}

// Returns the section of the calendar src link referring to
// the user's main calendar.
// For this part of the link, recieve user's main calendar ID
// and parse it into the string
function getSrcMainCalString(main_id) {
    var id = main_id.replace("@", "%40");
    var mainString = "src="+id+"&amp;";
    return mainString;
}

// Returns the section of the calendar src link referring to
// the user's study calendar.
// For this part of the link, recieve user's study calendar ID
// created by Schedule Handler and parse it into the string
function getSrcStudyCalString(study_id) {
    var id = study_id.replace("@", "%40");
    var studyString = "src="+id+"&amp;";
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

// Create & return iframe object specific to calendar id's
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