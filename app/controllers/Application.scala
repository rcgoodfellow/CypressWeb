package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, text, nonEmptyText}
import play.api.mvc._
import models.{Experiment, NewExp, User}


object Application extends Controller {

  private val loginForm: Form[User] = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(User.apply)(User.unapply)
  )

  private val newExpForm: Form[NewExp] = Form(
    mapping(
      "name" -> text
    )(NewExp.apply)(NewExp.unapply)
  )

  def index = Action {
    Ok(views.html.login(loginForm))
  }

  def login = Action { implicit request =>
    val form = loginForm.bindFromRequest()
    if(form.get.name == "ry" && form.get.password == "muffins") {
      Ok(views.html.welcome(form.get)).withSession("user" -> form.get.name)
    }
    else {
      Ok(views.html.login(form)).withNewSession
    }
  }

  def newExp = Action { implicit request =>
    val form = newExpForm.bindFromRequest()
    Ok(views.html.design(Experiment(form.get.name)))
  }
}