package com.nicta.rng
package test

import scalaz._, Scalaz._
import scalaz.scalacheck.ScalazArbitrary._
import org.scalacheck.{Gen, Arbitrary}, Arbitrary.arbitrary, Gen.{frequency, oneOf}

trait RngArbitraries {

  implicit def RngIntArbitrary: Arbitrary[Rng[Int]] =
    Arbitrary(Gen.parameterized(params => Gen.const(Rng.setseed(params.rng.nextInt) >> Rng.int)))

  implicit def RngIntFunctionArbitrary: Arbitrary[Rng[Int => Int]] =
    Arbitrary(Gen.parameterized(params => Gen.const(Rng.int.function[Int, Int](CoRng(_ * _)))))

}

object RngArbitrary extends RngArbitraries
