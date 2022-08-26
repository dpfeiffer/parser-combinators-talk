import cats.parse._
import cats.parse.Parser.{char => pchar}
import cats.parse.Rfc5234._
import cats.implicits._

case class Equals(variable: String, value: String)

@main
def ambiguity: Unit = {

  val stringLiteral: Parser[String] = alpha.rep.string
    .surroundedBy(Parser.char('\''))

  val variable: Parser[String] = alpha.rep.string

  val equals: Parser[Equals] =
    (variable ~ Parser.string("eq").surroundedBy(wsp) ~ stringLiteral).map {
      case ((variable, _), value) => Equals(variable, value)
    }

  val parser = equals.orElse(variable)
  // val parser = variable.orElse(equals)

  println("Successful Equals: " + parser.parse("name eq 'John'"))
  println("Arresting failure variable: " + parser.parse("employed"))

  println("Epsilon failure variable: " + variable.parse("'John'"))
  println("Arresting failure equals: " + equals.parse("name"))

  //some parser combinator libraries have opt-out backtracking
  val backtrackingParser = equals.backtrack | variable
  println("Successful Equals: " + backtrackingParser.parse("name eq 'John'"))
  println("Successful variable: " + backtrackingParser.parse("employed"))

}
