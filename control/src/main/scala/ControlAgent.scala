package cypress.control

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import akka.actor.{ActorRef, Actor}
import akka.io.{Udp, IO}
import akka.util.{ByteStringBuilder, ByteString}

import scala.pickling.binary.BinaryPickle

/**
 * The Cypress Project
 * Created by ry on 4/21/15.
 */

sealed trait Pub extends {
  type HashCode = Int
  val topic: HashCode
  val typecode: HashCode
}

sealed trait ShapeShifter[A<:Pub]{
  def shs(x: ByteString) : A
  def shs(x: A) : ByteString
}

case class DoublePub(topic: Int, x: Double) extends Pub {
  val typecode = DoublePub.typeid
}
object DoublePub { val typeid = "DoublePub".hashCode }

case class IntPub(topic: Int, x: Int) extends Pub {
  val typecode = DoublePub.typeid
}
object IntPub { val typeid = "IntPub".hashCode }

object ShapeShifters {

  import scala.pickling.Defaults._, scala.pickling.binary._

  object DblPubSS extends ShapeShifter[DoublePub]{
    def shs(x: ByteString): DoublePub = {
      BinaryPickle(x.toArray).unpickle[DoublePub]
    }
    def shs(x: DoublePub): ByteString = {
      ByteString(x.pickle.value)
    }
  }

  val a = DblPubSS

  object IntPubSS extends ShapeShifter[IntPub]{
    def shs(x: ByteString): IntPub = {
      BinaryPickle(x.toArray).unpickle[IntPub]
    }
    def shs(x: IntPub): ByteString = {
      ByteString(x.pickle.value)
    }
  }

}

trait BaseShapes {
  type HashCode = Int
  protected var shapes = Map(
    DoublePub.typeid -> ShapeShifters.DblPubSS,
    IntPub.typeid -> ShapeShifters.IntPubSS
  )

}

object util {
  def p(id: String): Int = id.hashCode
}

trait ControlAgent {
  def rx : PartialFunction[Pub,Unit]
}

abstract class ControlNode extends Actor with ControlAgent with BaseShapes {
  import context.system
  private var sock: Option[ActorRef] = None
  val multicast = new InetSocketAddress("multicast", 4074)

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", 0))

  def receive = {
    case Udp.Bound(local) =>
      sock = Some(sender())
      context.become(ready(sender())) }

  def ready(sock: ActorRef) : Receive = {

    case Udp.Received(data, remote) =>
      val id_tag = ByteBuffer.wrap(data.toArray.slice(0,4)).getInt
      val ss = shapes(id_tag)
      rx(ss.shs(data))

    case Udp.Unbind => sock ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

}
