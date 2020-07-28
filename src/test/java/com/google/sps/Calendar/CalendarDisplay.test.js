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

*/

const getSrcLastString = require("///home/maricarol/im-step-2020/src/main/webapp/script.js");

test("Returns the End section of the calendar src link", () => {
    expect(getSrcLastString()).toBe("color=%23616161&amp;color=%23cc94c1");
});