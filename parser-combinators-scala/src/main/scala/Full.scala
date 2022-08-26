import cats.parse._
import cats.parse.Rfc5234._
import cats.implicits._
import cats.kernel.Eq

case class Variable(name: String)
case class StringLiteral(value: String)
case class BooleanLiteral(value: Boolean)
case class IntegerLiteral(value: Int)

type BooleanExpression = Comparison | Variable | BooleanLiteral
type Constant = StringLiteral | BooleanLiteral | IntegerLiteral

enum ComparisonOperator {
  case Eq, Lt, Gt
}

case class Comparison(
    l: Variable | Constant,
    operator: ComparisonOperator,
    r: Variable | Constant
)

val expressionParser = Parser.recursive[BooleanExpression] { recurse =>
  val variable: Parser[Variable] = alpha.rep.string.map(Variable.apply)

  val integerLiteral: Parser[Constant] = digit.rep.string.map(_.toInt).map(IntegerLiteral.apply)

  val stringLiteral: Parser[Constant] =
    (Parser.char('\'') *> alpha.rep.string <* Parser.char('\''))
      .map(StringLiteral.apply)

  val booleanLiteral: Parser[BooleanLiteral] =
    Parser.stringIn(Seq("true", "false")).map {
      case "true"  => BooleanLiteral(true)
      case "false" => BooleanLiteral(false)
    }

  val constant: Parser[Constant] = stringLiteral.orElse(booleanLiteral).orElse(integerLiteral)

  val eq = Parser.string("eq").map(_ => ComparisonOperator.Eq)
  val lt = Parser.string("lt").map(_ => ComparisonOperator.Lt)
  val gt = Parser.string("gt").map(_ => ComparisonOperator.Gt)

  val comparisonOperator: Parser[ComparisonOperator] = eq | lt | gt

  val constantOrVariable: Parser[Constant | Variable] =
    constant.orElse(variable)

  val comparison: Parser[Comparison] = {
    val operator = comparisonOperator.surroundedBy(wsp)

    (constantOrVariable, operator, constantOrVariable).tupled
      .map((i, o, c) => Comparison(i, o, c))
  }

  Parser.oneOf(comparison.backtrack :: booleanLiteral :: variable :: Nil)
}

val people = List(
  Person("John", "Doe", false, 47),
  Person("Jane", "Doe", true, 35),
  Person("Max", "Mustermann", true, 47),
  Person("Erika", "Musterfrau", false, 49)
)

@main def full: Unit = {
  filterPeople("true")
  filterPeople("false")
  filterPeople("employed")
  filterPeople("firstName eq 'Erika'")
  filterPeople("age gt 47")
  filterPeople("age lt 40")
}

def filterPeople(filterExpression: String) = {

  val result = expressionParser
    .parse(filterExpression)
    .map((_, expression) => expression)
    .getOrElse(throw new IllegalArgumentException("invalid expression"))

  val predicate: Person => Boolean = result match {
    case BooleanLiteral(a)    => _ => a
    case Variable("employed") => _.employed

    case Comparison(Variable("firstName"), ComparisonOperator.Eq, StringLiteral(y)) => _.firstName == y
    case Comparison(Variable("lastName"), ComparisonOperator.Eq, StringLiteral(y))  => _.lastName == y
    case (Comparison(Variable("age"), operator, IntegerLiteral(i))) =>
      operator match {
        case ComparisonOperator.Eq => _.age == i
        case ComparisonOperator.Lt => _.age < i
        case ComparisonOperator.Gt => _.age > i
      }
    case _ => throw new IllegalArgumentException("unsupported expression")
  }

  println(people.filter(predicate))
}
