package com.nicta
package rng

import scalaz._, Scalaz._, Free._

sealed trait RngResume[+A] {
  def map[B](f: A => B): RngResume[B] =
    this match {
      case RngCont(x) =>
        RngCont(x map (_ map f))
      case RngTerm(x) =>
        RngTerm(f(x))
    }

  def free: Rng[A] =
    Rng(this match {
      case RngCont(x) =>
        Suspend(x map (_.free))
      case RngTerm(x) =>
        Return(x)
    })

  def term: Option[A] =
    this match {
      case RngCont(_) =>
        None
      case RngTerm(x) =>
        Some(x)
    }

  def cont: Option[RngOp[Rng[A]]] =
    this match {
      case RngCont(x) =>
        Some(x)
      case RngTerm(x) =>
        None
    }

}
case class RngCont[+A](x: RngOp[Rng[A]]) extends RngResume[A]
case class RngTerm[+A](x: A) extends RngResume[A]

object RngResume {
  implicit val RngResumeFunctor: Functor[RngResume] =
    new Functor[RngResume] {
      def map[A, B](fa: RngResume[A])(f: A => B) =
        fa map f
    }

  def distribute[F[_], B](a: RngResume[F[B]])(implicit D: Distributive[F]): F[RngResume[B]] =
    D.cosequence(a)

  def distributeR[A, B](a: RngResume[A => B]): A => RngResume[B] =
    distribute[({type f[x] = A => x})#f, B](a)

  def distributeRK[A, B](a: RngResume[A => B]): Kleisli[RngResume, A, B] =
    Kleisli(distributeR(a))

  def distributeK[F[+_]: Distributive, A, B](a: RngResume[Kleisli[F, A, B]]): Kleisli[F, A, RngResume[B]] =
    distribute[({type f[x] = Kleisli[F, A, x]})#f, B](a)

}

