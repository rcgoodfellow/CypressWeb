package models

import reactivemongo.bson.{BSONDocument, BSONInteger, BSONValue}
import reactivemongo.core.commands.{Project, Unwind, Aggregate}
import play.modules.reactivemongo.json.BSONFormats.toJSON


import scala.collection.mutable.{ListBuffer => L}
import cypress.model._
import play.api.libs.json.{Json}
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

  //val loadExp()

  def load() : Future[DB] = {
    experiments.clear()
    val sel = Json.obj()
    val proj = Json.obj("experiments" -> 1)
    val cmd = Aggregate(username, Seq(
      Project("experiments" -> BSONInteger(1))
    ))
    import play.modules.reactivemongo.json.BSONFormats._
    val barf =
      db.command(cmd).map(x => {
        val xl = x.toList
        val jxl = Json.toJson(xl(0))
        val abc = jxl \ "experiments"
        val efg = Json.fromJson[List[Experiment]](abc)
        efg.foreach(j => println(j))
        efg
        //abc.map(y => Json.fromJson[Experiment](y))
      }).map(z => {
        println(z)
        z.foreach(xz => xz.map(xxpp => experiments += xxpp))
        experiments foreach { xp => //attach views to experiment instances
          xp.views foreach { v =>
            experiments.find(y => y.name == v.expname).foreach(res => v.exp = Some(res))
          }
        }
        this
      })

    /*
    val fexp =
      collection.find(sel, proj).cursor[Experiment].collect[List]().
      //db.command(cmd).cursor[Experiment].collect[List]().
       map(x => {
          experiments ++= x
          experiments foreach { xp => //attach views to experiment instances
           xp.views foreach { v =>
             experiments.find(y => y.name == v.expname).foreach(res => v.exp = Some(res))
           }
          }
          this
       })
    fexp
    */

    barf

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
