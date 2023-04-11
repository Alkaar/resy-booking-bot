package com.resy

import akka.actor.ActorSystem
import org.apache.logging.log4j.scala.Logging
import org.joda.time.DateTime
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.util.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object ResyBookingBot extends Logging {

  def main(args: Array[String]): Unit = {
    logger.info("Starting Resy Booking Bot")

    val resyConfig = ConfigSource.resources("resyConfig.conf")
    val resyKeys   = resyConfig.at("resyKeys").loadOrThrow[ResyKeys]
    val resDetails = resyConfig.at("resDetails").loadOrThrow[ReservationDetails]
    val runDetails = resyConfig.at("runDetails").loadOrThrow[RunDetails]

    val resyApi             = new ResyApi(resyKeys)
    val resyClient          = new ResyClient(resyApi)
    val resyBookingWorkflow = new ResyBookingWorkflow(resyClient, resDetails)

    if (runDetails.mode == "once") {
      logger.info("Running snipe once")
      resyBookingWorkflow.run()
    }

    if (runDetails.mode == "scheduled") {
      logger.info("Setting up scheduled snipe")
      val system      = ActorSystem("System")
      val dateTimeNow = DateTime.now
      val todaysSnipeTime = dateTimeNow
        .withHourOfDay(runDetails.scheduled.hours)
        .withMinuteOfHour(runDetails.scheduled.minutes)
        .withSecondOfMinute(0)
        .withMillisOfSecond(0)

      val nextSnipeTime =
        if (todaysSnipeTime.getMillis > dateTimeNow.getMillis) todaysSnipeTime
        else todaysSnipeTime.plusDays(1)

      val millisUntilTomorrow = nextSnipeTime.getMillis - DateTime.now.getMillis - 2000
      val hoursRemaining      = millisUntilTomorrow / 1000 / 60 / 60
      val minutesRemaining    = millisUntilTomorrow / 1000 / 60 - hoursRemaining * 60
      val secondsRemaining =
        millisUntilTomorrow / 1000 - hoursRemaining * 60 * 60 - minutesRemaining * 60

      logger.info(s"Next snipe time: $nextSnipeTime")
      logger.info(
        s"Sleeping for $hoursRemaining hours, $minutesRemaining minutes, and $secondsRemaining seconds"
      )

      system.scheduler.scheduleOnce(millisUntilTomorrow millis) {
        resyBookingWorkflow.run()

        logger.info("Shutting down Resy Booking Bot")
        System.exit(0)
      }
    }

    if (runDetails.mode == "loop") {
      logger.info("Starting snipe loop")
      val system = ActorSystem("System")

      val maxRetries = runDetails.loop.maxRetries
      var retries    = 0

      system.scheduler.schedule(0 seconds, runDetails.loop.interval seconds) {
        val res = resyBookingWorkflow.run()

        res match {
          case Success(_) =>
            logger.info("Shutting down Resy Booking Bot")
            System.exit(0)
          case _ =>
        }

        retries += 1
        if (retries == maxRetries) {
          logger.info(s"Max retries reached: $maxRetries")
          logger.info("Shutting down Resy Booking Bot")
          System.exit(0)
        }

        logger.info(s"Retrying in ${runDetails.loop.interval} seconds...")
      }
    }
  }
}
