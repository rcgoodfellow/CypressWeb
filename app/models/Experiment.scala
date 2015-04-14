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
  var xy: CartesianCoord = CartesianCoord(0,0)
}

trait Software extends VisualComponent {
  val name: String
}

case class Interface(name: String, host: Host) extends VisualComponent {
  val substrates = L[Substrate]()
  xy = CartesianCoord(15,0)

  override def toString = {
    "name: " + name + "\n" +
    "substrates: " + substrates.map(_.name).mkString("[",",","]") + "\n" +
    "host: " + host.name + "\n"
  }

  def getPath : List[PathElement] = List(
    PathElement(Kinds.COMPUTER, host.name),
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
    interfaces += Interface(name, this)
    this
  }
}

case class Computer(
  var name: String,
  os: L[String] = L("linux"),
  software: L[Software] = L[Software](),
  interfaces: L[Interface] = L[Interface]()
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

trait CodeGen

object RCGen extends CodeGen {}

case class Controller(
  name: String,
  in: L[String], out: L[String],
  cg: CodeGen
) extends Software

case class Coupling(x: String)

case class PLink(a: Coupling, b: Coupling)

case class CLink(a: Interface, b: Interface)

trait PObject extends VisualComponent{
  val name: String
  val couplings: L[Coupling] = L[Coupling]()
  def coupling(x: String) = couplings.find(c => c.x == x).get
}

case class Actuator(
  var name: String,
  coupling : Coupling = Coupling("out")
) extends Host
{
  val kind = Kinds.ACTUATOR
  val interfaces = L(Interface("in", this))
  val software = L[Software]()
  val os = L[String]("embedded")
}

case class Sensor(
  var name: String,
  coupling : Coupling = Coupling("in")
) extends Host
{
  val kind = Kinds.SENSOR
  val interfaces = L(Interface("out", this))
  val software = L[Software]()
  val os = L("embedded")
}

case class Substrate(name: String, interfaces: L[Interface] = L[Interface]())
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


case class ExperimentView(name: String, exp: Experiment)
{
  override def toString: String = "ExperimentView"

  def computers : () => L[Computer] =
    () => exp.computers

  def objects : () => L[PObject] =
    () => exp.objects

  def substrates : () => L[Substrate] =
    () => exp.substrates

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
      "xy" -> Json.toJson(i.xy),
      "substrates" -> i.substrates.map(x => x.name)
    )
  }

  implicit val compWrites = new Writes[Computer] {
    def writes(c: Computer) = Json.obj(
      "name" -> c.name,
      "xy" -> Json.toJson(c.xy),
      "interfaces" -> c.interfaces.map(x => Json.toJson(x))
    )
  }

  implicit val pathElementWrites = new Writes[PathElement] {
    def writes(p: PathElement) = Json.obj(
      "kind" -> p.kind,
      "name" -> p.name
    )
  }

  implicit val substrateWrites = new Writes[Substrate] {
    def writes(s: Substrate) = Json.obj(
      "name" -> s.name,
      "xy" -> Json.toJson(s.xy),
      "interfaces" -> s.interfaces.map(_.getPath.map(x => Json.toJson(x)))
    )
  }

  implicit val expViewWrites = new Writes[ExperimentView] {
    def writes(exp: ExperimentView) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers().map(c => Json.toJson(c)),
      "substrates" -> exp.substrates().map(s => Json.toJson(s))
    )
  }

  implicit val expWrites = new Writes[Experiment] {
    def writes(exp: Experiment) = Json.obj(
      "name" -> exp.name,
      "computers" -> exp.computers.map(c => Json.toJson(c)),
      "substrates" -> exp.substrates.map(s => Json.toJson(s))
    )
  }

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

