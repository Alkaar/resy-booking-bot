[![CI](https://img.shields.io/github/actions/workflow/status/Alkaar/resy-booking-bot/ci.yml?branch=master&logo=githubactions&style=plastic)](https://github.com/Alkaar/resy-booking-bot/actions/workflows/ci.yml)
[![Release Version](https://img.shields.io/github/v/release/Alkaar/resy-booking-bot?logo=github&style=plastic)](https://github.com/Alkaar/resy-booking-bot/releases)
[![Auto Release](https://img.shields.io/badge/release-auto.svg?colorA=888888&colorB=9B065A&label=auto&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAACzElEQVR4AYXBW2iVBQAA4O+/nLlLO9NM7JSXasko2ASZMaKyhRKEDH2ohxHVWy6EiIiiLOgiZG9CtdgG0VNQoJEXRogVgZYylI1skiKVITPTTtnv3M7+v8UvnG3M+r7APLIRxStn69qzqeBBrMYyBDiL4SD0VeFmRwtrkrI5IjP0F7rjzrSjvbTqwubiLZffySrhRrSghBJa8EBYY0NyLJt8bDBOtzbEY72TldQ1kRm6otana8JK3/kzN/3V/NBPU6HsNnNlZAz/ukOalb0RBJKeQnykd7LiX5Fp/YXuQlfUuhXbg8Di5GL9jbXFq/tLa86PpxPhAPrwCYaiorS8L/uuPJh1hZFbcR8mewrx0d7JShr3F7pNW4vX0GRakKWVk7taDq7uPvFWw8YkMcPVb+vfvfRZ1i7zqFwjtmFouL72y6C/0L0Ie3GvaQXRyYVB3YZNE32/+A/D9bVLcRB3yw3hkRCdaDUtFl6Ykr20aaLvKoqIXUdbMj6GFzAmdxfWx9iIRrkDr1f27cFONGMUo/gRI/jNbIMYxJOoR1cY0OGaVPb5z9mlKbyJP/EsdmIXvsFmM7Ql42nEblX3xI1BbYbTkXCqRnxUbgzPo4T7sQBNeBG7zbAiDI8nWfZDhQWYCG4PFr+HMBQ6l5VPJybeRyJXwsdYJ/cRnlJV0yB4ZlUYtFQIkMZnst8fRrPcKezHCblz2IInMIkPzbbyb9mW42nWInc2xmE0y61AJ06oGsXL5rcOK1UdCbEXiVwNXsEy/6+EbaiVG8eeEAfxvaoSBnCH61uOD7BS1Ul8ESHBKWxCrdyd6EYNKihgEVrwOAbQruoytuBYIFfAc3gVN6iawhjKyNCEpYhVJXgbOzARyaU4hCtYizq5EI1YgiUoIlT1B7ZjByqmRWYbwtdYjoWoN7+LOIQefIqKawLzK6ID69GGpQgwhhEcwGGUzfEPAiPqsCXadFsAAAAASUVORK5CYII=&style=plastic)](https://github.com/intuit/auto)
[![Ko-fi](https://img.shields.io/badge/support_me_on_ko--fi-F16061?style=plastic&logo=kofi&logoColor=F5F5F5)](https://ko-fi.com/Alkaar)

# resy-booking-bot
## Introduction
This is a reservation booking bot designed to snipe reservations from [Resy](https://resy.com/) using the 
[Resy API](http://subzerocbd.info/). New reservations usually become available on a daily basis. Some restaurants may 
vary on what time and how many days out reservations are made available. When running the bot, it will sleep until the 
specified time and wake up to try to snipe a reservation. It will attempt to grab a reservation for a couple of 
seconds and shutdown, outputting whether is it was or wasn't successful in getting a reservation. Test addding to readme here. 

## Usage
You need to provide a few values before running the bot.  You can set these parameters in the `resyConfig.conf` file 
which is located in the `resources` folder. There are comments above the properties with what needs to be provided 
before it can be used, but I'll list it here as well for clarity.
* **apiKey** - Your user profile API key. Can be found once you're logged into Resy in most `api.resy.com` network 
calls (i.e. Try they `/find` API call when visiting a restaurant). Open your web console and look for a request header 
called `authorization`.
* **auth_token** - Your user profile authentication token when logging into Resy. Can be found once you're logged into 
Resy in most `api.resy.com` network calls (i.e. Try the `/find` API call when visiting a restaurant). Open your web 
console and look for a request header called `x-resy-auth-token`.
* **date** - The date you want to make the reservation in YYYY-MM-DD format. This should be set to the day after the 
last available day with restaurant reservations as this is the day you want to snipe for a reservation once they 
become available.
* **partySize** - Size of the party reservation
* **venueId** - The unique identifier of the restaurant you want to make the reservation at. Can be found when viewing 
available reservations for a restaurant as a query parameter in the `/find` API call if you have the web console open.
* **resTimeTypes** - Priority list of reservation times and table types. Time is in military time HH:MM:SS format. This 
allows full flexibility on your reservation preferences. For example, your priority order of reservations can be...
  * 18:00 - Dining Room
  * 18:00 - Patio
  * 18:15

  If you have no preference on table type, then simply don't set it and the bot will pick a reservation for that time 
  slot regardless of the table type.
* **hour** - Hour of the day when reservations become available and when you want to snipe
* **minute** - Minute of the day when reservations become available and when you want to snipe

Lastly, remember to have a credit card on file in your account. Some reservations require a credit card before making 
a reservation in case of late cancellations or no-shows. Not having one will result in the snipe to fail!

## How it works
The main entry point of the bot is in the `ResyBookingBot` object under the `main` function. It utilizes the arguments 
which you need to provide in the `resyConfig.conf` file, located in the `resources` folder.  The bot runs based on the 
local time of the machine it's running on. Upon running the bot, it will automatically sleep until the specified time. 
At the specified time, it will wake up and attempt to query for reservations for 10 seconds. This is because sometimes 
reservations are not available exactly at the same time every day so 10 seconds is to allow for some buffer. Once 
reservation times are retrieved, it will try to find the best available time slot given your priority list of 
reservation times. If a time can't be booked, the bot will shutdown here. If a time can be booked, it will make an 
attempt to snipe it. If a reservation couldn't be booked, and it's still within 10 seconds of the original start time, 
it will restart the whole workflow and try to find another available reservation. In the event it was unable to get any 
reservations, the bot will automatically shutdown.

## Running the bot
There are a multitude of ways to run it, but I'll share the two most 
common ways:
- You can use the `Run` button in IntelliJ. It may automatically be able to find the main class. If not, you have to 
configure it to look under `com.resy.ResyBookingBot`.
- You can run it via `sbt`. I would recommend doing this via CLI instead of inside IntelliJ. Type `sbt` to start the  
sbt instance, then type `run`. It will have some output then bring you back to the sbt prompt. Do not exit out of the 
sbt prompt as this will kill the bot. The bot is running inside the sbt instance and will wake up at the appropriate 
time to snipe a reservation.
