package com.resy

final case class ResyKeys(apiKey: String, authToken: String)

final case class ReservationDetails(
  date: String,
  partySize: Int,
  venueId: Int,
  resTimeTypes: Seq[ReservationTimeType]
)

final case class ReservationTimeType(reservationTime: String, tableType: Option[String] = None)

object ReservationTimeType {

  def apply(reservationTime: String, tableType: String): ReservationTimeType = {
    ReservationTimeType(reservationTime, Some(tableType))
  }
}

final case class RunDetails(
  mode: String,
  scheduled: Scheduled,
  loop: Loop)

final case class Scheduled(hours: Int, minutes: Int)

final case class Loop(interval: Int, maxRetries: Int)
