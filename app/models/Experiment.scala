package models

/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

import controllers.requests.{PathElement}

import scala.collection.mutable.{ListBuffer => L}

object Kinds {
  val COMPUTER:Int = 0
  val INTERFACE:Int = 1
  val ACTUATOR:Int = 2
  val SENSOR: Int = 3
  val SUBSTRATE: Int = 4
}

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
}

trait Host {
  var name: String
  val os: L[String]
  val software: L[Software]
  val interfaces: L[Interface]
  val kind: Int
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

