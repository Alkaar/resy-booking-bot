package com.resy

import akka.actor.ActorSystem
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object ResyBookingBot {

  private val resyKeys = ResyKeys(
    // Your user profile API key which can be found via your browser web console in your headers
    // called "authorization"
    apiKey = ???,
    // Your user profile authentication token which can be found via your browser web console in
    // your headers called "x-resy-auth-token"
    authToken = ???
  )

  private val resDetails = ReservationDetails(
    // Date of the reservation in YYYY-MM-DD format
    date = ???,
    // Size of the party reservation
    partySize = ???,
    // Unique identifier of the restaurant where you want to make the reservation
    venueId = ???,
    // Priority list of reservation times and table types. Time is in military time HH:MM:SS format.
    // If no preference on table type, then simply don't set it.
    resTimeTypes = ???
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

    system.scheduler.scheduleOnce(millisUntilTomorrow millis) {
      ResyBookingWorkflow.run(resyClient, resDetails)

      println("Shutting down Resy Booking Bot at " + DateTime.now)
      System.exit(0)
    }
  }
}

final case class ResyKeys(apiKey: String, authToken: String)

final case class ReservationDetails(
  date: String,
  partySize: Int,
  venueId: Int,
  resTimeTypes: Seq[ReservationTimeType]
)

final case class ReservationTimeType(reservationTime: String, tableType: Option[String] = None)

object ReservationTimeType {

  def apply(reservationTime: String, tableType: String): ReservationTimeType = {
    ReservationTimeType(reservationTime, Some(tableType))
  }
}
