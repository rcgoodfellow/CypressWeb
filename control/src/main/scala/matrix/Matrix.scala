package matrix

import cypress.control.{ControlAgent, DoublePub}

import scala.util.Random

/**
 * The Cypress Project
 * Created by ry on 4/21/15.
 */

object Tag {
  val spicy = "spicy".hashCode
  val taco = "taco".hashCode
  val crunch = "crunch".hashCode

  val pool = List(spicy, taco, crunch)
}

object Names {
  var agents = Map[Int,String](
    "Smith".hashCode -> "Smith",
    "Jones".hashCode -> "Jones",
    "Brown".hashCode -> "Brown"
  )

  var topics = Map[Int,String](
    "spicy".hashCode -> "spicy",
    "taco".hashCode -> "taco",
    "crunch".hashCode -> "crunch"
  )
}

object Printers {

  def showdp(dp: DoublePub): String =
    f"${Names.agents(dp.sender)} ---> [${Names.topics(dp.topic)}]:${dp.x}"

}


