package com.resy

import akka.actor.ActorSystem
import com.resy.ResyApi.{sendGetRequest, sendPostRequest}
import org.apache.logging.log4j.scala.Logging
import play.api.libs.ws.WSBodyWritables.writeableOf_String
import play.api.libs.ws.ahc.AhcWSClient

import java.net.URLEncoder
import scala.concurrent.Future

// Resy API docs can be found here http://subzerocbd.info/
class ResyApi(resyToken: ResyKeys) {

  /** Find available reservations
    * @param date
    *   Date of reservation to search in YYYY-MM-DD format
    * @param partySize
    *   Size of party
    * @param venueId
    *   Unique identifier of the restaurant
    * @return
    *   JSON object of available reservation times and seating types for that day
    */
  def getReservations(date: String, partySize: Int, venueId: Int): Future[String] = {
    val findResQueryParams = Map(
      "lat"        -> "0",
      "long"       -> "0",
      "day"        -> date,
      "party_size" -> partySize.toString,
      "venue_id"   -> venueId.toString
    )

    sendGetRequest(resyToken, "api.resy.com/4/find", findResQueryParams)
  }

  /** Get details of the reservation
    * @param configId
    *   Unique identifier for the reservation
    * @param date
    *   Date of reservation to get details for in YYYY-MM-DD format
    * @param partySize
    *   Size of party
    * @return
    *   JSON object with the details about the reservation
    */
  def getReservationDetails(configId: String, date: String, partySize: Int): Future[String] = {
    val findResQueryParams =
      Map(
        "config_id"  -> configId,
        "day"        -> date,
        "party_size" -> partySize.toString
      )

    sendGetRequest(resyToken, "api.resy.com/3/details", findResQueryParams)
  }

  /** Book the reservation
    * @param paymentMethodId
    *   Unique identifier of the payment id in case of a late cancellation fee
    * @param bookToken
    *   Unique identifier of the reservation in question
    * @return
    *   JSON object of the unique identifier of the confirmed booking
    */
  def postReservation(paymentMethodId: Int, bookToken: String): Future[String] = {
    val bookResQueryParams = Map(
      "book_token"            -> bookToken,
      "struct_payment_method" -> s"""{"id":$paymentMethodId}"""
    )

    sendPostRequest(resyToken, "api.resy.com/3/book", bookResQueryParams)
  }
}

object ResyApi extends Logging {
  implicit private val system: ActorSystem = ActorSystem()
  private val ws                           = AhcWSClient()

  private def sendGetRequest(
    resyKeys: ResyKeys,
    baseUrl: String,
    queryParams: Map[String, String]
  ): Future[String] = {
    val url =
      s"https://$baseUrl?${stringifyQueryParams(queryParams)}"

    logger.debug(s"URL Request: $url")

    ws.url(url)
      .withHttpHeaders(createHeaders(resyKeys): _*)
      .get
      .map(_.body)(system.dispatcher)
  }

  private def sendPostRequest(
    resyKeys: ResyKeys,
    baseUrl: String,
    queryParams: Map[String, String]
  ): Future[String] = {
    val url  = s"https://$baseUrl"
    val post = stringifyQueryParams(queryParams)

    logger.debug(s"URL Request: $url")
    logger.debug(s"Post Params: $post")

    ws.url(url)
      .withHttpHeaders(
        createHeaders(resyKeys) :+ "Content-Type" -> "application/x-www-form-urlencoded": _*
      )
      .post(post)
      .map(_.body)(system.dispatcher)
  }

  private[this] def createHeaders(resyKeys: ResyKeys): Seq[(String, String)] = {
    Seq(
      "Authorization"     -> s"""ResyAPI api_key="${resyKeys.apiKey}"""",
      "x-resy-auth-token" -> resyKeys.authToken
    )
  }

  private[this] def stringifyQueryParams(queryParams: Map[String, String]): String = {
    queryParams.foldLeft("") { case (acc, (key, value)) =>
      acc + s"$key=${URLEncoder.encode(value, "UTF-8")}&"
    }
  }
}
