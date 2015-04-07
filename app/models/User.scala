package models

/**
*  The Cypress Project
*  Created by ry on 4/6/15.
*/

case class User(name: String, password: String) {
  def experiments : List[Experiment] = {
    List(
      Experiment("Asilomar"),
      Experiment("del Monte"),
      Experiment("Forest Grove")
    )
  }
}
