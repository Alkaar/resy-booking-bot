package com.resy

import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

class ResyClientSpec extends AnyFlatSpec with Matchers {

  trait Fixture {
    val resyApi: ResyApi = mock(classOf[ResyApi])
    val resyClient       = new ResyClient(resyApi)
  }

  import ResyClientSpec._

  behavior of "ResyClientSpec"
  it should "find an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(
        Future(
          """{"results": {"venues": [{"slots": [{"config": {"token": "CONFIG_ID1"}, "date": {"start": "2022-01-30 16:00:00"}}, {"config": {"token": "CONFIG_ID2"}, "date": {"start": "2022-01-30 17:00:00"}}]}]}}"""
        )
      )

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = resDetails.preferredResTimes
    ) shouldEqual Success("CONFIG_ID2")
  }

  it should "find an available reservation that is not the highest priority" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(
        Future(
          """{"results": {"venues": [{"slots": [{"config": {"token": "CONFIG_ID1"}, "date": {"start": "2022-01-30 16:00:00"}}, {"config": {"token": "CONFIG_ID2"}, "date": {"start": "2022-01-30 17:00:00"}}]}]}}"""
        )
      )

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = Seq("15:00:00", "17:00:00")
    ) shouldEqual Success("CONFIG_ID2")
  }

  it should "find an available reservation after retrying" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(""))
      .thenReturn(
        Future(
          """{"results": {"venues": [{"slots": [{"config": {"token": "CONFIG_ID1"}, "date": {"start": "2022-01-30 16:00:00"}}, {"config": {"token": "CONFIG_ID2"}, "date": {"start": "2022-01-30 17:00:00"}}]}]}}"""
        )
      )

    resyClient.retryFindReservation(
      date              = resDetails.date,
      partySize         = resDetails.partySize,
      venueId           = resDetails.venueId,
      preferredResTimes = resDetails.preferredResTimes,
      millisToRetry     = (1 seconds).toMillis
    ) shouldEqual Success("CONFIG_ID2")

    verify(resyApi, Mockito.atLeast(2)).getReservations(
      resDetails.date,
      resDetails.partySize,
      resDetails.venueId
    )
  }

  it should "not find an available reservation" in new Fixture {
    when(resyApi.getReservations(resDetails.date, resDetails.partySize, resDetails.venueId))
      .thenReturn(Future(""))

    an[RuntimeException] shouldBe thrownBy {
      resyClient.retryFindReservation(
        date              = resDetails.date,
        partySize         = resDetails.partySize,
        venueId           = resDetails.venueId,
        preferredResTimes = resDetails.preferredResTimes,
        millisToRetry     = (.1 seconds).toMillis
      )
    }
  }

  it should "get reservation details" in new Fixture {
    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(
        Future(
          """{"user": {"payment_methods": [{"id": 42}]}, "book_token": {"value": "BOOK_TOKEN"}}"""
        )
      )

    resyClient.getReservationDetails(
      configId  = configId,
      date      = resDetails.date,
      partySize = resDetails.partySize
    ) shouldEqual Success(BookingDetails(42, "BOOK_TOKEN"))
  }

  it should "not get reservation details" in new Fixture {
    when(resyApi.getReservationDetails(configId, resDetails.date, resDetails.partySize))
      .thenReturn(Future(""))

    an[RuntimeException] shouldBe thrownBy {
      resyClient.getReservationDetails(
        configId  = configId,
        date      = resDetails.date,
        partySize = resDetails.partySize
      )
    }
  }

  it should "book reservation" in new Fixture {
    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(
        Future(
          """{"resy_token": "RESY_TOKEN"}"""
        )
      )

    resyClient.bookReservation(
      paymentMethodId = paymentMethodId,
      bookToken       = bookToken
    ) shouldEqual Success("RESY_TOKEN")
  }

  it should "not book reservation" in new Fixture {
    when(resyApi.postReservation(paymentMethodId, bookToken))
      .thenReturn(Future(""))

    an[RuntimeException] shouldBe thrownBy {
      resyClient.bookReservation(
        paymentMethodId = paymentMethodId,
        bookToken       = bookToken
      )
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
