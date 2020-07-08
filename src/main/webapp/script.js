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
  if (clicked == false) {
      fetch('/videoResults').then(response => response.json()).then((data) => {
      const dataListElement = document.getElementById('videoResults');
      dataListElement.innerHTML = '';
      console.log(dataListElement.innerHTML);
      if (dataListElement.innerHTML == '') {
        for (i=0; i<data.length; i++) {
          dataListElement.appendChild(
            createIFrame(data[i]));
        }
      }
    });
  }
  clicked = true;
}

function createIFrame(text) {
  const liElement = document.createElement('iframe');
  liElement.src = text;
  return liElement;
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
        