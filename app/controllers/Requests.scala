package controllers.requests

/**
 * The Cypress Project
 * Created by ry on 4/11/15.
 */

import play.api.data.Form
import play.api.data.Forms.{mapping, text, nonEmptyText}
import models.User

case class Exp(name: String)

case class View(name: String, view: String)

case class Code(source: String, exp: String)

object Forms {

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