package com.sghaida.akka.actors.scheduler.traites

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.sghaida.models.{ConfigModel, ConfigModels}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  import spray.json.RootJsonFormat

  implicit val configFotmat: RootJsonFormat[ConfigModel] = jsonFormat8(ConfigModel.apply)
  implicit val listOfConfigsFormat: RootJsonFormat[ConfigModels] = jsonFormat1(ConfigModels)
}
