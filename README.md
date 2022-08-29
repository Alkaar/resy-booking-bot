# resy-booking-bot
## Introduction
This is a reservation booking bot designed to snipe reservations from [Resy](https://resy.com/) using the Resy API. New
reservations usually become available on a daily basis. Some restaurants may vary on what time and how many days out 
reservations are made available. When running the bot, it will sleep until the specified time and wake up to try to 
snipe a reservation. It will attempt to grab a reservation for a couple of seconds and shutdown, outputting whether is 
it was or wasn't successful in getting a reservation.

## Usage
You need to provide a few values before running the bot.  These values are located in the `ResyBookingBot` object at 
the top. There are comments above the variables with what needs to be provided before it can be used, but I'll list it 
here as well for clarity.
* **apiKey** - Your user profile API key. Can be found when logging into Resy if you have the web console open  in your 
 headers called `authorization`.
* **auth_token** - Your user profile authentication token when logging into Resy. Can be found when logging into Resy 
if you have the web console open in your headers called `x-resy-auth-token`.
* **date** - The date you want to make the reservation in YYYY-MM-DD format.  This should be set to the day after the 
last available day with restaurant reservations as this is the day you want to snipe for a reservation once they become 
available.
* **partySize** - Size of the party reservation
* **venueId** - The id of the restaurant you want to make the reservation at.  Can be found when viewing available
reservations for a restaurant as a query parameter in the `/find` API call if you have the web console open.
* **resTimeTypes** - Priority list of reservation times and table types. Time is in military time HH:MM:SS format. This 
allows full flexibility on your reservation preferences. For example, your priority order of reservations can be...
  * 17:00 - Patio
  * 18:00 - Indoor
  * 17:30 - Patio

  If you have no preference on table type, then simply don't set it and the bot will pick from whatever is available.
* **hour** - Hour of the day when reservations become available and when you want to snipe
* **minute** - Minute of the day when reservations become available and when you want to snipe

## How it works
The main entry point of the bot is in the `ResyBookingBot` object under the `main` function. Upon running the bot, it 
will
automatically sleep until the specified time. At the specified time, it will wake up and attempt to query for 
reservations for 10 seconds. This is because sometimes reservations are not available exactly at the same time every
day so 10 seconds is to allow for some buffer. Once reservation times are retrieved, it will try to find the best 
available time slot given your priority list of reservation times. If a time can't be booked, the bot will shutdown 
here. If a time can be booked, it will make an attempt to snipe it. If a reservation couldn't be booked, and it's still 
within 10 seconds of the original start time, it will restart the whole workflow and try to find another available 
reservation. In the event it was unable to get any reservations, the bot will automatically shutdown.

## Running the bot
There are a multitude of ways to run it, but I'll share the two most 
common ways:
- You can use the `Run` button in IntelliJ. It may automatically be able to find the main class. If not, you have to 
configure it to look under `com.resy.ResyBookingBot`.
- You can run it via `sbt`. I would recommend doing this via CLI instead of inside IntelliJ. Type `sbt` to start the  
sbt instance, then type `run`. It will have some output then bring you back to the sbt prompt. Do not exit out of the 
sbt prompt as this will kill the bot. The bot is running inside the sbt instance and will wake up at the appropriate 
time to snipe a reservation.