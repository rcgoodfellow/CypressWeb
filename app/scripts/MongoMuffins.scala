/**
 * The Cypress Project
 * Created by ry on 4/13/15.
 */

import scala.util.{Failure, Success}
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global

object DBMuffins {

  def dbTest(): Unit = {
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("muffins")
    val collection = db[BSONCollection]("test")
    val query = BSONDocument()
    collection.find(query).cursor[BSONDocument].collect[List]().
      onComplete {
      case Success(y) => y.foreach(d => println(BSONDocument.pretty(d)))
      case Failure(_) => println("shart")
    }

  }

}
