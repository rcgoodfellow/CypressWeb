package models

/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

import scala.collection.mutable.{ListBuffer => L}

trait Software {
  val name: String
}

case class Interface(name: String)

case class Computer(
  name: String,
  os: L[String],
  software: L[Software] = L[Software](),
  interfaces: L[Interface] = L[Interface]()
){
  def interface(x: String) = interfaces.find(x => x.name == x.name).get
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

trait PObject{
  val name: String
  val couplings: L[Coupling] = L[Coupling]()
  def coupling(x: String) = couplings.find(c => c.x == x).get
}

case class Actuator(
  name: String,
  coupling : Coupling = Coupling("out"),
  interface : Interface = Interface("in")
)

case class Sensor(
  name: String,
  coupling : Coupling = Coupling("in"),
  interface : Interface = Interface("out")
)

case class Substrate(interfaces: L[Interface] = L[Interface]())
{
  def interface(x: String) = interfaces.find(i => i.name == x).get
}

case class Experiment(name: String,
  computers:  L[Computer]   = L[Computer](),
  objects:    L[PObject]    = L[PObject](),
  sensors:    L[Sensor]     = L[Sensor](),
  actuators:  L[Actuator]   = L[Actuator](),
  clinks:     L[CLink]      = L[CLink](),
  plinks:     L[PLink]      = L[PLink](),
  substrates: L[Substrate]  = L[Substrate](),
  views: L[ExperimentView] = L()
){
  def sensor(x: String) = sensors.find(s => s.name == x).get
  def actuator(x: String) = actuators.find(a => a.name == x).get
  def plink(a: Coupling, b: Coupling){ plinks += PLink(a, b) }
  def clink(a: Interface, b: Interface){ clinks += CLink(a, b) }
}

trait Cxy {
  var x: Double
  var y: Double
}

case class Coord(var x: Double, var y: Double) extends Cxy

case class Extent(var x: Double, var y: Double) extends Cxy

case class ExperimentView(name: String)
{
  val computers:  Map[Computer,  Cxy] = Map()
  val objects:    Map[PObject,   Cxy] = Map()
  val sensors:    Map[Sensor,    Cxy] = Map()
  val actuators:  Map[Actuator,  Cxy] = Map()
  val substrates: Map[Substrate, Cxy] = Map()

  def all[T <: Cxy] = List(computers, objects)
  def extent : Extent = {
    val x =
      all.map(l => l.values.minBy(a => a.x).x).min -
      all.map(l => l.values.maxBy(a => a.x).x).max

    val y =
      all.map(l => l.values.minBy(a => a.y).y).min -
      all.map(l => l.values.maxBy(a => a.y).y).max

    Extent(x, y)
  }
}

case class NewExp(name: String)

case class UserExp(name: String)

case class CodeEval(source: String, exp: String)
