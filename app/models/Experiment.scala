package models

/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

import scala.collection.mutable.{ListBuffer => L}
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Kinds {
  val COMPUTER:Int = 0
  val INTERFACE:Int = 1
  val ACTUATOR:Int = 2
  val SENSOR: Int = 3
  val SUBSTRATE: Int = 4
}

case class PathElement(kind: Int, name: String)
case class VisualUpdate(path: List[PathElement], x: Double, y: Double, exp: String)

case class CartesianCoord(var x: Double, var y: Double)
case class RadialCoord(var theta: Double)

trait VisualComponent {
  val xy: CartesianCoord = CartesianCoord(0,0)
}

//Software Type Family -------------------------------------------------------------------
trait Software extends VisualComponent {
  var name: String
  lazy val kind = getClass.getTypeName
  def write : JsValue
}

object Software {
  val readers = Map(
    GenericSoftware.getClass.getTypeName -> GenericSoftware.read _,
    Controller.getClass.getTypeName -> Controller.read _
  )
}


case class GenericSoftware (
  var name: String
) extends Software {

 def write = {
   implicit val w = Json.writes[GenericSoftware]
   Json.toJson(this)
 }

}

object GenericSoftware {
  implicit val r = Json.reads[GenericSoftware]
  def read(x: JsValue) = Json.fromJson[GenericSoftware](x)
}


case class Controller (
  var name: String,
  in: L[String], out: L[String],
  cg: CodeGen
) extends Software {

  def write = {
    implicit val cw = Json.writes[CodeGen]
    implicit val w = Json.writes[Controller]
    Json.toJson(this)
  }

}

object Controller {
  implicit val codeGenReads = Json.reads[CodeGen]
  implicit val r = Json.reads[Controller]
  def read(x: JsValue) = Json.fromJson[Controller](x)
}

case class Interface(name: String, hostname: String, override val xy: CartesianCoord = CartesianCoord(15,0))
  extends VisualComponent {
  val substrates = L[Substrate]()
  var host: Option[Host] = None

  override def toString = {
    "name: " + name + "\n" +
    "substrates: " + substrates.map(_.name).mkString("[",",","]") + "\n" +
    "host: " + hostname + "\n"
  }

  def getPath : List[PathElement] = List(
    PathElement(Kinds.COMPUTER, hostname),
    PathElement(Kinds.INTERFACE, name)
  )

  def connect(s: Substrate): Interface = {
    substrates += s
    s.interfaces += this
    this
  }
}

trait Host {
  var name: String
  val os: L[String]
  val software: L[Software]
  val interfaces: L[Interface]
  val kind: Int

  def addInterface(name: String) : Host = {
    val ifx = Interface(name, this.name)
    ifx.host = Some(this)
    interfaces += ifx
    this
  }
}

object Host {
  def apply(name: String, os: L[String], software: L[Software],
             interfaces: L[Interface], kind: Int) : Host = {
    kind match {
      case Kinds.COMPUTER => Computer(name, os, software, interfaces)
      case Kinds.ACTUATOR => Actuator(name)
      case Kinds.SENSOR => Sensor(name)
    }
  }

  def unapply(h: Host)
    : Option[(String, L[String], L[Software], L[Interface], Int)] = {
    Some(h.name, h.os, h.software, h.interfaces, h.kind)
  }
}

case class Computer(
  var name: String,
  os: L[String] = L("linux"),
  software: L[Software] = L[Software](),
  interfaces: L[Interface] = L[Interface](),
  override val xy: CartesianCoord = CartesianCoord(0,0)
) extends Host with VisualComponent {

  val kind = Kinds.COMPUTER

  def interface(x: String) = interfaces.find(x => x.name == x.name).get

  override def toString = {
    "name: " + name + "\n" +
    "os: " + os.mkString("[",",","]") + "\n" +
    "software: " + software.mkString("[",",","]") + "\n" +
    "interfaces: " + interfaces.mkString("[",",","]")
  }
}

/*
object Computer {
  def VComputer(name: String, os: L[String], software: L[Software],
                interfaces: L[Interface], coord: CartesianCoord)
  : Computer = {
    val c = Computer(name, os, software, interfaces)
    c
  }
}
*/
//todo back to trait with concrete empty implementer
case class CodeGen(name: String)

//object RCGen extends CodeGen {}


case class Coupling(x: String)

case class PLink(a: Coupling, b: Coupling)

case class CLink(a: Interface, b: Interface)

case class PObject(name: String, couplings: L[Coupling])
  extends VisualComponent{
  def coupling(x: String) = couplings.find(c => c.x == x).get
}

case class Actuator(
  var name: String,
  coupling : Coupling = Coupling("out")
) extends Host
{
  val kind = Kinds.ACTUATOR
  val interfaces = L(Interface("in", this.name))
  interfaces(0).host = Some(this)
  val software = L[Software]()
  val os = L[String]("embedded")
}

case class Sensor(
  var name: String,
  coupling : Coupling = Coupling("in")
) extends Host
{
  val kind = Kinds.SENSOR
  val interfaces = L(Interface("out", this.name))
  interfaces(0).host = Some(this)
  val software = L[Software]()
  val os = L("embedded")
}

case class Substrate(name: String,
  interfaces: L[Interface] = L[Interface](), override val xy: CartesianCoord = CartesianCoord(0,0))
  extends VisualComponent
{
  def interface(x: String) = interfaces.find(i => i.name == x).get
}

case class Experiment(name: String,
  computers:  L[Computer]   = L(),
  objects:    L[PObject]    = L(),
  sensors:    L[Sensor]     = L(),
  actuators:  L[Actuator]   = L(),
  clinks:     L[CLink]      = L(),
  plinks:     L[PLink]      = L(),
  substrates: L[Substrate]  = L(),
  views: L[ExperimentView] = L()
){
  def sensor(x: String) = sensors.find(s => s.name == x).get
  def actuator(x: String) = actuators.find(a => a.name == x).get
  def plink(a: Coupling, b: Coupling){ plinks += PLink(a, b) }
  def clink(a: Interface, b: Interface){ clinks += CLink(a, b) }
}

case class Extent(var x: Double, var y: Double)

case class ExperimentView(name: String, expname: String)
{
  override def toString: String = "ExperimentView"

  var exp: Option[Experiment] = None

  def computers : () => L[Computer] =
    () => exp.get.computers

  def objects : () => L[PObject] =
    () => exp.get.objects

  def substrates : () => L[Substrate] =
    () => exp.get.substrates

  def visuals[T <: VisualComponent] = List(computers(), objects(), substrates())

  def extent : Extent = {
    val x =
      visuals.map(l => l.minBy(a => a.xy.x).xy.x).min -
      visuals.map(l => l.maxBy(a => a.xy.x).xy.x).max

    val y =
      visuals.map(l => l.minBy(a => a.xy.y).xy.y).min -
      visuals.map(l => l.maxBy(a => a.xy.y).xy.y).max

    Extent(x, y)
  }
}


object IO {

  implicit val ccoordWrites = new Writes[CartesianCoord] {
    def writes(c: CartesianCoord) = Json.obj(
      "x" -> c.x,
      "y" -> c.y
    )
  }

  implicit val rcoordWrites = new Writes[RadialCoord] {
    def writes(r: RadialCoord) = Json.obj(
      "theta" -> r.theta
    )
  }

  implicit val interfaceWrites = new Writes[Interface] {
    def writes(i: Interface) = Json.obj(
      "name" -> i.name,
      "host" -> i.hostname,
      "xy" -> Json.toJson(i.xy),
      "substrates" -> i.substrates.map(x => x.name)
    )
  }

  implicit val fCarto = Json.format[CartesianCoord]

  implicit val interfaceReads : Reads[Interface] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "host").read[String] and
    (JsPath \ "xy").read[CartesianCoord]
  )(Interface.apply _)


  implicit val pathElementWrites = new Writes[PathElement] {
    def writes(p: PathElement) = Json.obj(
      "kind" -> p.kind,
      "name" -> p.name
    )
  }

  implicit val substrateReads : Reads[Substrate] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "interfaces").read[L[Interface]] and
    (JsPath \ "xy").read[CartesianCoord]
  )(Substrate.apply _)

  implicit val substrateWrites = new Writes[Substrate] {
    def writes(s: Substrate) = Json.obj(
      "name" -> s.name,
      "interfaces" -> s.interfaces.map(_.getPath.map(x => Json.toJson(x))),
      "xy" -> Json.toJson(s.xy)
    )
  }

  implicit val expViewDBWrites = new Writes[ExperimentView] {
    def writes(exp: ExperimentView) = Json.obj(
      "name" -> exp.name,
      "expname" -> exp.expname
    )
  }


  implicit val expViewReads : Reads[ExperimentView] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "expname").read[String]
  )(ExperimentView.apply _)


  implicit val softwareReads = new Format[Software] {
    def reads(v: JsValue) : JsResult[Software] = {
      val kind = (v \ "kind").as[String]
      Software.readers(kind)(v)
    }
    def writes(s: Software) = s.write
  }

  implicit val compWrites = new Writes[Computer] {
    def writes(c: Computer) = Json.obj(
      "name" -> c.name,
      "os" -> c.os,
      "software" -> c.software.map(x => Json.toJson(x)),
      "interfaces" -> c.interfaces.map(x => Json.toJson(x)),
      "xy" -> Json.toJson(c.xy)
    )
  }


  implicit val compReads : Reads[Computer] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "os").read[L[String]] and
      (JsPath \ "software").read[L[Software]] and
      (JsPath \ "interfaces").read[L[Interface]] and
      (JsPath \ "xy").read[CartesianCoord]
    )(Computer.apply _)

  implicit val fHost = Json.format[Host]
  implicit val fCoupling = Json.format[Coupling]
  implicit val fPObject = Json.format[PObject]
  implicit val fSensor = Json.format[Sensor]
  implicit val fActuator = Json.format[Actuator]
  implicit val fCLink = Json.format[CLink]
  implicit val fPLink = Json.format[PLink]
  implicit val fSubstrate = Json.format[Substrate]

  implicit val expViewClientWrites = new Writes[ExperimentView] {
    def writes(exp: ExperimentView) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers().map(c => Json.toJson(c)(compWrites)),
      "substrates" -> exp.substrates().map(s => Json.toJson(s))
    )
  }

  implicit val expWrites = new Writes[Experiment] {
    def writes(exp: Experiment) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers.map(c => Json.toJson(c)(compWrites)),
      "objects" -> exp.objects.map(o => Json.toJson(o)),
      "sensors" -> exp.sensors.map(s => Json.toJson(s)(fSensor)),
      "actuators" -> exp.actuators.map(a => Json.toJson(a)(fActuator)),
      "clinks" -> exp.clinks.map(c => Json.toJson(c)),
      "plinks" -> exp.plinks.map(p => Json.toJson(p)),
      "substrates" -> exp.substrates.map(s => Json.toJson(s)),
      "views" -> exp.views.map(v => Json.toJson(v)(expViewDBWrites))
    )
  }

  implicit val expReads : Reads[Experiment] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "computers").read[L[Computer]] and
      (JsPath \ "objects").read[L[PObject]] and
      (JsPath \ "sensors").read[L[Sensor]] and
      (JsPath \ "actuators").read[L[Actuator]] and
      (JsPath \ "clinks").read[L[CLink]] and
      (JsPath \ "plinks").read[L[PLink]] and
      (JsPath \ "substrates").read[L[Substrate]] and
      (JsPath \ "views").read[L[ExperimentView]]
  )(Experiment.apply _)

  implicit val pathElementsReads : Reads[PathElement] = (
      (JsPath \ "kind").read[Int] and
      (JsPath \ "name").read[String]
    )(PathElement.apply _)

  implicit val visualUpdateReads : Reads[VisualUpdate] = (
    (JsPath \ "path").read[List[PathElement]] and
      (JsPath \ "x").read[Double] and
      (JsPath \ "y").read[Double] and
      (JsPath \ "exp").read[String]
    )(VisualUpdate.apply _)
}

