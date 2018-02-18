package com.resy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.util.matching.Regex._

object ResyBookingBot {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val ws = AhcWSClient()

    val auth_token = ""
    val api_key = ""

    val paymentKeyPattern = """"payment_method_id": [0-9]+""".r
    val paymentValuePattern = """[0-9]+""".r

    val userDetailsResp = ws.url(s"https://api.resy.com/2/user?auth_token=$auth_token")
      .withHttpHeaders("Authorization" -> s"""ResyAPI api_key="$api_key"""").get.map(resp => resp.body)(system.dispatcher)

    Await.result(userDetailsResp, 5000 millis)

    println(userDetailsResp)

    val userDetails = userDetailsResp.value.get.get

    //PaymentMethodId - Searching for this pattern "payment_method_id": 334169
    val paymentMethodId = paymentValuePattern.findFirstIn(
      paymentKeyPattern.findFirstIn(userDetails).get)
      .get

    println(paymentMethodId)

//    val req = ws.url("https://api.resy.com/3/details?auth_token=9_ziUdgf76tqxUlb1MYtKD6M96P0ty6w3ihJ_XsEnLuDiM6WZWBkDyhUaUbeKum3Rf060tleKpfLEQ08qBsh_0KZAzlVg1b%7CwlMOs90vfZo%3D-0f4abbf54f91428fe258a8073df32919f7120207ff05564d625624dd&config_id=305323&day=2018-03-14&party_size=2")
//      .withHttpHeaders("Authorization" -> "ResyAPI api_key=\"VbWk7s3L4KiK5fzlO7JD3Q5EYolJI7n5\"").get.map(resp => println(resp.body))(system.dispatcher)
//
//
//    val req = ws.url("https://api.resy.com/3/details?auth_token=9_ziUdgf76tqxUlb1MYtKD6M96P0ty6w3ihJ_XsEnLuDiM6WZWBkDyhUaUbeKum3Rf060tleKpfLEQ08qBsh_0KZAzlVg1b%7CwlMOs90vfZo%3D-0f4abbf54f91428fe258a8073df32919f7120207ff05564d625624dd&config_id=305323&day=2018-03-14&party_size=2")
//        .withHttpHeaders("Authorization" -> "ResyAPI api_key=\"VbWk7s3L4KiK5fzlO7JD3Q5EYolJI7n5\"").get.map(resp => println(resp.body))(system.dispatcher)

    //https://api.resy.com/3/book
//    auth_token:9_ziUdgf76tqxUlb1MYtKD6M96P0ty6w3ihJ_XsEnLuDiM6WZWBkDyhUaUbeKum3Rf060tleKpfLEQ08qBsh_0KZAzlVg1b|wlMOs90vfZo=-0f4abbf54f91428fe258a8073df32919f7120207ff05564d625624dd
//      book_token:FQNOBtElivH50I_rWlTn2v_jg_vJ7xfTm9XW7iRxmt2zEjLRjc0mFdsY5sKguCUnmUIuSwlr5oG5TxyBfgiUGNdlaDiQCCPuMySvjCqrM|UWOXmDwmGkUQobf2FbrgNo-eaa7cf6549a7208cdb1f94550c70a30d49bde0bd285270b23fefb351
//    struct_payment_method:{"id":334169}
//    source_id:resy.com-venue-details

//    println(req.value)

    //ws.close()

    println("Done")
  }
}
