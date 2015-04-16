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
import generated.MultiInfoRequestType
import scala.concurrent.Await
import scala.concurrent.duration._
import scalaxb.HttpClientsAsync

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


object FeddQ {

  System.setProperty("javax.net.ssl.keyStore", "/Users/ry/keystore.ImportKey")
  System.setProperty("javax.net.ssl.keyStorePassword", "importkey")

  val client =
    new generated.FeddBindings with
        scalaxb.Soap11ClientsAsync with
        scalaxb.DispatchHttpClientsAsync {}

  val svc = client.service

  def multiStatus() {
    println("multistatus --> " + client.baseAddress)

    val req = MultiInfoRequestType()

    val defaultScope = scalaxb.toScope(Some("tns") -> "http://www.isi.edu/fedd_types",
      Some("tns0") -> "http://www.isi.edu/fedd.wsdl",
      Some("topdl") -> "http://www.isi.edu/topdl",
      Some("xs") -> "http://www.w3.org/2001/XMLSchema",
      Some("xsi") -> "http://www.w3.org/2001/XMLSchema-instance")

    val xml = scalaxb.toXML(req, Some("http://www.isi.edu/fedd.wsdl"), "MultiInfoRequestBody", defaultScope)
    println(xml)

    val fresponse = svc.multiInfo(MultiInfoRequestType())
    val response = Await.result(fresponse, 15 seconds)
    response.info foreach { ix =>
      println(ix.experimentID.last.toString + ix.experimentStatus.toString)
    }
  }
}