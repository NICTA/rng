# RNG Random Generation Library

This library provides the ability for a user to generate random values of arbitrary type. It provides a pure-functional
interface to preserve equational reasoning. Existing *combinator* libraries are provided for built-in data types. For
example:

* Generating random product types (tuples) given generators for its components.
* Generating random sum types given generators for its constructors
  * `scala.Either`
  * `scalaz.\/`
  * `scalaz.Validation`

Example usages are provided in the `/examples` source directory.

### `Rng` data type

A generator is represented by the `Rng` data type. A value of the type `Rng[T]` will produce random values of the type
`T`. For example, to generate random pairs `(X, Y)` and given a `Rng[X]` (call it `randomX`) and a `Rng[Y]`
(call it `randomY`), use the `zip` function:

    val randomPair: Rng[(X, Y)] =
      randomX zip randomY

To generate random sum type `(X \/ Y)` and given a `Rng[X]` (call it `randomX`) and a `Rng[Y]` (call it `randomY`), use
the `either` function:

    val randomEither: Rng[(X \/ Y)] =
      randomX either randomY

#### `Rng` structure

The `Rng` data type uses a specific technique for ensuring a pure-functional programming interface. It utilises
*the free monad* by defining a *grammar* for manipulation by the free monad.

The grammar is defined by the `RngOp` data type, which defines two primitive operations:

* `nextbits` which is expected to produce a random integer
* `setseed` which manipulates the seed for random generation

All other random generation operations are defined in terms of these two primitive operations.

The library user manipulates random generation by essentially building up a program that is composed of combinations of
these operations. Although it is important to understand this programming model, the library user is effectively
insulated by higher-level libraries. Users can expect the types provided by the library to dictate purpose. This
robustness is a consequence of the pure-functional programming interface. Side-effects are guaranteed to _never_ occur.

The `run` method on `Rng` executes the grammar and returns an `IO` action to manipulate the value arbitrary. The
provided examples demonstrate how to achieve this.

#### `RngOp`

Th `RngOp` data type underlies the random generator by providing a grammar with two instructions (`nextbits` and
`setseed`). Programming the `RngOp` data type directly is atypical and users might consider the higher-level library
provided on the `Rng` data type instead. The `RngOp` type constructor forms a `comonad` and so has operations for
manipulating the operation:

* The `map` method on `RngOp[A]` accepts a function (`A => B`) and returns a `RngOp[B]`
* The `coflatMap` method on `RngOp[A]` accepts a function (`RngOp[A] => B`) and returns a `RngOp[B]`

`RngOp` values can then be lifted to a generator (`Rng`) using the `lift` method.

#### `Rng` combinator library

Many random generators are provided, such as:

* The `option` method produces a generator for `Rng[Option[T]]` when the method is called on a value of the type
`Rng[T]`.
* The `list` method produces a generator for `Rng[List[T]]` when the method is called on a value of the type
`Rng[T]`.
* The `list1` method produces a generator for `Rng[NonEmptyList[T]]` when the method is called on a value of the type
`Rng[T]`.
* Generators for primitive types provided as functions on the `Rng` object:
  * `int` to generate random integer values
  * `byte` to generate random byte values
  * `short` to generate random short values
  * `long` to generate random long values
  * `double` to generate random double values
  * `float` to generate random float values
  * `boolean` to generate random boolean values
  * `short` to generate random short values
  * `char` to generate random character values
  * `chars` to generate random lists of character values
  * `chars1` to generate random non-empty lists of character values
  * Upper-case characters
    * `upper` to generate random upper-case (A-Z) character values
    * `uppers` to generate random lists of upper-case (A-Z) character values
    * `uppers1` to generate random non-empty lists of upper-case (A-Z) character values
    * `upperstring` to generate random strings of upper-case (A-Z) character values
    * `upperstring1` to generate random non-empty strings of upper-case (A-Z) character values
  * Lower-case characters
    * `lower` to generate random lower-case (a-z) character values
    * `lowers` to generate random lists of lower-case (a-z) character values
    * `lowers1` to generate random non-empty lists of lower-case (a-z) character values
    * `lowerstring` to generate random strings of lower-case (a-z) character values
    * `lowerstring1` to generate random non-empty strings of lower-case (a-z) character values
  * Alpha characters
    * `alpha` to generate random alpha (a-z and A-Z) character values
    * `alphas` to generate random lists of alpha (a-z and A-Z)character values
    * `alphas1` to generate random non-empty lists of (a-z and A-Z) alpha character values
    * `alphastring` to generate random strings of alpha (a-z and A-Z) character values
    * `alphastring1` to generate random non-empty strings of alpha (a-z and A-Z) character values
  * Numeric characters
    * `numeric` to generate random numeric (0-9) character values
    * `numerics` to generate random lists of numeric (0-9) character values
    * `numerics1` to generate random non-empty lists of numeric (0-9) character values
    * `numericstring` to generate random strings of numeric (0-9) character values
    * `numericstring1` to generate random non-empty strings of numeric (0-9) character values
  * Alpha-numeric characters
    * `alphanumeric` to generate random alpha-numeric (a-z, A-Z and 0-9) character values
    * `alphanumerics` to generate random lists of alpha-numeric (a-z, A-Z and 0-9) character values
    * `alphanumerics1` to generate random non-empty lists of alpha-numeric (a-z, A-Z and 0-9) character values
    * `alphanumericstring` to generate random strings of alpha-numeric (a-z, A-Z and 0-9) character values
    * `alphanumericstring1` to generate random non-empty strings of alpha-numeric (a-z, A-Z and 0-9) character values
  * Identifiers (an identifier is defined by a string of characters starting with an alpha character, followed by zero
        or more alpha-numeric characters)
    * `identifier` for generating random non-empty lists of characters representing an identifier
    * `identifierstring` for generating random non-empty strings representing an identifier
  * Proper noun (a proper noun is defined by a string of characters starting with an upper-case character, followed by
        zero or more lower-case characters)
    * `propernoun` for generating random non-empty lists of characters representing a proper noun
    * `propernounstring` for generating random non-empty strings representing a proper noun
  * Integer ranges
    * `negative(double/float/long/int)` to generate random negative doubles/floats/longs/integers
    * `positive(double/float/long/int)` to generate random positive doubles/floats/longs/integers
    * `chooseint` to generate random integers in a given range
    * `chooselong` to generate random longs in a given range
    * `choosefloat` to generate random floats in a given range
    * `choosedouble` to generate random doubles in a given range
* Generators for values of the `scalaz.Digit` data type provided as functions on the `Rng` object:
  * `digit` to generate random digit values
  * `digits` to generate random lists of digit values
  * `digits1` to generate random non-empty lists of digit values
* Generators for manipulating lists of values
  * `oneof` accepts a non-empty (variable argument) list of values and returns a generator that produces one of those
        values
  * `oneofL` does the same as `oneof`, however, it is accepts an argument of `scalaz.NonEmptyList` instead of a
        non-empty argument list.
  * `frequency` accepts a non-empty (variable argument) list of pairs of values. The pair is an integer and a random
        generator where the integer represents the skewed frequency of the associated random generator. The `frequencyL`
        function returns a generator that will select from the given list of generators with a skewed distribution.
  * `frequencyL` does the same as `frequency`, however, it is accepts an argument of `scalaz.NonEmptyList` instead of a
        non-empty argument list.
* Distributing and traversing generators
  * `sequence` for taking a traversable of generators to a generator of traversables. A Traversable value is represented
        as a generalised interface (`scalaz.Traverse`).
  * `distribute` for taking a generator of distributive values to a distribution of generators. A distributive value is
        represented as a generalised interface (`scalaz.Distributive`).

#### `Rng` monad

The `Rng` type constructor forms a *monad* making it trivial to combine existing random generators for user-defined data
types. For example, consider a data type combined of products and sums:

    case class Person(name: String, age: Option[Int])

A random generator can be constructed by combining random generators for `String`, `Int` and `Option` using a
*for-comprehension*:

    val randomPerson: Rng[Person] =
      for {
        n <- Rng.string
        a <- Rng.int.option
      } yield Person(n, a)

### Documentation

Documentation for this library is provided by this document, example usages and static-type verification. The statically verified constraints provided by this
library are a consequence of the pure-functional programming interface.
