/**
 * The Cypress Project
 * Created by ry on 4/13/15.
 */

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.util.{Failure, Success}
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

object DBMuffins {

  def dbTest(): Unit = {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("cypress")
    val collection = db[JSONCollection]("meta")
    val query = Json.obj()
    collection.find(query).cursor[JsObject].collect[List]().
      onComplete {
      case Success(y) => y.foreach(d => println(d))
      case Failure(_) => println("shart")
    }

  }

}
