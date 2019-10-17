package com.resy

object BookingDetails {
  //Your user profile Auth Token
  val auth_token = ""
  //Your user profile API key
  val api_key = ""
  //RestaurantId where you want to make the reservation
  val venueId = ""
  //YYYY-MM-DD of reservation
  val day = "2018-01-01"
  //Seq of HH:MM:SS times of reservations in military time format
  val times = Seq(
    "18:00:00",
    "18:15:00",
    "18:30:00",
    "18:45:00",
    "19:00:00",
    "19:15:00",
    "19:30:00",
    "19:45:00",
    "20:00:00")
  //Size of party
  val partySize = "2"
}
