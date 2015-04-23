package matrix.jones

/**
 * The Cypress Project
 * Created by ry on 4/22/15.
 */

import akka.actor.{Props, ActorSystem}
import cypress.control.{DoublePubTx, Tx, DoublePub, ControlAgent}
import scala.util.Random
import matrix.Tag
import matrix.Printers._

class Jones extends ControlAgent {
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
  val agentJones = sys.actorOf(Props[Jones], name="Jones")
  println("Running the Jones...")
  while(true) {
    Thread.sleep(700+Random.nextInt(4700))
    agentJones ! DoublePubTx(DoublePub("Jones".hashCode, Tag.pool(Random.nextInt(3)), Random.nextDouble()))
  }
}
