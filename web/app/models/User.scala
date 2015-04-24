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

  def saveAll(): Unit = {
    experiments foreach saveExp
    controllers foreach saveController
  }

  def saveExp(exp: Experiment): Unit = {
    val sel = Json.obj("experiments.name" -> exp.name)
    val update = Json.obj("$set" -> Json.obj("experiments.$" -> Json.toJson(exp)))
    collection.update(selector=sel, update=update, upsert=true)
  }

  def saveController(ct: Controller): Unit = {
    //val sel = Json.obj("controllers.name" -> ct.name)
    //val update = Json.obj("$set" -> Json.obj("controllers.$" -> Json.toJson(ct)))
    //println("bongo-mongo")
    //println(update)
    //collection.update(selector=sel, update=update, upsert=true)

    val sel = Json.obj()
    val pull =
      Json.obj("$pull" ->
        Json.obj("controllers" -> Json.obj("name" -> ct.name))
      )
    collection.update(selector=sel, update=pull)

    val push =
      Json.obj("$push" ->
        Json.obj("controllers" -> ct)
      )
    collection.update(selector=sel, update=push)
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

  def loadControls : Future[DB] = {
    val cmd = Aggregate(username, Seq(Project("controllers" -> BSONInteger(1))))

    val ctrlExtratctor = { x:Stream[BSONDocument] =>
      Json.fromJson[List[Controller]](Json.toJson(x.head) \ "controllers").get
    }

    val populateControls = { x:List[Controller] =>
      x.foreach(ct => controllers += ct)
      this
    }

    db.command(cmd)
      .map(ctrlExtratctor)
      .map(populateControls)
  }

  def load() : Future[DB] = {
    experiments.clear()
    loadExp.flatMap(_ => loadControls)
  }
}

case class User(name: String, password: String)

