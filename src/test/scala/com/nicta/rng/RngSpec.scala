package com.nicta.rng

import scalaz._, Scalaz._, effect._
import scalaz.scalacheck.ScalazProperties._
import org.specs2.matcher._

object RngSpec extends test.Spec {
  "Rng" should {
    "satisfy monad laws" ! monad.laws[Rng]
  }

  "chooseint must return a value between low and high" >> prop { (low: Int, high: Int) =>
    Rng.chooseint(low, high) must beBetween(low, high)
  }

  "choosedouble must return a value between low and high" >> prop { (low: Double, high: Double) =>
    Rng.choosedouble(low, high) must beBetween(low, high)
  }

  "choosefloat must return a value between low and high" >> prop { (low: Float, high: Float) =>
    Rng.choosefloat(low, high) must beBetween(low, high)
  }

  def beBetween[T : Numeric](low: T, high: T): Matcher[Rng[T]] = { generator: Rng[T] =>
    val (l, h) = if (low <= high) (low, high) else (high, low)
    generator.run.unsafePerformIO must beBetween(l, h)
  }

  implicit def RngIntEqual: Equal[Rng[Int]] =
    Equal.equal((a, b) => {
      def run[A](x: Rng[A]): IO[A] =
        (Rng.setseed(1) >> x).run
      val (aa, bb) = (for { aa <- run(a); bb <- run(b) } yield (aa, bb)).unsafePerformIO
      aa === bb
    })
}
