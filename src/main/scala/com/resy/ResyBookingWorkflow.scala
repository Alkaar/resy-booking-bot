package com.resy

import org.joda.time.DateTime

object ResyBookingWorkflow {

  def run(
    resyClient: ResyClient,
    resDetails: ReservationDetails
  ): Unit = {
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
    } yield {
      println(s"Successfully sniped reservation at ${DateTime.now}")
      println(s"Resy token is $resyToken")
    }

    println("Shutting down Resy Booking Bot at " + DateTime.now)
    System.exit(0)
  }
}
