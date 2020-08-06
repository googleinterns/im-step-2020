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


/*  Examples

const getAboutUsLink = require("./index");
test("Returns about-us for english language", () => {
    expect(getAboutUsLink("en-US")).toBe("/about-us");
});


const FUNCTIONNAMEthatwewanttoimport = require("./script");
test("Description/Name of the test", () => {
    expect(FUNCTIONNAME("parameter")).toBe("return value");
});

test("Description/Name of the test", () => {
    var param = "";
    var expected = "";
    expect(functions.name(param)).toBe(expected);
});

if (typeof exports !== undefined) {
  module.exports = {displayCalendarHandler, getCalendarSrcString, getSrcFirstString, 
                    getSrcMainCalString, getSrcStudyCalString, getSrcLastString, 
                    getResources, createHyperLink}
}

*/

const functions = require("///home/maricarol/im-step-2020/src/main/webapp/script.js");

test("Returns complete calendar src link needed to build calendar iframe", () => {
    var param1 = "maricarol@google.com";
    var param2 = "c_cb8r480sletjmcd8ojvc50fbbo@group.calendar.google.com";
    var param3 = "America/Los_Angeles";
    var expected = "https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23ffffff&amp;ctz=America%2FLos_Angeles&amp;src=maricarol%40google.com&amp;src=c_cb8r480sletjmcd8ojvc50fbbo%40group.calendar.google.com&amp;color=%23616161&amp;color=%23cc94c1";
    expect(functions.getCalendarSrcString(param1, param2, param3)).toBe(expected);
});

test("Returns the Beginning section of the calendar src link", () => {
    var param = "America/Los_Angeles";
    var expected = "https://calendar.google.com/calendar/embed?height=600&amp;wkst=1&amp;bgcolor=%23ffffff&amp;ctz=America%2FLos_Angeles&amp;";
    expect(functions.getSrcFirstString(param)).toBe(expected);
});

test("Returns the section of the calendar src link referring to the user's main calendar", () => {
    var param = "maricarol@google.com";
    var expected = "src=maricarol%40google.com&amp;";
    expect(functions.getSrcMainCalString(param)).toBe(expected);
});

test("Returns the section of the calendar src link referring to the user's study calendar", () => {
    var param = "c_cb8r480sletjmcd8ojvc50fbbo@group.calendar.google.com";
    var expected = "src=c_cb8r480sletjmcd8ojvc50fbbo%40group.calendar.google.com&amp;"
    expect(functions.getSrcStudyCalString(param)).toBe(expected);
});

test("Returns the End section of the calendar src link", () => {
    expect(functions.getSrcLastString()).toBe("color=%23616161&amp;color=%23cc94c1");
});