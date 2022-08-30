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
    // scalafix:off
    val resyApi: ResyApi = mock(classOf[ResyApi])
    val resyClient       = new ResyClient(resyApi)
    // scalafix:on
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

    val resyBookingWorkflow = new ResyBookingWorkflow(resyClient, resDetails)

    resyBookingWorkflow.run() shouldEqual Success("RESY_TOKEN")
  }

  it should "find and book an available reservation with retrying" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(""))
      .thenReturn(Future(Source.fromResource("bookReservation.json").mkString))

    val resyBookingWorkflow = new ResyBookingWorkflow(resyClient, resDetails)

    resyBookingWorkflow.run() shouldEqual Success("RESY_TOKEN")

    verify(resyApi, Mockito.times(2))
      .getReservations(
        date      = resDetails.date,
        partySize = resDetails.partySize,
        venueId   = resDetails.venueId
      )

    verify(resyApi, Mockito.times(2))
      .getReservationDetails(
        configId  = configId,
        date      = resDetails.date,
        partySize = resDetails.partySize
      )

    verify(resyApi, Mockito.times(2))
      .postReservation(
        paymentMethodId = paymentMethodId,
        bookToken       = bookToken
      )
  }

  it should "find but fail to book an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(""))

    val resyBookingWorkflow = new ResyBookingWorkflow(resyClient, resDetails)

    resyBookingWorkflow.run(0) match {
      case Failure(exception) =>
        exception match {
          case _: RuntimeException =>
            exception.getMessage shouldEqual ResyClientErrorMessages.resNoLongerAvailMsg
          case _ => fail("RuntimeException not found")
        }
      case _ => fail("Failure not found")
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

  it should "fail to find any available reservations and short circuit" in new Fixture {

    val newResDetails =
      resDetails.copy(resTimeTypes =
        Seq(ReservationTimeType("12:34:56", "TABLE_TYPE_DOES_NOT_EXIST"))
      )
    when(
      resyApi.getReservations(newResDetails.date, newResDetails.partySize, newResDetails.venueId)
    )
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    when(resyApi.getReservationDetails(configId, newResDetails.date, newResDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(Source.fromResource("bookReservation.json").mkString))

    val resyBookingWorkflow = new ResyBookingWorkflow(resyClient, newResDetails)

    resyBookingWorkflow.run() match {
      case Failure(exception) =>
        exception match {
          case _: RuntimeException =>
            exception.getMessage shouldEqual ResyClientErrorMessages.cantFindResMsg
          case _ => fail("RuntimeException not found")
        }
      case _ => fail("Failure not found")
    }

    verify(resyApi, Mockito.times(1))
      .getReservations(
        date      = newResDetails.date,
        partySize = newResDetails.partySize,
        venueId   = newResDetails.venueId
      )

    verify(resyApi, Mockito.times(0))
      .getReservationDetails(
        configId  = configId,
        date      = newResDetails.date,
        partySize = newResDetails.partySize
      )

    verify(resyApi, Mockito.times(0))
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
