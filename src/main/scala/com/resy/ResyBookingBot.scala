package com.resy

import play.api.routing.sird.&

import scala.concurrent.Await
import scala.concurrent.duration._

object ResyBookingBot {
  def main(args: Array[String]): Unit = {
    //STEP 1: GET PAYMENT ID
    val userDetailsResp = ResyApiWrapper.sendRequest(ResyApiMapKeys.UserDetails, Map.empty, true)

    Await.result(userDetailsResp, 5000 millis)

    val userDetails = userDetailsResp.value.get.get
    println(s"URL Response: $userDetails")

    //PaymentMethodId - Searching for this pattern "payment_method_id": 123456
    val paymentKeyPattern = """"payment_method_id": [0-9]+""".r
    val paymentValuePattern = """[0-9]+""".r

    val paymentMethodId =
      paymentValuePattern
        .findFirstIn(paymentKeyPattern
          .findFirstIn(userDetails)
          .get)
        .get

    println(s"Payment Method Id: ${paymentMethodId}")

    //STEP 1: GET RESERVATION
    val queryParams = Map(
      "day" -> "2018-03-21",
      "lat" -> "0",
      "long" -> "0",
    "party_size" -> "2",
    "venue_id" -> "834")

    val reservationResp = ResyApiWrapper.sendRequest(ResyApiMapKeys.FindReservation, queryParams, true)

    Await.result(reservationResp, 5000 millis)

    val reservationDetails = reservationResp.value.get.get
    println(s"URL Response: $reservationDetails")

    println("Done")
    System.exit(1)
  }
}
