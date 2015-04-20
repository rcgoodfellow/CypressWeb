package fedd

import org.scalatest.FlatSpec
import net.deterlab.cypress.io.fedd.FeddQ
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import org.scalatest.concurrent.ScalaFutures
import net.deterlab.cypress.io.fedd.DataFormatHelpers._
import org.scalatest.time.Span
import org.scalatest.time.Seconds
import generated._
import net.deterlab.cypress.model.{Experiment, Computer}
import scalaxb.Soap11Fault
import scalaxb.Soap11Fault

trait FeddIOTest extends ScalaFutures {
  implicit override val patienceConfig =
    PatienceConfig(timeout=Span(47, Seconds))
}

class NewDeleteSpec extends FlatSpec with FeddIOTest {
  
  "Multistatus" should "list experiment info" in {
    whenReady(FeddQ.multiStatus){ 
      _.info.map(showExpInfo).foreach(println)
    }
  }
  
  "New" should "create empty experiment called abby" in {
    whenReady(FeddQ.`new`("abby")){ x => println(x.dumpString) }
  }
  
  "Terminate" should "remove the empty experiment called abby" in {
    whenReady(FeddQ.terminate("abby")){
      x => println(x.dumpString)
    }
  }
}

class CreateSpec extends FlatSpec with FeddIOTest {
  
  val exp = Experiment(name="abby")
  exp.computers += Computer("murphy")
  
  "Create" should "start an experiment called abby" in {
    
    val f = FeddQ.create(exp)
    var tex: Option[Soap11Fault[FaultType]] = None
    
    whenReady(f) {
      x => x.status match {
        case Starting => println(x.dumpString)
        case _ => fail(x.dumpString)
      }
    }
   
  }
  
}

