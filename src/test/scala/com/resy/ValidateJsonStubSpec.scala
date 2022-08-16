package com.resy

import org.apache.logging.log4j.scala.Logging
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

import java.io.{File, FileInputStream}
import scala.io.Source
import scala.util.{Failure, Success, Try}

class ValidateJsonStubSpec extends AnyFlatSpec with Matchers with Logging {

  behavior of "ValidateJsonStubSpec"
  it should "validate JSON stubs are valid JSON" in {
    val rootPath  = new File(".").getCanonicalPath
    val jsonFiles = new File(s"$rootPath/src/test/resources").listFiles.toSeq

    for {
      jsonFile <- jsonFiles
    } yield {
      val jsonBufferedSource = Source.fromInputStream(new FileInputStream(jsonFile))

      val json =
        try jsonBufferedSource.mkString
        finally jsonBufferedSource.close()

      Try(Json.parse(json)) match {
        case Success(_) =>
          logger.info(s"Validated $jsonFile")
          succeed
        case Failure(_) => fail(s"$jsonFile failed JSON validation")
      }
    }
  }
}
