package controllers


import models._
import javax.script.{ScriptException, ScriptEngineManager}
import cypress.model._
import cypress.model.IO._
import org.apache.commons.io.output.NullOutputStream
import requests.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import akka.actor.ActorRef
import java.io.{ByteArrayOutputStream, File}

import scala.concurrent.Future

class Thing

object Application extends play.api.mvc.Controller {

  def index = Action {
    Ok(views.html.login(loginForm))
  }

  private var db : Option[DB] = None

  def login = Action.async { implicit request =>
    val form = loginForm.bindFromRequest()
    if(form.get.name == "ry" && form.get.password == "muffins") {
      db = Some(DB("ry"))
      db.get.load().map(
        x => Ok(views.html.welcome(form.get, db.get)).withSession("user" -> form.get.name))
    }
    else {
      Future{ Ok(views.html.login(form)).withNewSession }
    }
  }

  def newExp = Action { implicit request =>

    val form = expForm.bindFromRequest()

    val user = request.session.get("user").get
    val exp = Experiment(form.get.name)
    val view = ExperimentView("default", exp.name)
    view.exp = Some(exp)
    exp.views += view
    //DB.users.get(user).get += exp
    db.get.experiments += exp

    Ok("Creating : " + form.get.name)
  }

  def designer = Action { implicit request =>
    val form = expForm.bindFromRequest()
    val user = request.session.get("user").get

    //val exp = DB.users.get(user).get.find(x => x.name == form.get.name)
    val exp = db.get.experiments.find(x => x.name == form.get.name)

    exp match {
      case Some(value) => Ok(views.html.design(value))
      case None => NotFound(
        "The experiment <b>"+form.get.name+"</b> does not seem to exist :("
      )
    }
  }

  def expView = Action { implicit request =>

    Ok(views.html.experiments(db.get))

  }

  def code = Action { implicit request =>

    val form = expForm.bindFromRequest()
    val user = request.session.get("user").get

    //val exp = DB.users.get(user).get.find(x => x.name == form.get.name)
    val exp = db.get.experiments.find(x => x.name == form.get.name)

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
        db.get.experiments.find(x => x.name == frm.name) match {
          case Some(exp) =>
            exp.views.find(x => x.name == frm.view) match {
              case Some(view) => Ok(Json.toJson(view)(expViewClientWrites))
              case None => NotFound(
                "The view <b>" + form.get.view + "</b> does not seem to exist " +
                  "for the experiment <b>" + frm.name + "</b>"); }
          case None => NotFound(
            "The experiment <b>" + frm.name + "</b> does not seem to exist :(" ) }
      case None => BadRequest("Malformed Request")
    }

  }

  def ucontrollers = Action { implicit request =>

    val user = request.session.get("user").get

    Ok(views.html.controller(db.get.controllers.toList))
  }


  private val E = new ScriptEngineManager().getEngineByName("scala")
  private val settings = E.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings

  private val cp = 
    scala.tools.util.PathResolver.Environment.javaBootClassPath +
    File.pathSeparator + "web/lib/scala-library-2.11.6.jar" +
    //File.pathSeparator + "model/target/scala-2.11/model_2.11-0.1.0.jar" +
    File.pathSeparator + "model/target/scala-2.11/model-assembly-0.1.0.jar" +
    File.pathSeparator + "io/target/scala-2.11/io_2.11-0.1.0.jar" +
    File.pathSeparator + "web/target/scala-2.11/web_2.11-0.1.0.jar"
      
  settings.bootclasspath.value = cp
  settings.embeddedDefaults[Thing]
  private val ctx = E.getContext
  E.eval("import cypress.model._", ctx)
  E.eval("import scala.collection.mutable.{ListBuffer => L}", ctx)

  def eval = Action { implicit request =>

    val form = codeForm.bindFromRequest()
    val user = request.session.get("user").get

    val src = form.get.source + ";"
    val exp = db.get.experiments.find(x => x.name == form.get.exp).get
    val controls = db.get.controllers
    Console.withOut(new NullOutputStream){
      E.put("_exp", exp)
      E.put("_controls", controls)
    }
    E.eval("val exp = _exp.asInstanceOf[Experiment]", ctx)
    E.eval("val controls = _controls.asInstanceOf[L[Controller]]", ctx)

    var result = Ok(":(")
    val baos = new ByteArrayOutputStream
    Console.withOut(baos) {
      try {
        val eval_result = E.eval(src, ctx)
        if (eval_result != null) {
          //db.get.saveExp(exp)
          db.get.saveAll()
          boom.foreach(ak => ak ! Json.toJson(exp.views.head)(expViewClientWrites))
          result = Ok(eval_result.toString)
        }
        else {
          db.get.saveAll()
          result = Ok(baos.toString("UTF-8"))
        }
      }
      catch {
        case ex: ScriptException =>
          result = Ok(baos.toString("UTF-8"))
      }
    }
    println(baos.toString("UTF-8"))
    result
  }

  def findExpObject(o: PathElement, os: List[PathElement], exp: Experiment)
    : VisualComponent = {
     o.kind match {
       case Kinds.COMPUTER =>
         val c = exp.computers.find(x => o.name == x.name).get
         if(os.nonEmpty) {
           val x :: xs = os
           findComputerObject(x, xs, c)
         }
         else c

       case Kinds.SUBSTRATE =>
         exp.substrates.find(x => o.name == x.name).get
     }
  }

  def findComputerObject(o: PathElement, os: List[PathElement], c: Computer)
  : VisualComponent = {
    o.kind match {
      case Kinds.INTERFACE =>
        c.interface(o.name)
    }
  }

  def updateXY() = Action { implicit request =>

    val user = request.session.get("user").get
    val vup = Json.fromJson[VisualUpdate](request.body.asJson.get).get
    //val exp = DB.users.get(user).get.find(x => x.name == vup.exp).get
    val exp = db.get.experiments.find(x => x.name == vup.exp).get

    val p::ps = vup.path
    val vobj = findExpObject(p, ps, exp)
    vobj.xy.x = vup.x
    vobj.xy.y = vup.y

    db.get.saveExp(exp)

    Ok("")
  }

  private var boom : Option[ActorRef] = None

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    boom = Some(out)
    BoomSocketActor.props(out)
  }
}
