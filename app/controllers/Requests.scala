package controllers.requests

/**
 * The Cypress Project
 * Created by ry on 4/11/15.
 */

import play.api.data.Form
import play.api.data.Forms.{mapping, text, of, list, number}
import models.User
import play.api.data.format.Formats._

case class Exp(name: String)

case class View(name: String, view: String)

case class Code(source: String, exp: String)

case class PathElement(kind: Int, name: String)
case class VisualUpdate(path: List[PathElement], x: Double, y: Double, exp: String)

object Forms {

  val visualUpdateForm: Form[VisualUpdate] = Form(
    mapping(
      "path" -> list[PathElement](
        mapping(
          "kind" -> number,
          "name" -> text
        )(PathElement.apply)(PathElement.unapply)
      ),
      "x" -> of(doubleFormat),
      "y" -> of(doubleFormat),
      "exp" -> text
    )(VisualUpdate.apply)(VisualUpdate.unapply)
  )

  val loginForm: Form[User] = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(User.apply)(User.unapply)
  )
  val expForm: Form[Exp] = Form(
    mapping(
      "name" -> text
    )(Exp.apply)(Exp.unapply)
  )

  val viewForm: Form[View] = Form(
    mapping(
      "name" -> text,
      "view" -> text
    )(View.apply)(View.unapply)
  )

  val codeForm: Form[Code] = Form(
    mapping(
      "source" -> text,
      "exp" -> text
    )(Code.apply)(Code.unapply)
  )
}