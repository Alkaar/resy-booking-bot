# resy-booking-bot
## Introduction
This is a reservation booking bot designed to snipe reservations from [Resy](https://resy.com/) using the Resy API. New
reservations usually become available at midnight every day, 30 days from the current date. Some restaurants may vary 
on what time and how many days out reservations are made available. When running the bot, it will sleep until midnight 
and wake up to try to snipe a reservation. It will attempt to grab a reservation for a couple of seconds
and shutdown, outputting whether is it was or wasn't successful in getting a reservation.

## Usage
You need to provide a few values before running the bot.  These values are located in the ResyBookingBot object at the
top. There are comments above the variables with what needs to be provided before it can be used, but I'll list it here
as well for clarity.
* apiKey - Your user profile API key. Can be found when logging into Resy if you have the web console open.
* auth_token - Your user profile authentication token when logging into Resy. Can be found when logging into Resy if 
you have the web console open.
* date - The date you want to make the reservation in YYYY-MM-DD format.  This should be set to the day after the last 
available day with restaurant reservations as this is the day you want to snipe for a reservation once they become 
available.
* partySize - Size of the party reservation
* venueId - The id of the restaurant you want to make the reservation at.  Can be found when viewing available
reservations for a restaurant if you have the web console open.
* preferredResTimes - Priority list of reservation times in military time HH:MM:SS format

The main entry point of the bot is in ResyBookingBot under the main function. Upon running the bot, it will
automatically sleep until midnight the night you are running the bot. At midnight, it will wake up and attempt to query
for reservations for 10 seconds. This is because sometimes reservations are not available exactly at midnight every
night so 10 seconds is to allow for some buffer. Once times are retrieved, it will try to find the best available time
slot given your priority list of reservation times. If a time can be booked, it will make an attempt to snipe it.
Otherwise, it will report that it was unable to acquire a reservation. In the event it was unable to get any
reservations for 10 seconds, the bot will automatically shutdown.