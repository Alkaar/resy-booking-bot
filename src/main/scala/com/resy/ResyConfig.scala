package com.resy

object ResyConfig {

  val resyKeys: ResyKeys = ResyKeys(
    // Your user profile API key which can be found via your browser web console in your headers
    // called "authorization"
    apiKey = ???,
    // Your user profile authentication token which can be found via your browser web console in
    // your headers called "x-resy-auth-token"
    authToken = ???
  )

  val resDetails: ReservationDetails = ReservationDetails(
    // Date of the reservation in YYYY-MM-DD format
    date = ???,
    // Size of the party reservation
    partySize = ???,
    // Unique identifier of the restaurant where you want to make the reservation
    venueId = ???,
    // Priority list of reservation times and table types. Time is in military time HH:MM:SS format.
    // If no preference on table type, then simply don't set it.
    resTimeTypes = ???
  )

  val snipeTime: SnipeTime = SnipeTime(
    // Hour of the day when reservations become available and when you want to snipe
    hours = ???,
    // Minute of the day when reservations become available and when you want to snipe
    minutes = ???
  )
}
