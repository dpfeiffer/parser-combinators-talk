import cats.parse._
import cats.parse.Parser.{char => pchar}
import cats.parse.Rfc5234._
import cats.implicits._

@main
def combinators = {

  val numberOrString = digit.rep.string | alpha.rep.string
  println("numberOrString number: " + numberOrString.parse("4711"))
  println("numberOrString string: " + numberOrString.parse("Vienna"))

  val naturalNumber = Parser.char('-').? ~ digit.rep.string
  println("naturalNumber positive: " + naturalNumber.parse("10"))
  println("naturalNumber negative: " + naturalNumber.parse("-10"))

  val stringLiteral = alpha.rep0.string.surroundedBy(Parser.char('\''))
  println("stringLiteral literal: " + stringLiteral.parse("'Vienna'"))
  println("stringLiteral string: " + stringLiteral.parse("Vienna"))

}
