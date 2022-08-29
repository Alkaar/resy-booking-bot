package com.resy

import org.apache.logging.log4j.scala.Logging
import org.joda.time.DateTime

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ResyBookingWorkflow(resyClient: ResyClient, resDetails: ReservationDetails) extends Logging {

  def run(millisToRetry: Long = (10 seconds).toMillis): Try[String] =
    runnable(millisToRetry, DateTime.now.getMillis)

  @tailrec
  private[this] def runnable(millisToRetry: Long, dateTimeStart: Long): Try[String] = {
    logger.info("Taking the shot...")
    logger.info("(҂‾ ▵‾)︻デ═一 (˚▽˚’!)/")
    logger.info(s"Attempting to snipe reservation")

    val maybeConfigId = resyClient.findReservations(
      date         = resDetails.date,
      partySize    = resDetails.partySize,
      venueId      = resDetails.venueId,
      resTimeTypes = resDetails.resTimeTypes
    )

    maybeConfigId match {
      case Success(configId) =>
        val maybeResyTokenResp = snipeReservation(resyClient, resDetails, configId)

        maybeResyTokenResp match {
          case Failure(_) if millisToRetry > DateTime.now.getMillis - dateTimeStart =>
            runnable(dateTimeStart, millisToRetry)
          case maybeResyToken => maybeResyToken
        }
      case error => error
    }
  }

  private[this] def snipeReservation(
    resyClient: ResyClient,
    resDetails: ReservationDetails,
    configId: String
  ): Try[String] = {
    for {
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
