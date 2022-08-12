package com.resy

import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}

class ResyBookingWorkflowSpec extends AnyFlatSpec with Matchers {

  trait Fixture {
    val resyApi: ResyApi = mock(classOf[ResyApi])
    val resyClient       = new ResyClient(resyApi)
  }

  import ResyBookingWorkflowSpec._

  behavior of "ResyBookingWorkflowSpec"
  it should "find and book an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(Source.fromResource("bookReservation.json").mkString))

    ResyBookingWorkflow.run(resyClient, resDetails) shouldEqual Success("RESY_TOKEN")
  }

  it should "find but fail to book an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(""))

    ResyBookingWorkflow.run(resyClient, resDetails) match {
      case Failure(exception) =>
        withClue("RuntimeException not found:") {
          exception.isInstanceOf[RuntimeException] shouldEqual true
        }
        exception.getMessage shouldEqual ResyClientErrorMessages.resNoLongerAvailMsg
      case _ =>
        fail("Failure not found")
    }

    verify(resyApi, Mockito.times(1))
      .getReservations(
        date      = resDetails.date,
        partySize = resDetails.partySize,
        venueId   = resDetails.venueId
      )

    verify(resyApi, Mockito.times(1))
      .getReservationDetails(
        configId  = configId,
        date      = resDetails.date,
        partySize = resDetails.partySize
      )

    verify(resyApi, Mockito.times(1))
      .postReservation(
        paymentMethodId = paymentMethodId,
        bookToken       = bookToken
      )
  }
}

object ResyBookingWorkflowSpec {

  val resDetails: ReservationDetails = ReservationDetails(
    date         = "2099-01-30",
    partySize    = 2,
    venueId      = 1,
    resTimeTypes = Seq(ReservationTimeType("18:00:00", "TABLE_TYPE5"))
  )

  val configId = "CONFIG_ID5"

  val paymentMethodId = 42
  val bookToken       = "BOOK_TOKEN"
}
