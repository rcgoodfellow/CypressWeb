package controllers

/**
 * The Cypress Project
 * Created by ry on 4/14/15.
 */

import akka.actor._
import play.api.libs.json.{Json, JsObject}

class BoomSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: JsObject => {
      BoomSocketActor.shakalaka(out)
    }
  }
}

object BoomSocketActor {
  def props(out: ActorRef) = Props(new BoomSocketActor(out))
  def shakalaka(out: ActorRef): Unit = {
    out ! Json.obj("response" -> "shaka laka laka")
  }
}
