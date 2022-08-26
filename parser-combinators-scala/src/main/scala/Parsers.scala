import cats.parse._
import cats.parse.Parser.{char => pchar}
import cats.parse.Rfc5234._
import cats.implicits._

@main
def parsers: Unit = {

  println("alpha: " + alpha.parse("ABCD"))
  println("alpha.rep: " + alpha.rep.parse("ABCD"))
  println("alpha.rep: " + alpha.rep.string.parse("ABCD"))

  println("wsp: " + wsp.parse(" "))

  println("true string: " + Parser.string("true").parse("true"))
  println("false string: " + Parser.string("true").parse("false"))

}
