package com.nicta
package rng

import scalaz._, Scalaz._, effect._

// A data type representing a person.
// We are going to produce random instances.
// Note that this data type is recursive.
// The `children` field holds 0 or many Person instances.
case class Person(name: String, age: Int, surname: Option[String], children: List[Person])

object Person {
  // Implement a `Show` instance for `Person` so that we can display the result.
  implicit val ShowPerson: Show[Person] =
    Show.show(p =>
      ("Person(": Cord) ++
      p.name ++ "," ++
      p.age.show ++ "," ++
      (p.surname | "<no-surname>") ++ "," ++
      p.children.show ++ ")"
    )
}

// Use the `SafeApp` trait from scalaz to maintain purity.
// This use-case will override the `runc` method
// which returns a value of the type `IO[Unit]`.
object RunPerson extends SafeApp {
  import Rng._

  // The random generator for person
  // is constructed from generators for its parts.
  val randomPerson: Rng[Person] =
    for {
      // The `name` field is generated as a proper noun.
      // The size of the name is limited to a maximum of 10.
      n <- propernounstring(Size(10))
      // The `age` field is generated as an integer between 0 and 120.
      a <- chooseint(0, 120)
      // The optional surname field is generated as an alpha string
      // with a size limited to a maximum of 15.
      s <- alphastring(15).option
      // The `children` field is generated as a recursive call.
      // The `list` random generator is used
      // and size is limited to a maximum of 2.
      c <- randomPerson.list(2)
    } yield Person(n, a, s, c)

  override def runc = {
    // Run the person generator.
    // Produces an `IO` of `Person`.
    val p = randomPerson.run
    // Return the action that prints result of the person generator.
    p map (_.println)
  }

}
