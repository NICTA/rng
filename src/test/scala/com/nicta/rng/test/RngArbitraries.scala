package com.nicta.rng
package test

import scalaz.Show, scalaz.syntax.show._
import scalaz.scalacheck.ScalazArbitrary._
import org.scalacheck.{Pretty, Gen, Arbitrary}, Arbitrary.arbitrary, Gen.{frequency, oneOf}

trait RngArbitraries {
  implicit def ShowPretty[A: Show](a: A): Pretty =
    Pretty(_ => a.shows)

  implicit def RngIntArbitrary: Arbitrary[Rng[Int]] =
    Arbitrary(Gen(params => Some(Rng.int)))

  implicit def RngIntFunctionArbitrary: Arbitrary[Rng[Int => Int]] =
    Arbitrary(Gen(params => Some(Rng.int.function[Int, Int](CoRng(_ * _)))))

}

object RngArbitrary extends RngArbitraries
