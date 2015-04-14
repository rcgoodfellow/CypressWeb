package models

import scala.collection.mutable.{ListBuffer => L}

/**
*  The Cypress Project
*  Created by ry on 4/6/15.
*/

object DB {
  val experiments = Map[String, L[Experiment]](
    "ry" -> L[Experiment]()
  )

  def save: Unit = {

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
