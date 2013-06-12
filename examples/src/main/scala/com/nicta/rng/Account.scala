package com.nicta
package rng

import scalaz._, Scalaz._, Validation._, effect._
import Rng._

case class Balance(amount: Balance.ErrorMessage \?/ Int)

object Balance {
  type ErrorMessage =
  String

  val randomBalance =
    string(3) \?/ positiveint map (Balance(_))

  implicit val ShowBalance: Show[Balance] =
    Show.shows(_.amount.swap valueOr (_.shows))
}

case class AccountNumber(value: List[Digit] \/ Int)

object AccountNumber {
  val randomAccountNumber =
    digits(10) \/ positiveint map (AccountNumber(_))

  implicit val ShowAccountNumber: Show[AccountNumber] =
    Show.shows(_.value fold (_.shows, _.shows))
}

sealed trait AccountType
case object Cheque extends AccountType
case object Savings extends AccountType
case class Other(n: NonEmptyList[Digit]) extends AccountType

object AccountType {
  val randomAccountType =
    digits1(10).option.option map {
      case None => Cheque
      case Some(None) => Savings
      case Some(Some(s)) => Other(s)
    }

  implicit val ShowAccountType: Show[AccountType] =
    Show.shows {
      case Cheque => "Cheque"
      case Savings => "Savings"
      case Other(x) => "Other(" + x.shows + ")"
    }

}

case class Account(name: String, balance: Balance, number: AccountNumber, tp: AccountType)

object Account {
  val randomAccount =
    for {
      n <- alphastring1(6)
      b <- Balance.randomBalance
      m <- AccountNumber.randomAccountNumber
      t <- AccountType.randomAccountType
    } yield Account(n, b, m, t)

  implicit val ShowAccount: Show[Account] =
    Show.show(a =>
      ("Account(": Cord) ++
      a.name ++ "," ++
      a.balance.show ++ "," ++
      a.number.show ++ "," ++
      a.tp.show ++ ")"
    )
}

object RunAccount extends SafeApp {
  override def runc = {
    val a = Account.randomAccount.run

    a map (_.println)
  }

}