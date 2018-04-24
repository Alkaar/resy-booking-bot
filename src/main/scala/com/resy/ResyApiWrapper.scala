package com.resy

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.resy.BookingDetails._
import com.resy.ResyApiMapKeys.{BookReservation, FindReservation, ReservationDetails, UserDetails}
import org.joda.time.DateTime
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResyApiWrapper {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  private val ws = AhcWSClient()

  private val apiMap : Map[ResyApiMapKey, ApiDetails] = Map(
    UserDetails ->
      ApiDetails(
        url = "api.resy.com/2/user",
        testSuccessResponse = """{"cc_last_4": "1234", "first_name": "John", "date_fb_token_expires": null, "mobile_number_is_verified": 0, "fb_user_id": null, "em_is_verified": 1, "payment_provider_id": 1, "num_bookings": 12, "allergies": null, "bio": null, "payment_methods": [{"id": 123456, "provider_name": "braintree", "display": "1234", "provider_id": 1, "is_default": true}], "profile_image_url": "https://s3.amazonaws.com/imagehash", "date_of_birth": null, "payment_method_id": 123456, "venue_credits": null, "payment_provider_name": "braintree", "long_code_id": 8, "date_created": 1500000000, "last_name": "Doe", "em_address": "test@gmail.com", "profile_image_id": null, "id": 1234567, "is_active": 1, "mobile_number": "+17181234567", "date_updated": 1512000000, "referral_code": "abc", "twit_user_id": null, "in_user_id": null, "payment_display": "1234", "date_app_first_opened": null}""",
        testFailureRespnse = """{"status": 419, "message": "Unauthorized"}"""),
    FindReservation ->
      ApiDetails(
        url = "api.resy.com/3/find",
        testSuccessResponse = """{"results": [{"configs": [{"table_config_id": 123456, "type": "Dining Room", "time_slot": "17:15:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123457, "day": "2018-03-20", "tags": {"inside_outside": ["inside"], "time_period": ["late"], "service_type": ["dinner"]}, "on_market": 1}, {"table_config_id": 123458, "type": "Dining Room", "time_slot": "19:15:00", "badge": null, "service_type_id": 2, "colors": {"background": "2E6D81", "font": "FFFFFF"}, "template": null, "id": 123459, "day": "2018-03-20", "tags": {"inside_outside": ["inside"], "time_period": ["late"], "service_type": ["dinner"]}, "on_market": 1}], "venue": {"rating": 4.9, "id": {"foursquare": "123456", "resy": 800, "google": "123456"}, "name": "Restaurant", "templates": {}, "metadata": {"description": "Description", "keywords": ["book", "dining", "reservations", "restaurant", "new york city", "123456", "times square", "steakhouse", "resy", "5.0 star"]}, "images": ["https://image.resy.com/1.jpg/jpg/640x360", "https://image.resy.com/2.jpg/jpg/640x360", "https://image.resy.com/3.jpg/jpg/640x360"], "config": {"enable_resypay": 0, "allow_bypass_payment_method": 1, "allow_multiple_resys": 0, "hospitality_included": 0, "enable_invite": 1}, "url_slug": "restaurant", "service_types": {"2": {"name": "dinner", "available": true, "user": {"notify": {"set": false, "matched": false, "preferences": null}}}}, "price_range": 3, "travel_time": {"distance": 0, "driving": 0, "walking": 0}, "rater": {"image": "https://s3.amazonaws.com/resy.png", "score": 5.0, "name": "Resy", "scale": 5}, "location": {"address_2": null, "postal_code": "12345", "address_1": "1 Wall St", "locality": "New York City", "neighborhood": "Times Square", "region": "NY", "time_zone": "EST5EDT", "longitude": -0.0, "latitude": 0.0}, "contact": {"phone_number": "+17181234567"}, "num_ratings": 5000, "type": "Steakhouse", "top": true, "notify": {"matched": 0}}}], "filter": [{"options": [["early", null, "early"], ["late", null, "late"]], "id": "time_period", "unavailable": false}], "query": {"day": "2018-03-20", "party_size": 2}}""",
        testFailureRespnse = """{"results": [], "query": {"day": "2018-03-21", "party_size": 2}, "filter": []}"""),
    ReservationDetails ->
      ApiDetails(
        url = "api.resy.com/3/details",
        testSuccessResponse = """{"change": {"date_cut_off": "2018-03-19T00:00:00Z"}, "venue": {"content": [{"title": "Why We Like It", "body": "Just eat here!", "locale": {"language": "en-us"}, "icon": {"url": "https://s3.amazonaws.com/heart.svg"}, "attribution": null, "display": {"type": "text"}, "name": "why_we_like_it"}, {"title": null, "body": "Prime Rib.", "locale": {"language": "en-us"}, "icon": {"url": "https://s3.amazonaws.com/quote.svg"}, "attribution": null, "display": {"type": "text"}, "name": "tagline"}, {"title": null, "body": null, "locale": null, "icon": {"url": "https://s3.amazonaws.com/chat.svg"}, "attribution": null, "display": {"type": "text"}, "name": "from_the_venue"}, {"title": "Need to Know", "body": "Please cancel 24 hours before the reservation.", "locale": {"language": "en-us"}, "icon": {"url": "https://s3.amazonaws.com/info.svg"}, "attribution": null, "display": {"type": "text"}, "name": "need_to_know"}, {"title": null, "body": "Delicious steaks!", "locale": {"language": "en-us"}, "icon": {"url": null}, "attribution": null, "display": {"type": "text"}, "name": "about"}]}, "payment": {"comp": false, "amounts": {"service_charge": {"value": null, "amount": 0.0}, "add_ons": 0.0, "price_per_unit": 0.0, "surcharge": 0.0, "quantity": 2, "tax": 0.0, "resy_fee": 0.0, "total": 0.0}, "config": {"type": "free"}, "display": {"description": [], "buy": {"before_modifier": "", "init": "", "value": "RESERVE", "action": "NOW", "after_modifier": ""}, "balance": {"modifier": "", "value": ""}}}, "viewers": {"total": 1}, "config": {"service_charge_options": null, "add_ons": null, "double_confirmation": null, "menu_items": ["Steaks", "Steaks", "More Steaks"], "features": null}, "cancellation": {"fee": {"date_cut_off": "2018-03-19T00:00:00Z", "amount": 0.0, "display": {"amount": "$0"}}, "refund": {"date_cut_off": "2018-03-18T00:00:00Z"}, "display": {"policy": ["This reservation can only be changed until 24 hours before.", "Canceling this reservation afterwards trigger a cancellation fee."]}, "credit": {"date_cut_off": "2018-03-19T00:00:00Z"}}, "locale": {"currency": "USD"}, "user": {"payment_methods": [{"is_default": true, "provider_name": "braintree", "id": 123456, "display": "1234", "provider_id": 1}]}, "book_token": {"value": "book_1|token_2|value_3", "date_expires": "2018-02-19T00:00:00Z"}}""",
        testFailureRespnse = ""),
    BookReservation ->
      ApiDetails(
        url = "api.resy.com/3/book",
        testSuccessResponse = """{"resy_token": "resy_token"}""",
        testFailureRespnse = """{"message": "Invalid user authentication credentials were provided."}""")
  )

  def sendGetRequest(resyApiMapKey: ResyApiMapKey, queryParams: Map[String, String])(implicit testing: Boolean): Future[String] = {
    val apiDetails = apiMap.get(resyApiMapKey).get
    val url = s"https://${apiDetails.url}?auth_token=$auth_token&${stringifyQueryParams(queryParams)}"

    println(s"\n${DateTime.now} URL Request: $url")


    testing match {
      case false =>
        ws.url(url)
          .withHttpHeaders("Authorization" -> s"""ResyAPI api_key="$api_key"""")
          .get
          .map(_.body)(system.dispatcher)
      case true =>
        testResponse(apiDetails)
    }
  }

  def sendPostRequest(resyApiMapKey: ResyApiMapKey, queryParams: Map[String, String])(implicit testing: Boolean): Future[String] = {
    val apiDetails = apiMap.get(resyApiMapKey).get
    val url = s"https://${apiDetails.url}"
    val post = s"auth_token=$auth_token&${stringifyQueryParams(queryParams)}"

    println(s"\n${DateTime.now} URL Request: $url")
    println(s"${DateTime.now} Post Params: $post")

    testing match {
      case false =>
        ws.url(url)
          .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded", "Authorization" -> s"""ResyAPI api_key="$api_key"""")
          .post(post)
          .map(_.body)(system.dispatcher)
      case true =>
        testResponse(apiDetails)
    }
  }

  private[this] def stringifyQueryParams(queryParams: Map[String, String]): String = {
    queryParams.foldLeft("") {
      (combined, tuple) => {
        combined + s"""${tuple._1}=${URLEncoder.encode(tuple._2, "UTF-8")}&"""
      }
    }
  }

  private[this] def testResponse(apiDetails: ApiDetails): Future[String] = {
    println("THIS IS A TEST RESPONSE")
    Thread.sleep(1000)
    Future(apiDetails.testSuccessResponse)
  }

  private[this] case class ApiDetails(val url: String, val testSuccessResponse: String, val testFailureRespnse: String)
}