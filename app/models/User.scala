package models

import play.api.libs.json.Json
import scala.collection.mutable.{ListBuffer => L}
import models.IO._
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Future
import scala.util.{Failure, Success}
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

/**
*  The Cypress Project
*  Created by ry on 4/6/15.
*/

case class DB(username: String) {

  val experiments = L[Experiment]();

  private val driver = new MongoDriver
  private val connection = driver.connection(List("localhost"))
  private val db = connection("cypress_users")
  private val collection = db[JSONCollection](username)

  def saveAll(): Unit = experiments foreach save

  def save(exp: Experiment): Unit = {
    val sel = Json.obj("name" -> exp.name)
    val jexp = Json.toJson(exp)
    collection.update(selector=sel, update=jexp, upsert=true)
  }

  def load() : Future[DB] = {
    experiments.clear()
    val sel = Json.obj()
    collection.find(sel).cursor[Experiment].collect[List]().
     map(x => {
        experiments ++= x
        experiments foreach { xp =>
         xp.views foreach { v =>
           experiments.find(y => y.name == v.expname).foreach(res => v.exp = Some(res))
         }
        }
        this
     })
  }
}

case class User(name: String, password: String) {

  /*
  def experiments : List[Experiment] = {
    val exps = DB.users.get(name)

    exps match {
      case Some(value) => value.toList
      case None => List()
    }

    //List(
      //Experiment("Asilomar"),
      //Experiment("del Monte"),
      //Experiment("Forest Grove")
    //)
  }
*/

}
