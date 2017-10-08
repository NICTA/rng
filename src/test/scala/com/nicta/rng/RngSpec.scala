package com.nicta.rng

import scalaz._, Scalaz._, effect._
import scalaz.scalacheck.ScalazProperties._
import org.specs2.matcher._

object RngSpec extends test.Spec {

  "Rng" should {
    "satisfy monad laws" ! monad.laws[Rng]
  }

  "chooseint must return a value between low and high" >> prop { (low: Int, high: Int) =>
    Rng.chooseint(low, high) must beBoundedBy(low, high)
  }

  "chooseint distribution must be more or less uniform" >> {
    Rng.chooseint(0, 99) must beUniform
  }

  "choosedouble must return a value between low and high" >> prop { (low: Double, high: Double) =>
    Rng.choosedouble(low, high) must beBoundedBy(low, high)
  }

  "boolean must be more or less uniform" >> {
    Rng.boolean must beUniformBool
  }

  "choosedouble distribution must be more or less uniform" >> {
    Rng.choosedouble(0, 99) must beUniform
  }

  "choosefloat must return a value between low and high" >> prop { (low: Float, high: Float) =>
    Rng.choosefloat(low, high) must beBoundedBy(low, high)
  }

  "choosefloat distribution must be more or less uniform" >> {
    Rng.choosefloat(0, 99) must beUniform
  }

  "oneofL distribution must be more or less uniform" >> {
    Rng.oneofL(NonEmptyList.nel(0, IList(1.to(99):_*))) must beUniform
  }

  "oneof distribution must be more or less uniform" >> {
    Rng.oneof(0, 1.to(99): _*) must beUniform
  }

  "oneofV distribution must be more or less uniform" >> {
    Rng.oneofV(OneAnd(0, 1.to(99).toVector)) must beUniform
  }

  def beUniformBool: Matcher[Rng[Boolean]] = { generator: Rng[Boolean] =>
    val frequencies = generator.fill(100000).run.unsafePerformIO
      .groupBy(t => t).toList
      .map(x => (x._1, x._2.length))
      .sortBy(_._1).map(_._2)

    frequencies must contain(beBetween(49800, 50200)).forall
  }

  def beUniform[T : Numeric]: Matcher[Rng[T]] = { generator: Rng[T] =>
    val n = implicitly[Numeric[T]]
    val frequencies = generator.fill(100000).run.unsafePerformIO
      .groupBy(t => n.toInt(t) / 10).toList
      .map(x => (x._1, x._2.length))
      .sortBy(_._1).map(_._2)

    frequencies must contain(beBetween(9000, 11000)).forall
  }

  def beBoundedBy[T : Numeric](low: T, high: T): Matcher[Rng[T]] = { generator: Rng[T] =>
    val (l, h) = if (implicitly[Numeric[T]].lt(low, high)) (low, high) else (high, low)
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
