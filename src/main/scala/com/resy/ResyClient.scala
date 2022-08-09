package com.resy

import org.joda.time.DateTime
import play.api.libs.json.JsResult.Exception
import play.api.libs.json.{JsArray, JsError, JsValue, Json}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ResyClient(resyApi: ResyApi) {

  /** Tries to find a reservation based on the priority list of requested reservations times. Due to
    * race condition of when the bot runs and when the times become available, retry may be
    * required.
    * @param date
    *   Date of the reservation in YYYY-MM-DD format
    * @param partySize
    *   Size of the party reservation
    * @param venueId
    *   Unique identifier of the restaurant where you want to make the reservation
    * @param preferredResTimes
    *   Priority of reservation times in military time HH:MM:SS format
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
    preferredResTimes: Seq[String],
    millisToRetry: Long = (10 seconds).toMillis
  ): Try[String] = {
    val dateTimeStart = DateTime.now.getMillis

    val findResResp = Await.result(
      awaitable = resyApi.getReservations(date, partySize, venueId),
      atMost    = 5 seconds
    )

    println(s"${DateTime.now} URL Response: $findResResp")

    // Searching this JSON structure...
    // {"results": {"venues": [{"slots": [{...}, {...}]}]}}
    val results = Try(
      (Json.parse(findResResp) \ "results" \ "venues" \ 0 \ "slots").get
        .as[JsArray]
        .value
    )

    val timeLeftToRetry = millisToRetry - (DateTime.now.getMillis - dateTimeStart)

    results match {
      case Success(reservationTimes) =>
        Success(findReservationTime(reservationTimes.toSeq, preferredResTimes))
      case Failure(_) if timeLeftToRetry > 0 =>
        retryFindReservation(date, partySize, venueId, preferredResTimes, timeLeftToRetry)
      case _ =>
        throw Exception(JsError("Could not find a reservation for the given time(s)"))
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
    val resDetailsResp = Await.result(
      awaitable = resyApi.getReservationDetails(configId, date, partySize),
      atMost    = 5 seconds
    )

    println(s"${DateTime.now} URL Response: $resDetailsResp")

    val results = Try {
      val resDetails = Json.parse(resDetailsResp)

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

    results match {
      case Success(bookingDetails) =>
        Success(bookingDetails)
      case _ =>
        throw Exception(JsError("Unknown error occurred"))
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
    val bookResResp = Await.result(
      awaitable = resyApi.postReservation(paymentMethodId, bookToken),
      atMost    = 5 seconds
    )

    println(s"${DateTime.now} URL Response: $bookResResp")

    // Searching this JSON structure...
    // {"resy_token": "RESY_TOKEN", ...}
    val results =
      Try(
        (Json.parse(bookResResp) \ "resy_token").get.toString
          .drop(1)
          .dropRight(1)
      )

    results match {
      case Success(resyToken) =>
        Success(resyToken)
      case _ =>
        throw Exception(JsError("Unknown error occurred"))
    }
  }

  @tailrec
  private[this] def findReservationTime(
    reservationTimes: Seq[JsValue],
    timePref: Seq[String]
  ): String = {

    // Searching a list of JSON objects with this JSON structure...
    // {"config": {"token": "CONFIG_ID"}, "date": {"start": "2022-01-30 17:00:00"}}
    val results =
      Try(
        (reservationTimes
          .filter(x => (x \ "date" \ "start").get.toString.contains(timePref.head))
          .head \ "config" \ "token").get.toString.drop(1).dropRight(1)
      )

    results match {
      case Success(configId) =>
        println(s"${DateTime.now} Config Id: $configId")
        configId
      case Failure(_) if timePref.nonEmpty =>
        findReservationTime(reservationTimes, timePref.tail)
      case _ =>
        throw Exception(JsError("Could not find a reservation for the given time(s)"))
    }
  }
}

case class BookingDetails(paymentMethodId: Int, bookingToken: String)
