package com.resy

import org.apache.logging.log4j.scala.Logging

import scala.util.Try

object ResyBookingWorkflow extends Logging {

  def run(
    resyClient: ResyClient,
    resDetails: ReservationDetails
  ): Try[String] = {
    logger.info("Taking the shot...")
    logger.info("(҂‾ ▵‾)︻デ═一 (˚▽˚’!)/")
    logger.info(s"Attempting to snipe reservation")

    for {
      configId <- resyClient.retryFindReservation(
        date         = resDetails.date,
        partySize    = resDetails.partySize,
        venueId      = resDetails.venueId,
        resTimeTypes = resDetails.resTimeTypes
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
