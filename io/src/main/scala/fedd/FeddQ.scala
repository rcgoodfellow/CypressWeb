package cypress.io.fedd

import cypress.model.Experiment
import net.deterlab.abac.{Identity, Context, Role, Credential}
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import generated.{ StatusType, Failed}
import scalaxb.{DataRecord, Soap11Fault, Base64Binary}
import java.io.File

object DataFormatHelpers {
  
  import generated._
  
  implicit def extractId[T](ids: Seq[IDType], key: String) : T = {
    ids.find { x => 
      x.idtypeoption.key == Some(key) 
    }.get.idtypeoption.value.asInstanceOf[T]
  }
  
  implicit def localname(ids: Seq[IDType]): String = {
    extractId[String](ids, "localname")
  }
  
  implicit def localname(name: String): IDType = {
    IDType(DataRecord(None, Some("localname"), name))
  }
  
  implicit def fedid(ids: Seq[IDType]): String = {
    val bits = extractId[Base64Binary](ids, "fedid")
    bits.vector.map { x => "%02x".format(x) } mkString
  }
  
  def showExpInfo(irt: InfoResponseType): String = {
    localname(irt.experimentID)+": "+fedid(irt.experimentID)+" - "+
    irt.experimentStatus
  }
  
  def showExpInfo(nrt: NewResponseType): String = {
    localname(nrt.experimentID)+": "+fedid(nrt.experimentID)+" - "+
    nrt.experimentStatus
  }
   
  def showExpInfo(trt: TerminateResponseType): String = {
    var s = ""
    if(trt.experiment.idtypeoption.key.toString == "fedid") {
      s = s + fedid(Seq(trt.experiment))
    }
    else {
      s = s + trt.experiment.idtypeoption.value
    }
    s + " - terminated"
  }

  def showExpInfo(crt: CreateResponseType): String = {
   localname(crt.experimentID)+": "+fedid(crt.experimentID)+" - "+
   crt.experimentStatus
  }
  
  def showSoapFault(sf: Soap11Fault[FaultType]) = {
      var msg = "unkown failure"
      sf.detail.foreach(d => {
        msg = f"${d.desc}%s (${d.code}%d)"
      })
      msg
  }
}

object SecurityHelpers {
  lazy val userFedid : Identity = {
    new Identity(new File(System.getProperty("user.home") + "/.ssl/fedid.pem"))
  } 
  
  def delegate(fromKey: String, toKey: String, issuer: Identity): Credential = {
    val cred = 
      new Context().newCredential(
          new Role(fromKey + ".acting_for"), 
          new Role(toKey))
    cred.make_cert(issuer)
    cred
  }
  
  def getEncoded(cred: Credential): Array[Byte] = {
    val out = new java.io.ByteArrayOutputStream()
    cred.write(out)
    out.toByteArray
  }
}

case class DeterExpInfo(name: String, fedid: String, 
    var status: StatusType, var info: Seq[String]) {
  
  override def toString: String = f"$name: <$fedid> - $status"
  
  def dumpString: String = toString + "\n" + logString
  
  def logString = {
    if(info.isEmpty) ""
    else {
      "--- Log -------------------------------------\n" +
    info.mkString("\n")
    }
  }
  
  def fatal(msg: String) = {
    log(msg)
    status = Failed
    this
  }
  
  def log(x: String) = {
    info :+= x
    this
  }
}


object FeddQ {
  
  import generated._
  import DataFormatHelpers._
  
  System.setProperty(
      "javax.net.ssl.keyStore", 
      System.getProperty("user.home") + "/.ssl/deterlab.keystore"
  )
      
  System.setProperty(
      "javax.net.ssl.keyStorePassword", 
      "importkey"
  )

  private val svc = 
    new FeddBindings with
        scalaxb.Soap11ClientsAsync with
        scalaxb.DispatchHttpClientsAsync {}
        .service
  
  def multiStatus = {
    val req = MultiInfoRequestType()
    svc.multiInfo(req)
  }
        
  def `new`(name: String) = {
    val req = NewRequestType(experimentID = Some(localname(name)))
    svc.`new`(req).map(x => {
      DeterExpInfo(
        name = localname(x.experimentID),
        fedid = fedid(x.experimentID),
        status = x.experimentStatus,
        info = Seq()
      )
    })
  }
  
  def terminate(name: String, force: Boolean = false, log :Seq[String] = Seq()) = {
    val req = TerminateRequestType(localname(name), force=Some(force))
    svc.terminate(req).map(x => {
      DeterExpInfo(
        name = x.experiment.idtypeoption.value.toString,
        fedid = "",
        status = Terminating,
        info = log ++ Seq(x.deallocationLog.getOrElse(""))
      )
    }).recover({
      case ex: Soap11Fault[FaultType] =>
          DeterExpInfo(
          name = name,
          fedid = "",
          status = Failed,
          info = log ++ Seq("terminate failed", showSoapFault(ex))
        )
    })
  }
  
  def create(model: Experiment) = {
    
    import SecurityHelpers._
    
    var log = Seq(f"creating ${model.name}")
    
    `new`(model.name).flatMap{exp =>
      
      log :+= f"get new experiment: ${exp.toString}"
      
      val topdl = TopDLGen(model)
      val xpd = ExperimentDescriptionType(DataRecord(None, Some("topdldescription"), topdl))
      
      val proj = 
        CreateServiceInfoType(
          name="project_export",
          fedAttr = Seq(FedAttrType("project", "cypress")),
          export = Seq("deter"))
      
      log :+= f"user fedid: ${userFedid.getKeyID}"
      log :+= f"exp fedid: ${exp.fedid}"
      
      val delegation = delegate(userFedid.getKeyID, exp.fedid, userFedid)
      
      val cred = Seq(
        Base64Binary(userFedid.getCertificate.getEncoded: _*),
        Base64Binary(getEncoded(delegation): _*)
      )
          
      val req = 
        CreateRequestType(
          testbedmap = Nil,
          experimentdescription = xpd,
          service = Seq(proj),
          experimentID = localname(model.name),
          credential = cred
          )
          
      svc.create(req)
        .map(x => {
          DeterExpInfo(
                name = localname(x.experimentID),
                fedid = fedid(x.experimentID),
                status = x.experimentStatus,
                info = log
          )
        })
        .recover({ case ex: Soap11Fault[FaultType] =>
            log :+= "experiment creation failed"
            log :+= showSoapFault(ex)
            DeterExpInfo(
                name = model.name,
                fedid = "",
                status = Failed,
                info = log
            )
          })
        .flatMap(y => y.status match {
          case Failed =>
            log :+= "terminating due to creation failure..."
            terminate(model.name, log=log)
          case _ =>
            Future(y)
        })
    }
  
  }
}

object IFeddQ {
  import DataFormatHelpers._
  
  val D = Duration(47, SECONDS)
  
  def multiStatus() {
    Await.result(FeddQ.multiStatus, D)
      .info.foreach { x => println(showExpInfo(x)) }
  }
  
  def terminate(x: String, force: Boolean = false) {
    println(Await.result(FeddQ.terminate(x, force), D))
  }
  
  def `new`(x:String) {
    println(Await.result(FeddQ.`new`(x), D))
  }
  
  def create(x: Experiment) {
    println(Await.result(FeddQ.create(x), D).toString)
  }
}
