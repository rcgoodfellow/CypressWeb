package cypress.control

import java.net._
import java.nio.channels.DatagramChannel
import akka.actor.{ActorRef, Actor}
import akka.io.Inet.{DatagramChannelCreator, SocketOption}
import akka.io.{Udp, IO}
import akka.util.{ByteString}
import scala.pickling.{Unpickler, Pickler}


/**
 * The Cypress Project
 * Created by ry on 4/21/15.
 */

sealed trait Pub extends {
  type HashCode = Int
  val topic: HashCode
  val sender: HashCode
}

trait Tx[A<:Pub]{
  val x: A
}

final case class MulticastGroup(address: String, iface: String) extends SocketOption {
  override def afterConnect(c: DatagramChannel): Unit = {
    val group = InetAddress.getByName(address)
    val networkInterface = NetworkInterface.getByName(iface)
    c.setOption[java.lang.Boolean](StandardSocketOptions.IP_MULTICAST_LOOP, true)
    c.join(group, networkInterface)
  }
}

final case class Inet6ProtocolFamily() extends DatagramChannelCreator {
  override def create() = {
    val dc = DatagramChannel.open(StandardProtocolFamily.INET6)
    dc.setOption[java.lang.Boolean](StandardSocketOptions.IP_MULTICAST_LOOP, true)
    dc
  }
}

case class DoublePub(sender: Int, topic: Int, x: Double) extends Pub
case class DoublePubTx(x: DoublePub) extends Tx[DoublePub]

case class IntPub(sender: Int, topic: Int, x: Int) extends Pub
case class IntPubTx(x: DoublePub) extends Tx[DoublePub]

class ShapeShifter[+A<:Pub]{

  import scala.pickling.Defaults._, scala.pickling.binary._

  def fromBytes[B >: A](x: ByteString)(implicit pickler: Unpickler[B]) : B = {
    BinaryPickle(x.toArray).unpickle[B]
  }
  def toBytes[B >: A](x: B)(implicit pickler: Pickler[B]) : ByteString = {
    ByteString(x.pickle.value)
  }

}

object BasePicklers {
  import scala.pickling.Defaults._, scala.pickling.binary._
  implicit val pk = Pickler.generate[Pub]
  implicit val ipk = Pickler.generate[IntPub]
  implicit val dpk = Pickler.generate[DoublePub]
  implicit val upk = Unpickler.generate[Pub]
  implicit val uipk = Unpickler.generate[IntPub]
  implicit val udpk = Unpickler.generate[DoublePub]
}

object util {
  def p(id: String): Int = id.hashCode
}


abstract class ControlAgent extends Actor {
  import context.system
  private var sock: Option[ActorRef] = None
  private var ssock: Option[ActorRef] = None
  val group = "ff02::C3A0:3EAC"
  val iface = "eth4"
  val multicast = new InetSocketAddress(s"$group%$iface", 4074)
  val myaddr = new InetSocketAddress("localhost", 7047)
  lazy val id = getClass.getName.hashCode
  def rx : PartialFunction[Any,Unit]

  import BasePicklers._

  val opts = List(
    MulticastGroup(group, iface),
    Inet6ProtocolFamily(),
    Udp.SO.ReuseAddress(on=true)
  )

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(4074), opts)
  IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))

  def receive = {
    case Udp.Bound(local) =>
      sock = Some(sender())

    case Udp.SimpleSenderReady =>
      ssock = Some(sender())

    case Udp.Received(data, remote) =>
      val ss = new ShapeShifter[Pub]
      val p = ss.fromBytes(data)
      rx(ss.fromBytes(data))

    case Udp.Unbind => sock.foreach(_ ! Udp.Unbind)
    case Udp.Unbound => context.stop(self)
    case x:Tx[Any] => rx(x)
  }


  def tx[A<:Pub](x: A) : Unit = {
    val ss = new ShapeShifter[Pub]
    val bits = ss.toBytes(x)
    ssock.foreach(_ ! Udp.Send(bits, multicast))
  }

}
