package com.nicta
package rng

import scalaz._

trait CoRng[A] {
  val run: (Int, A) => A

  def apply(n: Int, a: A): A =
    run(n, a)

  def co(n: Int): Endo[A] =
    Endo(run(n, _))

  def xmap[B](f: A => B, g: B => A): CoRng[B] =
    CoRng((n, b) => f(run(n, g(b))))

  def bumpint(f: Int => Int): CoRng[A] =
    CoRng((n, a) => run(f(n), a))

  def bumpvalue(f: A => A): CoRng[A] =
    CoRng((n, a) => run(n, f(a)))

  def bumpvalueE(f: Endo[A]): CoRng[A] =
    bumpvalue(f.run)

  def zip[B](x: CoRng[B]): CoRng[(A, B)] =
    CoRng((n, ab) => (run(n, ab._1), x.run(n, ab._2)))

  def either[B](x: CoRng[B]): CoRng[A \/ B] =
    CoRng((n, e) => e.bimap(run(n, _), x.run(n, _)))
}

object CoRng {
  def apply[A](f: (Int, A) => A): CoRng[A] =
    new CoRng[A] {
      val run = f
    }

  def constant[A](a: => A): CoRng[A] =
    apply((_, _) => a)

  def id[A]: CoRng[A] =
    CoRng((_, a) => a)

  def endo[A](f: A => A): CoRng[A] =
    CoRng((_, a) => f(a))

  def endoE[A](e: Endo[A]): CoRng[A] =
    endo(e.run)

  def fromint[A](f: Int => A): CoRng[A] =
    apply((n, _) => f(n))
}
