package com.resy

object BookingDetails {
  // Your user profile Auth Token
  val auth_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJleHAiOjE2NTg5NzA0MzksInVpZCI6MzIxNTEyMDAsImd0IjoiY29uc3VtZXIiLCJncyI6W10sImV4dHJhIjp7Imd1ZXN0X2lkIjoxMTM1NTU0NTh9fQ.ABlv34Puy0XiWmP_VxiOt0pGf3Xf-c72I8aKH-Ov2Bv5VactO-pJThkgXtKle217Uyj2ceoOiRAkVXRVluGwXpZgABtEFRMEPnUKrgqG-N8z6vSIVrQ5nP1ZfDbWMpDxLKtAcD0F3JB3AmRv--tbCK1aHdrBjiqVGgrN_d2F_5Ho3VJK"
  // Your user profile API key
  val api_key = "VbWk7s3L4KiK5fzlO7JD3Q5EYolJI7n5"
  // RestaurantId where you want to make the reservation
  val venueId = "6194"
  // YYYY-MM-DD of reservation
  val day = "2022-07-13"
  // Seq of HH:MM:SS times of reservations in military time format
  val times = Seq(
  "17:00:00",
    "17:15:00",
    "17:30:00",
    "17:45:00",
    "18:00:00",
    "18:15:00",
    "18:30:00",
    "18:45:00",
    "19:00:00",
    "19:15:00",
    "19:30:00",
    "19:45:00",
    "20:00:00"
  )
  // Size of party
  val partySize = "2"
}
