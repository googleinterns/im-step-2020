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

function displayCalendar() {
    var src="https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23ffffff&amp;ctz=America%2FLos_Angeles&amp;src=bWFyaWNhcm9sQGdvb2dsZS5jb20&amp;src=Y19pZmpiYzdmZ21ha25zZ3ZpMmlmc2xqcXBoOEBncm91cC5jYWxlbmRhci5nb29nbGUuY29t&amp;color=%23039BE5&amp;color=%237986CB";

    const calendarElement = document.getElementById('calendar-display');
    calendarElement.innerHTML = '';
    calendarElement.appendChild(createCalendarIFrame(src));

}

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
        