/**
 * Holidays app for Hubitat
 *
 * Copyright (c) 2024 Leandro Montedoro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  v1.0.0  - Initial version
 *
 */

definition(
    name: "Holidays App",
    namespace: "lmontedoro",
    author: "Leandro Montedoro",
    description: "Simple app to Tracking Holidays",
    category: "",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage", title: "Holidays Tracker", install: true, uninstall: true) {
        section {
            paragraph "Make sure to create two <b>Boolean</b> Hub variables in <b>Settings -> Hub Variables</b>"
            paragraph "<ul><li>IsHoliday "+ printVar("IsHoliday") + "</li><li>IsHolidayTomorrow " + printVar("IsHolidayTomorrow") +"</li></ul>"
            paragraph "<i>After creation, the application will initialize these two variables as 'true' or 'false' according to the provided list of dates, allowing you to incorporate them into your rules and automation processes.</i>"
        }
        section {
            input name: "inputDateList", type: "text", title: "<b>Enter Holidays (mm/dd) separated by comma:</b>", defaultValue: "1/1, 1/16, 5/29, 7/4, 9/4, 11/23, 11/24, 12/25", submitOnChange: true
            
        }        
    }
}

// Called when app first installed
def installed() {
    //log.trace "Holidays App was installed"
}

// Called when user presses "Done" button in app
def updated() {  
    initialize()
}

// Called when app uninstalled
def uninstalled() {
   removeAllInUseGlobalVar()
   unschedule(newDayHandler) 
   //log.trace "Holidays App was uninstalled"
}

// Set Variables & Cron
void initialize() {
    removeAllInUseGlobalVar()
    addInUseGlobalVar("IsHoliday")
    addInUseGlobalVar("IsHolidayTomorrow") 
    
    newDayHandler()
    schedule("0 1 0 * * ?", newDayHandler);   
}

def printVar(varName) {
    def hubVar = getGlobalVar(varName)
    if (hubVar != null && hubVar.type == "boolean") {
        return "<span style='color:green'>OK</span>"
    } else {
        return "<span style='color:red'>NOT SET</span>"
    }
}

def newDayHandler() {
    def today = new Date()
    def tomorrow = today + 1
    
    def holidays = inputDateList.split(", ")
    
    // Get the today's month and day
    def todayMonth = (today.format("MM")).toInteger()
    def todayDay = (today.format("dd")).toInteger()
    
    // Get tomorrow's month and day
    def tomorrowMonth = (tomorrow.format("MM")).toInteger()
    def tomorrowDay = (tomorrow.format("dd")).toInteger()    
    
    setGlobalVar("IsHoliday", false)
    setGlobalVar("IsHolidayTomorrow", false)       

    // Iterate through the holidays list and check if any match the current date
    holidays.each { holidayStr ->
        def parts = holidayStr.split("/")
        def holidayMonth = parts[0].toInteger()
        def holidayDay = parts[1].toInteger()

        if (todayMonth == holidayMonth && todayDay == holidayDay) {
            log.info "Today is a holiday: $holidayStr"
            setGlobalVar("IsHoliday", true)
        }
        if (tomorrowMonth == holidayMonth && tomorrowDay == holidayDay) {
            log.info "Tomorrow is a holiday: $holidayStr"
            setGlobalVar("IsHolidayTomorrow", true)       
        }
    }    
}