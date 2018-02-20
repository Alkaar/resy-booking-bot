package com.resy

import java.time.Instant

import com.resy.BookingDetails._

import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._

object ResyBookingBot {
  def main(args: Array[String]): Unit = {
    println("Starting Resy Booking Bot")

    println("Attempting to snipe reservation: " + Instant.ofEpochMilli(System.currentTimeMillis()))
    //STEP 1: FIND RESERVATION (GET CONFIG ID)
    val findResQueryParams = Map(
      "day" -> day,
      "lat" -> "0",
      "long" -> "0",
    "party_size" -> partySize,
    "venue_id" -> venueId)

    val findResResp = ResyApiWrapper.sendGetRequest(ResyApiMapKeys.FindReservation, findResQueryParams, false)

    Await.result(findResResp, 5000 millis)

    val reservations = Json.parse(findResResp.value.get.get)
    println(s"URL Response: $reservations")

    //ConfigId - Searching for this pattern - "time_slot": "17:15:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123457
    val configId = ((reservations \ "results" \ 0 \ "configs")
      .get
      .as[JsArray]
      .value
      .filter(x => (x \ "time_slot").get.toString == s""""$time"""")(0) \ "id").get.toString

    println(s"Config Id: ${configId}")

    //STEP 2: GET RESERVATION DETAILS (GET PAYMENT ID AND BOOK TOKEN)
    val resDetailsQueryParams = Map(
      "config_id" -> configId,
      "day" -> day,
      "party_size" -> partySize)

    val resDetailsResp = ResyApiWrapper.sendGetRequest(ResyApiMapKeys.ReservationDetails, resDetailsQueryParams, false)

    Await.result(resDetailsResp, 5000 millis)

    val resDetails = resDetailsResp.value.get.get
    println(s"URL Response: $resDetails")

    //PaymentMethodId - Searching for this pattern - "payment_methods": [{"is_default": true, "provider_name": "braintree", "id": 123456, "display": "1234", "provider_id": 1}]
    val paymentPattern = """"payment_methods"[\s]*:[\s]*\[.+?\]""".r
    val paymentKeyPattern = """"id"[\s]*:[\s]*\d+""".r
    val paymentValuePattern = """\d+$""".r

    val paymentMethodId =
      paymentValuePattern
        .findFirstIn(paymentKeyPattern
          .findFirstIn(
            paymentPattern
              .findFirstIn(resDetails)
              .get)
          .get)
        .get

    println(s"Payment Method Id: ${paymentMethodId}")

    //BookToken - Searching for this pattern - "book_token": {"value": "book_token_value"
    val bookTokenPattern = """"book_token"[\s]*:[\s]*.+?"value"[\s]*:[\s]*"[^"]+"""".r
    val bookTokenKeyPattern = """"value"[\s]*:[\s]*"[^"]+""".r
    val bookTokenValuePattern = """[^"]+$""".r

    val bookToken =
      bookTokenValuePattern
        .findFirstIn(bookTokenKeyPattern
          .findFirstIn(bookTokenPattern
            .findFirstIn(resDetails)
            .get)
          .get)
        .get

    println(s"Book Token: ${bookToken}")

    //STEP 3: BOOK RESERVATION
    val bookResQueryParams = Map(
      "book_token" -> bookToken,
      "struct_payment_method" -> s"""{"id":$paymentMethodId}""",
      "source_id" -> "resy.com-venue-details")

    val bookResResp = ResyApiWrapper.sendPostRequest(ResyApiMapKeys.BookReservation, bookResQueryParams, false)

    Await.result(bookResResp, 10000 millis)

    val bookResDetails = bookResResp.value.get.get
    println(s"URL Response: $bookResDetails")

    println("Shutting down Resy Booking Bot: " + Instant.ofEpochMilli(System.currentTimeMillis()))
    System.exit(1)
  }
}
