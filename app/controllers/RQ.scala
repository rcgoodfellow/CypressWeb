package controllers

import play.api.mvc.Result
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context


/**
 * The Cypress Project
 * Created by ry on 4/10/15.
 */

object RQM {
  def dumpClass[A]: Int = macro dumpClass_impl[A]

  def dumpClass_impl[A: cx.WeakTypeTag](cx: Context) : cx.universe.Tree = {
    import cx.universe._
    //import Flag._

    //val hooligan = cx.reifyType(internal.gen.mkRuntimeUniverseRef, EmptyTree, cx.weakTypeOf[A])

    //val x = cx.Expr[A](hooligan).tree

    //val t = cx.weakTypeOf[A].

    //println("TREE: ")
    println(cx.weakTypeOf[A].toString)

    cx.Expr[Int](Literal(Constant(47))).tree
  }
}

abstract class RQ[A] {
  val state: Option[A]
  val result: Option[Result]

  def flatMap[B,T>:RQ[B]](f: A => T) : T = {
    result match {
      case Some(r) => Dead(r)
      case None => f(state.get)
    }
  }
}

case class Working[A](seed: A) extends RQ[A] {
  override val result = None
  override val state = Some(seed)
}

case class Dead[A](r :Result) extends RQ[A] {
  override val result = Some(r)
  override val state = None
}

