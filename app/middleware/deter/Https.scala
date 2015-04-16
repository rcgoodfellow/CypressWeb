package middleware.deter

/**
 * The Cypress Project
 * Created by ry on 4/15/15.
 */

import java.io.FileInputStream
import java.security.{SecureRandom, KeyStore}
import java.security.cert.X509Certificate
import javax.net.ssl.{TrustManagerFactory, X509TrustManager, TrustManager, KeyManagerFactory}

import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import dispatch.Http
import generated.{IDType, NewRequestType, MultiInfoRequestType}
import models.Experiment
import scala.concurrent.Await
import scala.concurrent.duration._
import scalaxb.{DataRecord, ElemName, HttpClientsAsync}

class X509Http extends Http {
  val ks = KeyStore.getInstance("JKS")
  val fis = new FileInputStream("/Users/ry/.ssl/cypress")
  ks.load(fis, "muffins".toCharArray)
  val kmf = KeyManagerFactory.getInstance("SunX509")
  kmf.init(ks, "muffins".toCharArray)


  val DUMMY_TRUST_MANAGER = new X509TrustManager {
    override def getAcceptedIssuers: Array[X509Certificate] = Array()

    override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

    override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
  }

  val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
  tmf.init(ks)

  val km = kmf.getKeyManagers
  val tm = Array[TrustManager](DUMMY_TRUST_MANAGER)
  val sr = new SecureRandom

  override val client = new AsyncHttpClient(
    new AsyncHttpClientConfig.Builder()
      .setSSLContext({
        val ctx = javax.net.ssl.SSLContext.getInstance("TLS")
        ctx.init(km, tm, sr)
        ctx})
      .build
  )
}

trait X509DispatchHttpClientsAsync extends HttpClientsAsync {
  lazy val httpClient = new DispatchHttpClient {}

  trait DispatchHttpClient extends HttpClient {
    import dispatch._, Defaults._

    lazy val http = new X509Http()

    def request(in: String, address: java.net.URI, headers: Map[String, String]): concurrent.Future[String] = {
      val req = url(address.toString).setBodyEncoding("UTF-8") <:< headers << in
      http(req > as.String)
    }
  }
}


//noinspection LanguageFeature

object Helpers {
  import generated._

  def localname(name: String) = IDType(DataRecord(None, Some("localname"), name))
}

object FeddQ {

  import generated._

  System.setProperty("javax.net.ssl.keyStore", "/Users/ry/keystore.ImportKey")
  System.setProperty("javax.net.ssl.keyStorePassword", "importkey")

  val client =
    new generated.FeddBindings with
        scalaxb.Soap11ClientsAsync with
        scalaxb.DispatchHttpClientsAsync {}

  val svc = client.service

  def multiStatus() {

    val req = MultiInfoRequestType()
    val res = Await.result(svc.multiInfo(req), 15 seconds)
    res.info foreach {
      ix => println(
        ix.experimentID.last.idtypeoption.value + ": "  +
        ix.experimentStatus.toString
      )
    }
  }

  def `new`(name: String) {

    val expAccess = None
    val expName = Some(Helpers.localname(name))

    val req = NewRequestType(expAccess, expName)
    val res = Await.result(svc.`new`(req), 15 seconds)

    println(res.experimentStatus)

  }

  def terminate(name: String, force: Option[Boolean] = Some(false)) {

    val expName = Helpers.localname(name)

    val req = TerminateRequestType(expName, force)
    val res = Await.result(svc.terminate(req), 15 seconds)

    res.deallocationLog.foreach(x => println(x))

  }

  def create(exp: Experiment) {

    val tdl = TopDLGen(exp)

    val xpd =
      ExperimentDescriptionType(
        DataRecord(None, Some("topdldescription"), tdl))

    val req = CreateRequestType(
      testbedmap = Nil,
      experimentdescription = xpd,
      service = Nil,
      Helpers.localname(exp.name),
      credential = Nil)

    val res = Await.result(svc.create(req), 15 seconds)

    println(res.experimentStatus)
  }

}

object TopDLGen {
  import generated._

  def apply(exp: Experiment) : TopologyType = {
    ???
  }

}
