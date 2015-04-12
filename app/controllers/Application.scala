package controllers

import java.io.{ByteArrayOutputStream, File}

import play.api.mvc._
import models._
import play.api.libs.json._
import scala.collection.mutable.{ListBuffer => L}
import javax.script.{ScriptException, ScriptEngineManager}
import requests._
import requests.Forms._

class Thing

object Application extends play.api.mvc.Controller {

  implicit val expWrites = new Writes[Experiment] {
    def writes(exp: Experiment) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers.map(x => Json.obj("name" -> x.name))
    )
  }

  implicit val coordWrites = new Writes[Coord] {
    def writes(c: Coord) = Json.obj(
      "x" -> c.x,
      "y" -> c.y
    )
  }

  implicit val compWrites = new Writes[Computer] {
    def writes(c: Computer) = Json.obj(
      "name" -> c.name,
      "xy" -> Json.toJson(c.xy)
    )
  }

  implicit val expViewWrites = new Writes[ExperimentView] {
    def writes(exp: ExperimentView) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers().map(c => Json.toJson(c))
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

    val form = expForm.bindFromRequest()

    val user = request.session.get("user").get
    val exp = Experiment(form.get.name)
    exp.views += ExperimentView("default", exp)
    DB.experiments.get(user).get += exp

    Ok("Creating : " + form.get.name)
  }

  def designer = Action { implicit request =>
    val form = expForm.bindFromRequest()
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

    val form = expForm.bindFromRequest()
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

    val form = viewForm.bindFromRequest()
    val user = request.session.get("user").get

    form.value match {
      case Some(frm) =>
        DB.experiments.get(user) match {
          case Some(user_data) =>
            user_data.find(x => x.name == frm.name) match {
              case Some(exp) =>
                exp.views.find(x => x.name == frm.view) match {
                  case Some(view) => Ok(Json.toJson(view))
                  case None => NotFound(
                    "The view <b>" + form.get.view + "</b> does not seem to exist " +
                      "for the experiment <b>" + frm.name + "</b>"); }
              case None => NotFound(
                "The experiment <b>" + frm.name + "</b> does not seem to exist :(" ) }
          case None => NotFound("The user <b>"+user+"</b> does not seemt o exist :(") }
      case None => BadRequest("Malformed Request")
    }

  }

  private val E = new ScriptEngineManager().getEngineByName("scala")
  private val settings = E.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
  private val cp =
    scala.tools.util.PathResolver.Environment.javaBootClassPath +
      File.pathSeparator + "lib/scala-library-2.11.1.jar" +
      File.pathSeparator + "target/scala-2.11/cypressweb_2.11-1.0.jar"
  settings.bootclasspath.value += cp
  settings.embeddedDefaults[Thing]
  private val ctx = E.getContext;
  E.eval("import models._", ctx)
  E.eval("import scala.collection.mutable.{ListBuffer => L}", ctx)

  def eval = Action { implicit request =>

    val form = codeForm.bindFromRequest()
    val user = request.session.get("user").get

    val src = form.get.source + ";"

    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.exp).get
    E.put("_exp", exp)
    E.eval("val exp = _exp.asInstanceOf[Experiment]", ctx)


    val baos = new ByteArrayOutputStream
    Console.withOut(baos) {
      try {
        val eval_result = E.eval(src, ctx)
        if (eval_result != null) {
          Ok(eval_result.toString)
        }
        else {
          Ok(baos.toString("UTF-8"))
        }
      }
      catch {
        case ex: ScriptException =>
          Ok(baos.toString("UTF-8"))
      }
    }

  }

  def updateXY = Action { implicit request =>

    val form = visualUpdateForm.bindFromRequest()
    val user = request.session.get("user").get

    val fdata = form.get
    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.exp).get

    if(fdata.typ == "computer") {
      val comp = exp.computers.find(c => c.name == fdata.name)
      comp.foreach(c => {
        c.xy.x = fdata.x
        c.xy.y = fdata.y
      })
    }

    Ok("")
  }
}