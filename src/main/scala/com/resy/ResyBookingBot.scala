package com.resy

import scala.concurrent.Await
import scala.concurrent.duration._

object ResyBookingBot {
  def main(args: Array[String]): Unit = {
    //STEP 1: GET PAYMENT ID
    val userDetailsResp = ResyApiWrapper.sendRequest(ResyApiMapKeys.UserDetails, Map.empty, false)

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

    println("Done")
  }
}
