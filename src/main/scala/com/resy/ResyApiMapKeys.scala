package com.resy

abstract class ResyApiMapKey(val key: String)

object ResyApiMapKeys {
  case object UserDetails extends ResyApiMapKey(key = "USER_DETAILS")
  case object FindReservation extends ResyApiMapKey(key = "FIND_RESERVATION")
}