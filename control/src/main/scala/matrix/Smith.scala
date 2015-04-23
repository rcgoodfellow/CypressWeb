package matrix.smith

/**
 * The Cypress Project
 * Created by ry on 4/22/15.
 */

import akka.actor.{Props, ActorSystem}
import cypress.control.{DoublePubTx, Tx, DoublePub, ControlAgent}
import scala.util.Random
import matrix.Tag
import matrix.Printers._

class Smith extends ControlAgent {
  def rx = {
    case dp @ DoublePub(sender, Tag.spicy, x) => println("S;; "+showdp(dp))
    case dp @ DoublePub(sender, Tag.taco, x) => println("T;; "+showdp(dp))
    case dp @ DoublePub(sender, Tag.crunch, x) => println("C;; "+showdp(dp))
    case tp @ DoublePubTx(p) =>
      println("TX:::>>> " + showdp(p))
      tx(p)
  }
}

object Main extends App {

  val sys = ActorSystem("Matrix")
  val agentSmith = sys.actorOf(Props[Smith], name="Smith")
  println("Running the Smith ...")
  while(true) {
    Thread.sleep(100+Random.nextInt(2100))
    agentSmith ! DoublePubTx(DoublePub("Smith".hashCode, Tag.pool(Random.nextInt(3)), Random.nextDouble()))
  }
}
