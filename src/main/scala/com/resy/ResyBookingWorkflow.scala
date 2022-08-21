package com.resy

import org.apache.logging.log4j.scala.Logging
import org.joda.time.DateTime

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Try}

object ResyBookingWorkflow extends Logging {

  @tailrec
  def run(
    resyClient: ResyClient,
    resDetails: ReservationDetails,
    millisToRetry: Long = (10 seconds).toMillis
  ): Try[String] = {
    val dateTimeStart = DateTime.now.getMillis

    logger.info("Taking the shot...")
    logger.info("(҂‾ ▵‾)︻デ═一 (˚▽˚’!)/")
    logger.info(s"Attempting to snipe reservation")

    val maybeResyTokenResp = for {
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

    val timeLeftToRetry = millisToRetry - (DateTime.now.getMillis - dateTimeStart)

    maybeResyTokenResp match {
      case Failure(_) if timeLeftToRetry > 0 =>
        run(resyClient: ResyClient, resDetails, timeLeftToRetry)
      case maybeResyToken => maybeResyToken
    }
  }
}
