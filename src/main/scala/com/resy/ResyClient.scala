package com.resy

import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ResyClient(resyApi: ResyApi) {

  import ResyClientErrorMessages._

  /** Tries to find a reservation based on the priority list of requested reservations times. Due to
    * race condition of when the bot runs and when the times become available, retry may be
    * required.
    * @param date
    *   Date of the reservation in YYYY-MM-DD format
    * @param partySize
    *   Size of the party reservation
    * @param venueId
    *   Unique identifier of the restaurant where you want to make the reservation
    * @param resTimeTypes
    *   Priority list of reservation times and table types. Time is in military time HH:MM:SS
    *   format.
    * @param millisToRetry
    *   Optional parameter for how long to try to find a reservations in milliseconds
    * @return
    *   configId which is the unique identifier for the reservation
    */
  @tailrec
  final def retryFindReservation(
    date: String,
    partySize: Int,
    venueId: Int,
    resTimeTypes: Seq[ReservationTimeType],
    millisToRetry: Long = (10 seconds).toMillis
  ): Try[String] = {
    val dateTimeStart = DateTime.now.getMillis

    val reservationTimesResp = Try {
      val response = Await.result(
        awaitable = resyApi.getReservations(date, partySize, venueId),
        atMost    = 5 seconds
      )

      println(s"${DateTime.now} URL Response: $response")

      // Searching this JSON structure...
      // {"results": {"venues": [{"slots": [{...}, {...}]}]}}
      (Json.parse(response) \ "results" \ "venues" \ 0 \ "slots").get
        .as[JsArray]
        .value
        .toSeq
    }

    val timeLeftToRetry = millisToRetry - (DateTime.now.getMillis - dateTimeStart)

    reservationTimesResp match {
      case Success(reservationTimes) if reservationTimes.nonEmpty =>
        findReservationTime(reservationTimes, resTimeTypes)
      case _ if timeLeftToRetry > 0 =>
        retryFindReservation(date, partySize, venueId, resTimeTypes, timeLeftToRetry)
      case _ =>
        Failure(new RuntimeException(cantFindResMsg))
    }
  }

  /** Get details of the reservation
    * @param configId
    *   Unique identifier for the reservation
    * @param date
    *   Date of the reservation in YYYY-MM-DD format
    * @param partySize
    *   Size of the party reservation
    * @return
    *   The paymentMethodId and the bookingToken of the reservation
    */
  def getReservationDetails(configId: String, date: String, partySize: Int): Try[BookingDetails] = {
    val bookingDetailsResp = Try {
      val response = Await.result(
        awaitable = resyApi.getReservationDetails(configId, date, partySize),
        atMost    = 5 seconds
      )

      println(s"${DateTime.now} URL Response: $response")

      val resDetails = Json.parse(response)

      // Searching this JSON structure...
      // {"user": {"payment_methods": [{"id": 42, ...}]}}
      val paymentMethodId =
        (resDetails \ "user" \ "payment_methods" \ 0 \ "id").get.toString

      println(s"${DateTime.now} Payment Method Id: $paymentMethodId")

      // Searching this JSON structure...
      // {"book_token": {"value": "BOOK_TOKEN", ...}}
      val bookToken =
        (resDetails \ "book_token" \ "value").get.toString
          .drop(1)
          .dropRight(1)

      println(s"${DateTime.now} Book Token: $bookToken")

      BookingDetails(paymentMethodId.toInt, bookToken)
    }

    bookingDetailsResp match {
      case Success(bookingDetails) =>
        Success(bookingDetails)
      case _ =>
        Failure(new RuntimeException(unknownErrorMsg))
    }
  }

  /** Book the reservation
    * @param paymentMethodId
    *   Unique identifier of the payment id in case of a late cancellation fee
    * @param bookToken
    *   Unique identifier of the reservation in question
    * @return
    *   Unique identifier of the confirmed booking
    */
  def bookReservation(paymentMethodId: Int, bookToken: String): Try[String] = {
    val resyTokenResp = Try {
      val response = Await.result(
        awaitable = resyApi.postReservation(paymentMethodId, bookToken),
        atMost    = 5 seconds
      )

      println(s"${DateTime.now} URL Response: $response")

      // Searching this JSON structure...
      // {"resy_token": "RESY_TOKEN", ...}
      (Json.parse(response) \ "resy_token").get.toString
        .drop(1)
        .dropRight(1)
    }

    resyTokenResp match {
      case Success(resyToken) =>
        println(s"Successfully sniped reservation at ${DateTime.now}")
        println(s"Resy token is $resyToken")
        Success(resyToken)
      case _ =>
        println(s"Could not snipe reservation at ${DateTime.now}")
        Failure(new RuntimeException(resNoLongerAvailMsg))
    }
  }

  @tailrec
  private[this] def findReservationTime(
    reservationTimes: Seq[JsValue],
    resTimeType: Seq[ReservationTimeType]
  ): Try[String] = {

    // Searching a list of JSON objects with this JSON structure...
    // {"config": {"type":"TABLE_TYPE","token": "CONFIG_ID"},
    // "date": {"start": "2099-01-30 17:00:00"}}
    val results =
      Try(
        (reservationTimes
          .filter(jsonObj =>
            (jsonObj \ "date" \ "start").get.toString
              .contains(resTimeType.head.reservationTime) &&
              resTimeType.head.tableType.forall(tableType =>
                (jsonObj \ "config" \ "type").get.toString.toLowerCase
                  .contains(tableType.toLowerCase)
              )
          )
          .head \ "config" \ "token").get.toString.drop(1).dropRight(1)
      )

    results match {
      case Success(configId) =>
        println(s"${DateTime.now} Config Id: $configId")
        Success(configId)
      case Failure(_) if resTimeType.nonEmpty =>
        findReservationTime(reservationTimes, resTimeType.tail)
      case _ =>
        Failure(new RuntimeException(cantFindResMsg))
    }
  }
}

object ResyClientErrorMessages {
  val cantFindResMsg      = "Could not find a reservation for the given time(s)"
  val unknownErrorMsg     = "Unknown error occurred"
  val resNoLongerAvailMsg = "Reservation no longer available"
}

final case class BookingDetails(paymentMethodId: Int, bookingToken: String)
