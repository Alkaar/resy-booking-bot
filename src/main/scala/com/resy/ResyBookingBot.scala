package com.resy

import akka.actor.ActorSystem
import com.resy.BookingDetails._
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResyBookingBot {
  def main(args: Array[String]): Unit = {
    println("Starting Resy Booking Bot")

    val system = ActorSystem("System")
    val startOfTomorrow = DateTime.now.withTimeAtStartOfDay.plusDays(1).getMillis
    val millisUntilTomorrow = startOfTomorrow - DateTime.now.getMillis
    val hoursRemaining = millisUntilTomorrow/1000/60/60
    val minutesRemaining = millisUntilTomorrow/1000/60 - hoursRemaining * 60
    val secondsRemaining = millisUntilTomorrow/1000 - hoursRemaining * 60 * 60 - minutesRemaining * 60

    println(s"Current time: ${DateTime.now}")
    println(s"Sleeping for $hoursRemaining hours, $minutesRemaining minutes and $secondsRemaining seconds")
    bookReservationWokrflow
//    system.scheduler.scheduleOnce(millisUntilTomorrow millis)(bookReservation)
  }

  def bookReservationWokrflow = {
    println("Attempting to snipe reservation at " + DateTime.now)

    //Need to make the first call
    for(findResResp <- findReservation;
        resDetailsResp <- getReservationDetails(findResResp);
        result <- bookReservation(resDetailsResp)) {
      println("Shutting down Resy Booking Bot at " + DateTime.now)
      System.exit(0)
    }

    //STEP 1: FIND RESERVATION (GET CONFIG ID)
    def findReservation: Future[String] = {
      val findResQueryParams = Map(
        "day" -> day,
        "lat" -> "0",
        "long" -> "0",
        "party_size" -> partySize,
        "venue_id" -> venueId)

      ResyApiWrapper.sendGetRequest(ResyApiMapKeys.FindReservation, findResQueryParams, true)
    }

    //STEP 2: GET RESERVATION DETAILS (GET PAYMENT ID AND BOOK TOKEN)
    def getReservationDetails(findResResp: String) = {
      val reservations = Json.parse(findResResp)

      println(s"${DateTime.now} URL Response: $reservations")

      //ConfigId - Searching for this pattern - "time_slot": "17:15:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123457
      val configId =
        ((reservations \ "results" \ 0 \ "configs")
          .get
          .as[JsArray]
          .value
          .filter(x => (x \ "time_slot").get.toString == s""""$time"""")
          (0) \ "id")
          .get
          .toString

      println(s"${DateTime.now} Config Id: ${configId}")

      val findResQueryParams = Map(
        "config_id" -> configId,
        "day" -> day,
        "party_size" -> partySize)

      ResyApiWrapper.sendGetRequest(ResyApiMapKeys.ReservationDetails, findResQueryParams, true)
    }

    //STEP 3: BOOK RESERVATION
    def bookReservation(resDetailsResp: String) = {
      val resDetails = Json.parse(resDetailsResp)
      println(s"${DateTime.now} URL Response: $resDetailsResp")

      //PaymentMethodId - Searching for this pattern - "payment_methods": [{"is_default": true, "provider_name": "braintree", "id": 123456, "display": "1234", "provider_id": 1}]
      val paymentMethodId =
        (resDetails \ "user" \ "payment_methods" \ 0 \ "id")
          .get
          .toString

      println(s"${DateTime.now} Payment Method Id: ${paymentMethodId}")

      //BookToken - Searching for this pattern - "book_token": {"value": "book_token_value"
      val bookToken =
        (resDetails \ "book_token" \ "value")
          .get
          .toString
          .stripPrefix("\"")
          .stripSuffix("\"")

      println(s"${DateTime.now} Book Token: ${bookToken}")

      val bookResQueryParams = Map(
        "book_token" -> bookToken,
        "struct_payment_method" -> s"""{"id":$paymentMethodId}""",
        "source_id" -> "resy.com-venue-details")

      ResyApiWrapper.sendPostRequest(ResyApiMapKeys.BookReservation, bookResQueryParams, true)
    }
  }
}
