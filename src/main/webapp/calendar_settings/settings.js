
function customDays() {
    if  (document.getElementById("custom").checked) {
        document.getElementById("CUSTOMDays").style.display = "block";
        document.getElementById("cd").required = true;
    } else {
        document.getElementById("CUSTOMDays").style.display = "none";
        document.getElementById("cd").required = false;
    }
}

async function setStateOfSettings() {

    try {
        const response = await fetch("/getSettingsState");
        const data = await response.json();
        console.log(data);
        putSettingsInHTML(data.settings);
    } catch (e) {
        console.log("There was an error getting the state of the settings." + e);
        //window.location.pathname = "/home.html";
        return;
    }
}

function putSettingsInHTML(data) {
    document.getElementById("startDay").value = data[7];
    document.getElementById("startWeek").value = data[8];

    /* Study intensity */
    const studyIntensity = data[9];
    const light = document.getElementById("light");
    const medium = document.getElementById("medium");
    const hard = document.getElementById("hard");
    const expert = document.getElementById("expert");

    light.checked = false;
    medium.checked = false;
    hard.checked = false;
    expert.checked = false;

    if (studyIntensity == 3) light.checked = true;
    else if (studyIntensity == 4) medium.checked = true;
    else if (studyIntensity == 5) hard.checked = true;
    else expert.checked = true;
    

    if (data[0] === null) data[0] = "";
    document.getElementById("description").value = data[0];
    document.getElementById("span").value = data[1];
    document.getElementById("recurrenceLength").value = data[2];

    document.getElementById("times").value = parseStartTimes(data[6]);
    document.getElementById("durations").value = parseDurations(data[3]);
    document.getElementById("overlapping").checked = JSON.parse(data[10]);

    // Update days [6,7] [1,7]
    document.getElementById("none").checked = false;
    const days = JSON.parse(data[5]);
    if (days.length === 7) {
        document.getElementById("none").checked = true;
    } else {
        if (days.length === 2) {
            if (checkValidDays([6,7], days)) {
                document.getElementById("weekEnd").checked = true;
                return;
            }
        } else if (days.length === 5) {
            if (checkValidDays([1,2,3,4,5], days)) {
                document.getElementById("weekDay").checked = true;
                return;
            }
        } 
        
        document.getElementById("custom").checked = true;
        customDays();
        const select = document.getElementById("cd");
        for (var i = 0; i < days.length; i++) {
            if (days[i] === 7) {
                select.options[0].selected = true;
                continue;
            }
            select.options[days[i]].selected = true;
        }
    }
}

function parseDurations(array) {
    var startResult = "";
    const start = JSON.parse(array);
    for (time of start) {
        startResult += time[0] + "hr ";
        startResult += time[1] + "min";
        startResult +=  ", ";
    }
    
    return startResult.trim().substr(0, startResult.length - 2);
}

function parseStartTimes(array) {
    var startResult = "";
    const start = JSON.parse(array);
    for (time of start) {
        var AM = true;
        var doubleZero = false;
        if (time[0] > 12) {
            time[0] -= 12;
            AM = false;
        } else if (time[0] == 12) {
            AM = false;
        }

        if (time[1] == 0) {
            doubleZero = true;
        }
        startResult += time[0] + ":" + time[1];
        if (doubleZero) startResult += "0";
        if (AM == false) startResult += "pm";
        else startResult += "am";
        startResult +=  ", ";
    }
    
    return startResult.trim().substr(0, startResult.length - 2);
}

function checkValidDays(expectedDays, days) {
    for (var i = 0; i < days.length; i++) {
        if (days[i] !== expectedDays[i]) return false;
    }
    return true;
}