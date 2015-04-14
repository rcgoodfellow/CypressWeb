package models

import play.api.libs.json.Json
import scala.collection.mutable.{ListBuffer => L}
import models.IO._
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.util.{Failure, Success}
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

/**
*  The Cypress Project
*  Created by ry on 4/6/15.
*/

object DB {
  val experiments = Map[String, L[Experiment]](
    "ry" -> L[Experiment]()
  )

  private val driver = new MongoDriver
  private val connection = driver.connection(List("localhost"))
  private val db = connection("cypress_users")

  def save: Unit = {
    experiments.foreach(x => {
      val user = x._1
      val collection = db[JSONCollection](user)
      x._2.foreach(y => {
        val sel = Json.obj("name" -> y.name)
        val exp = Json.toJson(y)
        //collection.insert(exp)
        collection.update(selector=sel, update=exp, upsert=true)

      })
    })
  }
}

case class User(name: String, password: String) {
  def experiments : List[Experiment] = {
    val exps = DB.experiments.get(name)

    exps match {
      case Some(value) => value.toList
      case None => List()
    }

    /*
    List(
      //Experiment("Asilomar"),
      //Experiment("del Monte"),
      //Experiment("Forest Grove")
    )
    */
  }
}
