package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, text, nonEmptyText}
import play.api.mvc._
import models._
import play.api.libs.json._


object Application extends play.api.mvc.Controller {

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

  private val userExpForm: Form[UserExp] = Form(
    mapping(
      "name" -> text
    )(UserExp.apply)(UserExp.unapply)
  )

  implicit val expWrites = new Writes[Experiment] {
    def writes(exp: Experiment) = Json.obj(
      "name" -> exp.name
    )
  }

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

    val user = request.session.get("user").get
    DB.experiments.get(user).get += Experiment(form.get.name)

    Ok("Creating : " + form.get.name)
  }

  def designer = Action { implicit request =>
    val form = newExpForm.bindFromRequest()
    val user = request.session.get("user").get

    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.name)

    exp match {
      case Some(value) => Ok(views.html.design(value))
      case None => NotFound(
        "The experiment <b>"+form.get.name+"</b> does not seem to exist :("
      )
    }
  }

  def expView = Action { implicit request =>

    Ok(views.html.experiments(User("ry", "muffins")))

  }

  def code = Action { implicit request =>

    val form = userExpForm.bindFromRequest()
    val user = request.session.get("user").get

    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.name)

    exp match {
      case Some(value) => Ok(views.html.code(value))
      case None => NotFound(
        "The experiment <b>"+form.get.name+"</b> does not seem to exist :("
      )
    }
  }

  def expData = Action { implicit request =>

    val form = userExpForm.bindFromRequest()
    val user = request.session.get("user").get

    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.name)

    exp match {
      case Some(value) => {
        val js = Json.toJson(value)
        Ok(Json.stringify(js))
      }
      case None => NotFound(
        "The experiment <b>"+form.get.name+"</b> does not seem to exist :("
      )
    }

  }
}