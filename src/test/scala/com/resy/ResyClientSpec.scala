package com.resy

import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success}

class ResyClientSpec extends AnyFlatSpec with Matchers {

  trait Fixture {
    val resyApi: ResyApi = mock(classOf[ResyApi])
    val resyClient       = new ResyClient(resyApi)
  }

  import ResyClientSpec._

  behavior of "ResyClientSpec"
  it should "find an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = resDetails.preferredResTimes,
      millisToRetry     = (.1 seconds).toMillis
    ) shouldEqual Success("CONFIG_ID2")
  }

  it should "find an available reservation that is not the highest priority" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = Seq("15:00:00", "17:00:00"),
      millisToRetry     = (.1 seconds).toMillis
    ) shouldEqual Success("CONFIG_ID2")
  }

  it should "find an available reservation after retrying" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(""))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = resDetails.preferredResTimes,
      millisToRetry     = (.1 seconds).toMillis
    ) shouldEqual Success("CONFIG_ID2")

    verify(resyApi, Mockito.times(2))
      .getReservations(
        date      = resDetails.date,
        partySize = resDetails.partySize,
        venueId   = resDetails.venueId
      )
  }

  it should "not find an available reservation due to bad response" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(""))

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = resDetails.preferredResTimes,
      millisToRetry     = (.1 seconds).toMillis
    ) match {
      case Failure(exception) =>
        withClue("RuntimeException not found:") {
          exception.isInstanceOf[RuntimeException] shouldEqual true
        }
        exception.getMessage shouldEqual ResyClientErrorMessages.cantFindResMsg
      case _ =>
        fail("Failure not found")
    }
  }

  it should "not find an available reservation in a list of reservations" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(Source.fromResource("getReservations.json").mkString))

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = Seq("18:00:00")
    ) match {
      case Failure(exception) =>
        withClue("RuntimeException not found:") {
          exception.isInstanceOf[RuntimeException] shouldEqual true
        }
        exception.getMessage shouldEqual ResyClientErrorMessages.cantFindResMsg
      case _ =>
        fail("Failure not found")
    }
  }

  it should "get reservation details" in new Fixture {
    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(Source.fromResource("getReservationDetails.json").mkString))

    resyClient.getReservationDetails(
      configId  = configId,
      date      = resDetails.date,
      partySize = resDetails.partySize
    ) shouldEqual Success(BookingDetails(42, "BOOK_TOKEN"))
  }

  it should "not get reservation details due to bad response" in new Fixture {
    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(""))

    resyClient.getReservationDetails(
      configId  = configId,
      date      = resDetails.date,
      partySize = resDetails.partySize
    ) match {
      case Failure(exception) =>
        withClue("RuntimeException not found:") {
          exception.isInstanceOf[RuntimeException] shouldEqual true
        }
        exception.getMessage shouldEqual ResyClientErrorMessages.unknownErrorMsg
      case _ =>
        fail("Failure not found")
    }
  }

  it should "book reservation" in new Fixture {
    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(Source.fromResource("bookReservation.json").mkString))

    resyClient.bookReservation(
      paymentMethodId = paymentMethodId,
      bookToken       = bookToken
    ) shouldEqual Success("RESY_TOKEN")
  }

  it should "not book reservation due to bad response" in new Fixture {
    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(""))

    resyClient.bookReservation(
      paymentMethodId = paymentMethodId,
      bookToken       = bookToken
    ) match {
      case Failure(exception) =>
        withClue("RuntimeException not found:") {
          exception.isInstanceOf[RuntimeException] shouldEqual true
        }
        exception.getMessage shouldEqual ResyClientErrorMessages.resNoLongerAvailMsg
      case _ =>
        fail("Failure not found")
    }
  }
}

object ResyClientSpec {

  val resDetails: ReservationDetails = ReservationDetails(
    date              = "2099-01-30",
    partySize         = 2,
    venueId           = 1,
    preferredResTimes = Seq("17:00:00")
  )

  val configId = "CONFIG_ID"

  val paymentMethodId = 42
  val bookToken       = "BOOK_TOKEN"
}
