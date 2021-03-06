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
var videos = 5;

var numVideos = 0;
var resourceSearchTerm;

function waitUntilScheduleGenerates() {
  // Allows for the schedule to generate before calling getVideoResults()
  if (event.keyCode == 13) {
    console.log("Waiting...");
    window.setTimeout(getVideoResults, 5000);
    window.setTimeout(getResources, 5000);
  }
}

// Utilize for scheduling videos and embedding videos
function getVideoResults() {
  var i; // Iterate over the array indexes
  let directUrls = [] // URLs to be put on the schedule
  const dataListElement = document.getElementById('videoResults'); // Location to embed videos on the page
  dataListElement.innerHTML = '';
  
  // Anytime we fetch with this function we will be grabbing from the current page
  sendNumOfVideos([new Number(numVideos), new Boolean(false)]) 
  fetch('/videoQuery').then(response => response.json()).then((data) => {
    if (dataListElement.innerHTML == '') {
      for (i=0; i<data.length; i++) {
        // Even URLs are embedded, Odd are Direct URLs
        if (i % 2 == 0) {
          console.log("Embed", data[i]);
          // Send embedded links to iframes on index.html
          dataListElement.appendChild(createIFrame(data[i]));
        }
        else {
          console.log("Direct", data[i]);
          // Add direct URLs to an array
          directUrls.push(data[i]);
        }
      }
    }
    // Send direct URLs to be scheduled
    sendURLsToEvents(directUrls);
  });
}

// Utilize to embed different videos that are NOT scheduled
function getMoreVideos() {
  var i;
  const dataListElement = document.getElementById('videoResults');
  dataListElement.innerHTML = '';
  // Anytime we fetch with this function we will be grabbing from the next page
  sendNumOfVideos([new Number(numVideos), new Boolean(true)]) 
  fetch('/videoQuery').then(response => response.json()).then((data) => {
    if (dataListElement.innerHTML == '') {
      for (i=0; i<data.length; i++) {
        // Even URLs are embedded, Odd are Direct URLs
        if (i % 2 == 0) {
          console.log("Embed", data[i]);
          // Send embedded links to iframes on index.html
          dataListElement.appendChild(createIFrame(data[i]));
        }
      }
    }
  });
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

async function sendNumOfVideos(num) {
  try {
    const response = await fetch('/videoQuery', {
      method: 'POST',
      headers: {
        'Content-Type' : 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(num)
    });
    const data = await response.json();
    if (data === true) {
      console.log("POST Request Succesful");
    }
  }
  catch (e){
    console.log("Failed");
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
        if (data.error != null) {
          window.location.pathname = "/request-permission";
          return;
        }

        displayCalendar(data.main, data.study, data.timezone);
    }
    catch (e) {
        console.log("Calendar IDs fetch failed! " + e);
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

    /*color=%23039BE5&amp;color=%237986CB*/
    iFrameElement.src = decoded;
    iFrameElement.style = "border:solid 1px #777";
    iFrameElement.width = "600";
    iFrameElement.height = "750";
    iFrameElement.frameborder = "0";
    iFrameElement.scrolling = "no";
    return iFrameElement;
}

// This function simply grabs the input when the user presses enter.
function setSearchInput(event) {
  const input = document.getElementsByName("keywords-input");
  if (event.keyCode === 13) {
    const searchTerm = event.target.value.replace(/\n/g, "").trim();
    if (searchTerm === "") return;
    if (confirm("Are you sure you would like to generate a schedule for '" + searchTerm + "'?")) {
      input.value = ""; // User confirmed request. 
      generateSchedule(searchTerm);
      console.log("Schedule Generated!");
    } else {
      input.value = searchTerm; // User canceled.
    }
  }
}

// This function passes important information to ResourceServlets to get resource information.
async function generateSchedule(searchKeyword) {
  // Grab current Calendar settings relevant to generate the schedule
  var response = await fetch(`/get-calendar-settings?searchKeyword=${searchKeyword}`);
  const resourceInformation = await response.json();

  // Grab number of videos and set them
  const numberOfVideos = resourceInformation.numberOfVideos;
  numVideos = numberOfVideos;
  console.log(numberOfVideos);
  getVideoResults();

  // Get relevant video results.
  document.getElementById("loader").style.visibility = "visible";
  console.log("Waiting for schedule to finish!")
  response = await fetch(`/schedule-handler?generate=true`);
  console.log("Finished generating the schedule!");
  document.getElementById("loader").style.visibility = "hidden";
}

// Fetch the Book resource data from BookServlet to post on Home page
function getResources() {
  console.log("Good news! You're getting resources!");
  var i;
  var page = 1;
  var dataListElement = document.getElementById('Page'+page);
  for (i=1; i < 6; i++) {
    dataListElement = document.getElementById('Page'+i);
    dataListElement.innerHTML = '';
  }
  fetch('/bookQuery').then(response => response.json()).then((data) => {
    console.log(data);
    if (dataListElement.innerHTML == '') {
      for (i=0; i<data.length; i++) {
        if(i%5 == 0){
            dataListElement = document.getElementById('Page'+ page++);
        }
        dataListElement.appendChild(
          createHyperLink(data[i]));
        console.log("LINK RETRIEVED: " + data[i]);
        dataListElement.appendChild(document.createElement('br'));
      }
    }
  });
}

function createHyperLink(link) {
  const aElement = document.createElement('a');
  var linkText = document.createTextNode(link.Title);
  aElement.appendChild(linkText);
  aElement.title = link.Title;
  aElement.href = link.URL;
  return aElement;
}



// Handle Book Link Display on Home Page using Tabs

function openResource(evt, pageName) {
  var i, tabcontent, tablinks;
  // Get all elements with class="tabcontent" and hide them
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  // Get all elements with class="tablinks" and remove the class "active"
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  // Show the current tab, and add an "active" class to the button that opened the tab
  document.getElementById(pageName).style.display = "block";
  evt.currentTarget.className += " active";
}

async function setLearningLevel() {
  const ele = document.getElementsByName("intensity"); 
              
  for (var i = 0; i < ele.length; i++) { 
    if (ele[i].checked) {
      const response = await fetch(`/updateLearningLevel?set=true&level=${ele[i].value}`);
      const data = await response.json();
    }
  } 
}

async function getLearningLevel() {
  const ele = document.getElementsByName("intensity"); 
  const notes = document.getElementById("notes");
  const response = await fetch(`/updateLearningLevel?set=false&setNotes=false&level=-1`);
  const data = await response.json();

  for (var i = 0; i < ele.length; i++) { 
    if (ele[i].value === data.level) {
      ele[i].checked = true;
    }
  } 
  notes.value = data.notes;
}

async function setNotes() {
  const notes = document.getElementById("notes");
  const response = await fetch(`/updateLearningLevel?set=false&setNotes=true&notes=${notes.value}`);
}

function delay() {
    var timer = null;
    clearTimeout(timer); 
    timer = setTimeout(setNotes, 1000)
}
