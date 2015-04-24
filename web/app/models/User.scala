package models

import reactivemongo.bson.{BSONDocument, BSONInteger}
import reactivemongo.core.commands.{Project, Aggregate}
import play.modules.reactivemongo.json.BSONFormats._

import scala.collection.mutable.{ListBuffer => L}
import cypress.model._
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Future
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global
import cypress.model.IO._

/**
*  The Cypress Project
*  Created by ry on 4/6/15.
*/

case class DB(username: String) {

  val experiments = L[Experiment]()
  val controllers = L[Controller]()

  private val driver = new MongoDriver
  private val connection = driver.connection(List("localhost"))
  private val db = connection("cypress_users")
  private val collection = db[JSONCollection](username)

  def saveAll(): Unit = experiments foreach save

  def save(exp: Experiment): Unit = {
    val sel = Json.obj("experiments.name" -> exp.name)
    val jexp = Json.toJson(exp)
    val uppd = Json.obj("$set" -> Json.obj("experiments.$" -> jexp))
    collection.update(selector=sel, update=uppd, upsert=true)
  }

  def loadExp : Future[DB] = {
    val cmd = Aggregate(username, Seq(Project("experiments" -> BSONInteger(1))))

    val expExtractor = { x:Stream[BSONDocument] =>
      Json.fromJson[List[Experiment]](Json.toJson(x.head) \ "experiments").get
    }

    val populateExperiments = { x:List[Experiment] =>
      x.foreach(xp => experiments += xp)
      experiments foreach {
        _.views foreach { v =>
          v.exp = experiments.collectFirst({case xp if xp.name == v.expname => xp})
        }
      }
      this
    }

    db.command(cmd)
      .map(expExtractor)
      .map(populateExperiments)
  }

  def load() : Future[DB] = {
    experiments.clear()
    loadExp
  }
}

case class User(name: String, password: String)

