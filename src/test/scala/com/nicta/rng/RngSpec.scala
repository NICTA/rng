package com.nicta.rng

import scalaz._, Scalaz._, effect._
import scalaz.scalacheck.ScalazProperties._

object RngSpec extends test.Spec {
  "Rng" should {
    "satisfy monad laws" ! monad.laws[Rng]
  }

  implicit def RngIntEqual: Equal[Rng[Int]] =
    Equal.equal((a, b) => {
      def run[A](x: Rng[A]): IO[A] =
        (Rng.setseed(1) >> x).run
      val (aa, bb) = (for { aa <- run(a); bb <- run(b) } yield (aa, bb)).unsafePerformIO
      aa === bb
    })
}
