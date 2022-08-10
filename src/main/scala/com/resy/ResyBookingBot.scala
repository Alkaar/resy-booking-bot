package com.resy

import akka.actor.ActorSystem
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object ResyBookingBot {

  private val resyKeys = ResyKeys(
    // Your user profile API key which can be found via your browser DevTools
    apiKey = ???,
    // Your user profile authentication token which can be found via your browser DevTools
    authToken = ???
  )

  private val resDetails = ReservationDetails(
    // Date of the reservation in YYYY-MM-DD format
    date = ???,
    // Size of the party reservation
    partySize = ???,
    // Unique identifier of the restaurant where you want to make the reservation
    venueId = ???,
    // Priority list of reservation times in military time HH:MM:SS format
    preferredResTimes = ???
  )

  def main(args: Array[String]): Unit = {
    val dateTimeNow = DateTime.now
    println("Starting Resy Booking Bot")

    val resyApi    = new ResyApi(resyKeys)
    val resyClient = new ResyClient(resyApi)

    val system              = ActorSystem("System")
    val startOfTomorrow     = dateTimeNow.withTimeAtStartOfDay.plusDays(1).getMillis
    val millisUntilTomorrow = startOfTomorrow - dateTimeNow.getMillis - 1000
    val hoursRemaining      = millisUntilTomorrow / 1000 / 60 / 60
    val minutesRemaining    = millisUntilTomorrow / 1000 / 60 - hoursRemaining * 60
    val secondsRemaining =
      millisUntilTomorrow / 1000 - hoursRemaining * 60 * 60 - minutesRemaining * 60

    println(s"Current time: ${DateTime.now}")
    println(
      s"Sleeping for $hoursRemaining hours, $minutesRemaining minutes and $secondsRemaining seconds"
    )

    system.scheduler.scheduleOnce(millisUntilTomorrow millis)(
      ResyBookingWorkflow.run(resyClient, resDetails)
    )
  }
}

final case class ResyKeys(apiKey: String, authToken: String)

final case class ReservationDetails(
  date: String,
  partySize: Int,
  venueId: Int,
  preferredResTimes: Seq[String]
)
