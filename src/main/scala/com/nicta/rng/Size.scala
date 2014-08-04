package com.nicta
package rng

import scalaz._, Scalaz._

sealed trait Size {
  val value: Option[Int]

  import Size._

  def orZero: Int =
    get(0)

  def get(n: => Int): Int =
    value getOrElse n

  def has: Boolean =
    value.isDefined

  def |(s: => Size): Size =
    if (value.isDefined)
      this
    else
      s

  def withsize(k: Int => Int): Size =
    size(value map k)

  def +(n: => Int): Size =
    withsize(_ + n)

  def *(n: => Int): Size =
    withsize(_ * n)

  def inc: Size =
    this + 1

  def dec: Size =
    this + (-1)

  def forall(p: Int => Boolean): Boolean =
    value forall p

  def exists(p: Int => Boolean): Boolean =
    value exists p

}

object Size {
  private[rng] def size(d: Option[Int]): Size =
    new Size {
      val value = d
    }

  def apply(n: Int): Size =
    size(Some(n))

  def nosize: Size =
    size(None)

  implicit def IntSize(n: Int): Size =
    Size(n)

  implicit val SizeMonoid: Monoid[Size] =
    new Monoid[Size] {
      def append(s1: Size, s2: => Size) =
        s1 | s2
      def zero =
        nosize
    }

  implicit val SizeOrder: Order[Size] =
    implicitly[Order[Option[Int]]].contramap(_.value)
}
