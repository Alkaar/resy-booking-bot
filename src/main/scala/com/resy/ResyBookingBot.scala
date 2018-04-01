package com.resy

import akka.actor.ActorSystem
import com.resy.BookReservationWorkflow._
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

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
    bookReservationWorkflow
//    system.scheduler.scheduleOnce(millisUntilTomorrow millis)(bookReservationWorkflow)
  }

  def bookReservationWorkflow = {
    println(s"Attempting to snipe reservation at ${DateTime.now}")

    //Try to get configId of the time slot for 10 seconds
    val findResResp = retryFindReservation(DateTime.now.plusSeconds(10).getMillis)

    //Try to book the reservation
    for(resDetailsResp <- getReservationDetails(findResResp);
        bookResResp <- bookReservation(resDetailsResp)
    ) {
      val resyToken =
        Try((Json.parse(bookResResp) \ "resy_token")
          .get
          .toString
          .stripPrefix("\"")
          .stripSuffix("\"")
        )

      resyToken match {
        case Success(token) =>
          println(s"Successfully sniped reservation at ${DateTime.now}")
          println(s"Resy token is $token")
        case Failure(error) =>
          println(s"Couldn't sniped reservation at ${DateTime.now}")
          println(s"Error message is $error")
      }

      println("Shutting down Resy Booking Bot at " + DateTime.now)
      System.exit(0)
    }
  }
}
