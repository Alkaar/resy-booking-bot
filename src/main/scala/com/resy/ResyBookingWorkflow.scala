package com.resy

import org.joda.time.DateTime

import scala.util.Try

object ResyBookingWorkflow {

  def run(
    resyClient: ResyClient,
    resDetails: ReservationDetails
  ): Try[String] = {
    println(s"Attempting to snipe reservation at ${DateTime.now}")

    for {
      configId <- resyClient.retryFindReservation(
        date              = resDetails.date,
        partySize         = resDetails.partySize,
        venueId           = resDetails.venueId,
        preferredResTimes = resDetails.preferredResTimes
      )
      bookingDetails <- resyClient.getReservationDetails(
        configId  = configId,
        date      = resDetails.date,
        partySize = resDetails.partySize
      )
      resyToken <- resyClient.bookReservation(
        bookingDetails.paymentMethodId,
        bookingDetails.bookingToken
      )
    } yield resyToken
  }
}
