package com.resy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.resy.ResyApiMapKeys.{FindReservation, UserDetails}
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResyApiWrapper {
  private val auth_token = ""
  private val api_key = ""

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  private val ws = AhcWSClient()

  private val apiMap = Map(
    UserDetails ->
      ApiDetails(
        url = "api.resy.com/2/user",
        testResponse = """{"cc_last_4": "1234", "first_name": "John", "date_fb_token_expires": null, "mobile_number_is_verified": 0, "fb_user_id": null, "em_is_verified": 1, "payment_provider_id": 1, "num_bookings": 12, "allergies": null, "bio": null, "payment_methods": [{"id": 123456, "provider_name": "braintree", "display": "1234", "provider_id": 1, "is_default": true}], "profile_image_url": "https://s3.amazonaws.com/imagehash", "date_of_birth": null, "payment_method_id": 123456, "venue_credits": null, "payment_provider_name": "braintree", "long_code_id": 8, "date_created": 1500000000, "last_name": "Doe", "em_address": "test@gmail.com", "profile_image_id": null, "id": 1234567, "is_active": 1, "mobile_number": "+17181234567", "date_updated": 1512000000, "referral_code": "abc", "twit_user_id": null, "in_user_id": null, "payment_display": "1234", "date_app_first_opened": null}"""),
    FindReservation ->
      ApiDetails(
        url = "https://api.resy.com/3/find",
        testResponse = """{"results": [{"configs": [{"table_config_id": 123456, "type": "Dining Room", "time_slot": "22:30:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123457, "day": "2018-03-20", "tags": {"inside_outside": ["inside"], "time_period": ["late"], "service_type": ["dinner"]}, "on_market": 1}, {"table_config_id": 123458, "type": "Dining Room", "time_slot": "22:45:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123459, "day": "2018-03-20", "tags": {"inside_outside": ["inside"], "time_period": ["late"], "service_type": ["dinner"]}, "on_market": 1}], "venue": {"rating": 4.9, "id": {"foursquare": "123456", "resy": 800, "google": "123456"}, "name": "Restaurant", "templates": {}, "metadata": {"description": "Description", "keywords": ["book", "dining", "reservations", "restaurant", "new york city", "123456", "times square", "steakhouse", "resy", "5.0 star"]}, "images": ["https://image.resy.com/1.jpg/jpg/640x360", "https://image.resy.com/2.jpg/jpg/640x360", "https://image.resy.com/3.jpg/jpg/640x360"], "config": {"enable_resypay": 0, "allow_bypass_payment_method": 1, "allow_multiple_resys": 0, "hospitality_included": 0, "enable_invite": 1}, "url_slug": "restaurant", "service_types": {"2": {"name": "dinner", "available": true, "user": {"notify": {"set": false, "matched": false, "preferences": null}}}}, "price_range": 3, "travel_time": {"distance": 0, "driving": 0, "walking": 0}, "rater": {"image": "https://s3.amazonaws.com/resy.png", "score": 5.0, "name": "Resy", "scale": 5}, "location": {"address_2": null, "postal_code": "12345", "address_1": "1 Wall St", "locality": "New York City", "neighborhood": "Times Square", "region": "NY", "time_zone": "EST5EDT", "longitude": -0.0, "latitude": 0.0}, "contact": {"phone_number": "+17181234567"}, "num_ratings": 5000, "type": "Steakhouse", "top": true, "notify": {"matched": 0}}}], "filter": [{"options": [["early", null, "early"], ["late", null, "late"]], "id": "time_period", "unavailable": false}], "query": {"day": "2018-03-20", "party_size": 2}}"""),
  )

  def sendRequest(resyApiMapKey: ResyApiMapKey with Product with Serializable, queryParams: Map[String, String], test: Boolean): Future[String] = {
    val apiDetails = apiMap.get(resyApiMapKey).get
    val url = s"https://${apiDetails.url}?auth_token=$auth_token&${stringifyQueryParams(queryParams)}"

    println(s"URL Request: $url")

    if (test == true) {
      ws.url(url)
        .withHttpHeaders("Authorization" -> s"""ResyAPI api_key="$api_key"""")
        .get
        .map(resp => resp.body)(system.dispatcher)
    } else {
      println("THIS IS A TEST RESPONSE")
      Future(apiDetails.testResponse)
    }
  }

  private[this] def stringifyQueryParams(queryParams: Map[String, String]): String = {
    queryParams.foldLeft("") {
      (combined, tuple) => {
        combined + s"""${tuple._1}=${tuple._2}"""
      }
    }
  }

  private[this] case class ApiDetails(val url: String, val testResponse: String)

}