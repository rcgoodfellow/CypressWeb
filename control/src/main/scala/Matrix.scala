package cypress.control.matrix

import cypress.control.{DoublePub, ControlAgent}
import cypress.control.util._

/**
 * The Cypress Project
 * Created by ry on 4/21/15.
 */

object Tag {
  val spicy = "spicy".hashCode
  val taco = "taco".hashCode
  val crunch = "crunch".hashCode
}


class Smith extends ControlAgent {

  def rx = {
    case DoublePub(Tag.spicy, x) => ???
    case DoublePub(Tag.taco, x) => ???
    case DoublePub(Tag.crunch, x) => ???
  }

}

class Brown {

}

class Jones {

}