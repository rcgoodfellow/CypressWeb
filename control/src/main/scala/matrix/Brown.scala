package matrix.brown

/**
 * The Cypress Project
 * Created by ry on 4/22/15.
 */

import akka.actor.{Props, ActorSystem}
import cypress.control.{DoublePubTx, Tx, DoublePub, ControlAgent}
import scala.util.Random
import matrix.Tag
import matrix.Printers._

class Brown extends ControlAgent {
  def rx = {
    case dp @ DoublePub(sender, Tag.spicy, x) => println(showdp(dp))
    case dp @ DoublePub(sender, Tag.taco, x) => println(showdp(dp))
    case dp @ DoublePub(sender, Tag.crunch, x) => println(showdp(dp))
    case tp @ DoublePubTx(p) =>
      println(showdp(p))
      tx(p)
  }
}

object Main extends App {
  val sys = ActorSystem("Matrix")
  val agentBrown = sys.actorOf(Props[Brown], name="Brown")
  println("Running the Brown ...")
  while(true) {
    Thread.sleep(300+Random.nextInt(3600))
    agentBrown ! DoublePubTx(DoublePub("Brown".hashCode, Tag.pool(Random.nextInt(3)), Random.nextDouble()))
  }
}
