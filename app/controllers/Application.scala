package controllers

import java.io.{ByteArrayOutputStream, File}

import play.api.data.Form
import play.api.data.Forms.{mapping, text, nonEmptyText}
import play.api.mvc._
import models._
import play.api.libs.json._
import scala.collection.mutable.{ListBuffer => L}
import javax.script.{ScriptException, ScriptEngineManager}

class Thing

object Alligator {
  def chomp = "womp womp"
}

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

  private val codeEvalForm: Form[CodeEval] = Form(
    mapping(
      "source" -> text,
      "exp" -> text
    )(CodeEval.apply)(CodeEval.unapply)
  )

  implicit val expWrites = new Writes[Experiment] {
    def writes(exp: Experiment) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers.map(x => Json.obj("name" -> x.name))
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
    val exp = Experiment(form.get.name)
    exp.computers += Computer(name="tc", os=L("Linux"))
    DB.experiments.get(user).get += exp

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

  private val E = new ScriptEngineManager().getEngineByName("scala")
  private val settings = E.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
  private val cp =
    scala.tools.util.PathResolver.Environment.javaBootClassPath +
      File.pathSeparator + "lib/scala-library-2.11.1.jar" +
      File.pathSeparator + "target/scala-2.11/cypressweb_2.11-1.0.jar"
  settings.bootclasspath.value += cp
  settings.embeddedDefaults[Thing]
  private val ctx = E.getContext;

  def eval = Action { implicit request =>

    val form = codeEvalForm.bindFromRequest()
    val user = request.session.get("user").get

    val src = form.get.source + ";";

    val exp = DB.experiments.get(user).get.find(x => x.name == form.get.exp).get

    E.eval("import models._", ctx)
    E.put("_exp", exp)
    E.eval("val exp = _exp.asInstanceOf[Experiment]", ctx)
    E.eval("import scala.collection.mutable.{ListBuffer => L}", ctx)

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
}