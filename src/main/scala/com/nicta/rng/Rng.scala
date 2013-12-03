package com.nicta
package rng
import Rng._

import scalaz._, Free._, Scalaz._, NonEmptyList._, Digit._, Leibniz._, Validation._, effect._

sealed trait Rng[+A] {
  val free: Free[RngOp, A]

  import Rng._

  def map[B](f: A => B): Rng[B] =
    Rng(free map f)

  def flatMap[B](f: A => Rng[B]): Rng[B] =
    Rng(free flatMap (f(_).free))

  def ap[X](f: Rng[A => X]): Rng[X] =
    for {
      ff <- f
      aa <- this
    } yield ff(aa)

  def zip[X](q: Rng[X]): Rng[(A, X)] =
    zipWith(q)(a => (a, _))

  def zipWith[B, C](r: Rng[B])(f: A => B => C): Rng[C] =
    r.ap(map(f))

  def foldRun[B, AA >: A](b: B)(f: (B, RngOp[Rng[AA]]) => (B, Rng[AA])): (B, AA) =
    free.foldRun[B, AA](b)((bb, t) => f(bb, t map (Rng(_))) :-> (_.free))

  def resume: RngResume[A] =
    free.resume match {
      case -\/(x) => RngCont(x map (Rng(_)))
      case \/-(x) => RngTerm(x)
    }

  def run: IO[A] =
    IO({
        class NextBitsRandom extends java.util.Random {
          def nextbits(bits: Int): Int =
            super.next(bits)
        }

        @annotation.tailrec
        def loop(g: Rng[A], r: NextBitsRandom): A =
          g.resume match {
            case RngCont(NextBits(b, n)) =>
              loop(n(r nextbits b), r)
            case RngCont(SetSeed(b, n)) =>
              loop({
                r setSeed b
                n()
              }, r)
            case RngTerm(a) =>
              a
          }

        loop(this, new NextBitsRandom)
    })

  def maph[G[+_]](f: RngOp ~> G)(implicit G: Functor[G]): Free[G, A] =
    free mapSuspension f

  def mapr(f: RngOp ~> RngOp): Rng[A] =
    Rng(free mapFirstSuspension f)

  def go[AA >: A](f: RngOp[Rng[AA]] => Rng[AA]): AA =
    free.go[AA](r => f(r map (Rng(_))).free)

  def |+|[AA >: A](x: Rng[AA])(implicit S: Semigroup[AA]): Rng[AA] =
    for {
      a <- this
      b <- x
    } yield S.append(a, b)

  def function[B, AA >: A](implicit q: CoRng[B]): Rng[B => AA] =
    Rng(resume match {
      case RngCont(x) =>
        Suspend(x map (d => d.function[B, AA].free map (e => b => e(x.storepos match {
          case None => b
          case Some(p) => q(p, b)
        }))))
      case RngTerm(x) =>
        Return(_ => x)
    })

  def fill(n: Int): Rng[List[A]] =
    sequence(List.fill(n)(this))

  def list(s: Size): Rng[List[A]] =
    for {
      n <- s.value match {
             case None => int
             case Some(y) => chooseint(0, y)
           }
      a <- fill(n)
    } yield a

  def list1(s: Size): Rng[NonEmptyList[A]] =
    for {
      z <- this
      n <- s.value match {
             case None => int
             case Some(y) => chooseint(0, y)
           }
      a <- fill(n)
    } yield nel(z, a)

  def vector(s: Size): Rng[Vector[A]] =
    list(s) map (Vector(_: _*))

  def stream[AA >: A](s: Size): Rng[EphemeralStream[AA]] =
    list(s) map (EphemeralStream(_: _*))

  def option: Rng[Option[A]] =
    boolean flatMap (p => sequence[Option, A](if(p) None else Some(this)))

  def ***[X](x: Rng[X]): Rng[(A, X)] =
    zip(x)

  def either[X](x: Rng[X]): Rng[A \/ X] =
    boolean flatMap (p => if(p) map(_.left) else x map (_.right))

  def \/[X](x: Rng[X]): Rng[A \/ X] =
    either(x)

  def validation[X](x: Rng[X]): Rng[A \?/ X] =
    boolean flatMap (p => if(p) map(_.fail) else x map (_.success))

  def \?/[X](x: Rng[X]): Rng[A \?/ X] =
    validation(x)

  def +++[X](x: Rng[X]): Rng[A \/ X] =
    either(x)

  def eitherS[X](x: Rng[X]): Rng[Either[A, X]] =
    either(x) map (_.toEither)

  def flatten[AA >: A, B](implicit f: AA === Rng[B]): Rng[B] =
    flatMap(f)

}

object Rng {
  private[rng] def apply[A](f: Free[RngOp, A]): Rng[A] =
    new Rng[A] {
      val free = f
    }

  def nextbits(n: Int): Rng[Int] =
    RngOp.nextbits(n, x => x).lift

  def setseed(s: Long): Rng[Unit] =
    RngOp.setseed(s, ()).lift

  def double: Rng[Double] =
    for {
      a <- nextbits(27)
      b <- nextbits(26)
    } yield (b.toLong << a) / (1.toLong << 53).toDouble

  /** @return a Double in the [0, 1[ interval */
  def unitdouble: Rng[Double] = for {
    i <- positivedouble
    j <- positivedoub le
  } yield
    if (i == 0 || j == 0 || i == j) 0.0
    else if (i < j)                 i / j
    else                            j / i

  /** @return a Float in the [0, 1[ interval */
  def unitfloat: Rng[Float] = for {
    i <- positivefloat
    j <- positivefloat
  } yield
    if (i == 0 || j == 0 || i == j) 0
    else if (i < j)                 i / j
    else                            j / i

  def float: Rng[Float] =
    nextbits(24) map (_ / (1 << 24).toFloat)

  def long: Rng[Long] =
    for {
      a <- nextbits(32)
      b <- nextbits(32)
    } yield (a.toLong << 32) + b

  def int: Rng[Int] =
    nextbits(32)

  def byte: Rng[Byte] =
    nextbits(8) map (_.toByte)

  def short: Rng[Short] =
    nextbits(16) map (_.toShort)

  def unit: Rng[Unit] =
    insert(())

  def boolean: Rng[Boolean] =
    chooseint(0, 1) map (_ == 0)

  def positivedouble: Rng[Double] =
    double map (math.abs(_))

  def negativedouble: Rng[Double] =
    double map (n => if(n < 0) n else -n)

  def positivefloat: Rng[Float] =
    float map (math.abs(_))

  def negativefloat: Rng[Float] =
    float map (n => if(n < 0) n else -n)

  def positivelong: Rng[Long] =
    long map (math.abs(_))

  def negativelong: Rng[Long] =
    long map (n => if(n < 0) n else -n)

  def positiveint: Rng[Int] =
    int map (math.abs(_))

  def negativeint: Rng[Int] =
    int map (n => if(n < 0) n else -n)

  def digit: Rng[Digit] =
    chooseint(0, 9) map mod10Digit

  def digits(z: Size): Rng[List[Digit]] =
    digit list z

  def digits1(z: Size): Rng[NonEmptyList[Digit]] =
    digit list1 z

  def numeric: Rng[Char] =
    digit map (_.toChar)

  def numerics(z: Size): Rng[List[Char]] =
    numeric list z

  def numerics1(z: Size): Rng[NonEmptyList[Char]] =
    numeric list1 z

  def char: Rng[Char] =
    nextbits(16) map (_.toChar)

  def chars(z: Size): Rng[List[Char]] =
    char list z

  def chars1(z: Size): Rng[NonEmptyList[Char]] =
    char list1 z

  def upper: Rng[Char] =
    chooseint(65, 90) map (_.toChar)

  def uppers(z: Size): Rng[List[Char]] =
    upper list z

  def uppers1(z: Size): Rng[NonEmptyList[Char]] =
    upper list1 z

  def lower: Rng[Char] =
    chooseint(97, 122) map (_.toChar)

  def lowers(z: Size): Rng[List[Char]] =
    lower list z

  def lowers1(z: Size): Rng[NonEmptyList[Char]] =
    lower list1 z

  def alpha: Rng[Char] =
    upper +++ lower map {
      case -\/(c) => c
      case \/-(c) => c
    }

  def alphas(z: Size): Rng[List[Char]] =
    alpha list z

  def alphas1(z: Size): Rng[NonEmptyList[Char]] =
    alpha list1 z

  def alphanumeric: Rng[Char] =
    chooseint(0, 61) map (c =>
      (if(c <= 25)
        c + 65
      else if(c <= 51)
        c + 71
      else
        c - 4).toChar)

  def alphanumerics(z: Size): Rng[List[Char]] =
    alphanumeric list z

  def alphanumerics1(z: Size): Rng[NonEmptyList[Char]] =
    alphanumeric list1 z

  def string(z: Size): Rng[String] =
    chars(z) map (_.mkString)

  def string1(z: Size): Rng[String] =
    chars1(z) map (_.toList.mkString)

  def upperstring(z: Size): Rng[String] =
    uppers(z) map (_.mkString)

  def upperstring1(z: Size): Rng[String] =
    uppers1(z) map (_.toList.mkString)

  def lowerstring(z: Size): Rng[String] =
    lowers(z) map (_.mkString)

  def lowerstring1(z: Size): Rng[String] =
    lowers1(z) map (_.toList.mkString)

  def alphastring(z: Size): Rng[String] =
    alphas(z) map (_.mkString)

  def alphastring1(z: Size): Rng[String] =
    alphas1(z) map (_.toList.mkString)

  def numericstring(z: Size): Rng[String] =
    numerics(z) map (_.mkString)

  def numericstring1(z: Size): Rng[String] =
    numerics1(z) map (_.toList.mkString)

  def alphanumericstring(z: Size): Rng[String] =
    alphanumerics(z) map (_.mkString)

  def alphanumericstring1(z: Size): Rng[String] =
    alphanumerics1(z) map (_.toList.mkString)

  def identifier(z: Size): Rng[NonEmptyList[Char]] =
    for {
      a <- alpha
      b <- alphanumerics(if(z exists (_ < 1)) z.inc else z.dec)
    } yield nel(a, b)

  def identifierstring(z: Size): Rng[String] =
    identifier(z) map (_.toList.mkString)

  def propernoun(z: Size): Rng[NonEmptyList[Char]] =
    for {
      a <- upper
      b <- lowers(if(z exists (_ < 1)) z.inc else z.dec)
    } yield nel(a, b)

  def propernounstring(z: Size): Rng[String] =
    propernoun(z) map (_.toList.mkString)

  def pair[A, B](a: Rng[A], b: Rng[B]): Rng[(A, B)] =
    a zip b

  def triple[A, B, C](a: Rng[A], b: Rng[B], c: Rng[C]): Rng[(A, B, C)] =
    for {
      aa <- a
      bb <- b
      cc <- c
    } yield (aa, bb, cc)

  def insert[A](a: A): Rng[A] =
    Rng(Return(a))

  def chooselong(l: Long, h: Long): Rng[Long] =
    long map (x => {
      val (ll, hh) = if(h < l) (h, l) else (l, h)
      ll + math.abs(x % (hh - ll + 1))
    })

  def choosedouble(l: Double, h: Double): Rng[Double] =
    unitdouble map (x => {
      val (ll, hh) = if(h < l) (h, l) else (l, h)
      val diff = hh - ll
      ll + x * diff
    })

  def choosefloat(l: Float, h: Float): Rng[Float] =
    unitfloat map (x => {
      val (ll, hh) = if(h < l) (h, l) else (l, h)

      if ((ll <= 0 && hh <= 0) || (ll >= 0 && hh >= 0)) ll + (hh - ll) * x
      else                                              ll * (1 - x) + hh * x
    })

  def chooseint(l: Int, h: Int): Rng[Int] =
    int map (x => {
      val (ll, hh) = if(h < l) (h, l) else (l, h)
      // using longs to avoid overflows
      val diff = hh.toLong - ll.toLong
      if (diff == 0) ll
      else           (ll.toLong + math.abs(x.toLong % (diff + 1))).toInt
    })

  def oneofL[A](x: NonEmptyList[A]): Rng[A] =
    chooseint(0, x.length - 1) map (x toList _)

  def oneof[A](a: A, as: A*): Rng[A] =
    oneofL(NonEmptyList(a, as: _*))

  def sequence[T[_], A](x: T[Rng[A]])(implicit T: Traverse[T]): Rng[T[A]] =
    T.sequence(x)

  def sequencePair[X, A](x: X, r: Rng[A]): Rng[(X, A)] =
    sequence[({type f[x] = (X, x)})#f, A]((x, r))

  def distribute[F[_], B](a: Rng[F[B]])(implicit D: Distributive[F]): F[Rng[B]] =
    D.cosequence(a)

  def distributeR[A, B](a: Rng[A => B]): A => Rng[B] =
    distribute[({type f[x] = A => x})#f, B](a)

  def distributeRK[A, B](a: Rng[A => B]): Kleisli[Rng, A, B] =
    Kleisli(distributeR(a))

  def distributeK[F[+_]: Distributive, A, B](a: Rng[Kleisli[F, A, B]]): Kleisli[F, A, Rng[B]] =
    distribute[({type f[x] = Kleisli[F, A, x]})#f, B](a)

  def frequencyL[A](x: NonEmptyList[(Int, Rng[A])]): Rng[A] = {
    val t = x.foldLeft(0) {
      case (a, (b, _)) => a + b
    }

    @annotation.tailrec
    def pick(n: Int, l: NonEmptyList[(Int, Rng[A])]): Rng[A] = {
      val (q, r) = l.head
      if(n <= q)
        r
      else l.tail match {
        case Nil => r
        case e::es => pick(n - q, nel(e, es))
      }
    }

    for {
      n <- chooseint(1, t)
      w <- pick(n, x)
    } yield w
  }

  def frequency[A](x: (Int, Rng[A]), xs: (Int, Rng[A])*): Rng[A] =
    frequencyL(NonEmptyList(x, xs: _*))

  implicit val RngMonad: Monad[Rng] =
    new Monad[Rng] {
      def bind[A, B](a: Rng[A])(f: A => Rng[B]) =
        a flatMap f
      def point[A](a: => A) =
        insert(a)
    }

  implicit def RngSemigroup[A](implicit S: Semigroup[A]): Semigroup[Rng[A]] =
    new Semigroup[Rng[A]] {
      def append(r1: Rng[A], r2: => Rng[A]) =
        r1 |+| r2
    }

  implicit def RngMonoid[A](implicit M: Monoid[A]): Monoid[Rng[A]] =
    new Monoid[Rng[A]] {
      def append(r1: Rng[A], r2: => Rng[A]) =
        r1 |+| r2

      def zero =
        insert(M.zero)
    }

}
