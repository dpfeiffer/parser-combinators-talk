using Sprache;

public record Person(string FirstName, string LastName, int Age, bool Employed);

public interface BooleanExpression { }

public interface Literal { }
public record StringLiteral(string Value) : Literal;

public record BooleanLiteral(bool Value) : BooleanExpression, Literal;

public record IntegerLiteral(int Value) : Literal;

public record Variable(string Name) : BooleanExpression;


public enum ComparisonOperator
{
    Eq, Lt, Gt
}

public record Comparison(Variable variable, ComparisonOperator op, Literal value) : BooleanExpression;

class BooleanExpressionGrammar
{
    private static Parser<BooleanLiteral> s_booleanLiteral = Parse.String("true").Return(new BooleanLiteral(true))
            .Or(Parse.String("false").Return(new BooleanLiteral(false)));

    private static Parser<IntegerLiteral> s_integerLiteral = from nr in Parse.Number select new IntegerLiteral(int.Parse(nr));

    private static Parser<StringLiteral> s_stringLiteral =
        from lQuote in Parse.Char('\'')
        from value in Parse.Letter.Many().Text()
        from rQuote in Parse.Char('\'')
        select new StringLiteral(value);

    private static Parser<ComparisonOperator> s_comparisonOperatorParser =
        Parse.String("lt").Return(ComparisonOperator.Lt)
            .Or(Parse.String("eq").Return(ComparisonOperator.Eq))
            .Or(Parse.String("gt").Return(ComparisonOperator.Gt));

    private static Parser<Variable> Variable =
        from name in Parse.Letter.Many().Text()
        select new Variable(name);

    private static Parser<Literal> s_literalParser = s_booleanLiteral.Or<Literal>(s_integerLiteral).Or<Literal>(s_stringLiteral);


    private static Parser<Comparison> s_comparisonParser =
        from variable in Variable
        from leading in Parse.WhiteSpace.Once()
        from op in s_comparisonOperatorParser
        from trailing in Parse.WhiteSpace.Once()
        from literal in s_literalParser
        select new Comparison(variable, op, literal);

    public static Parser<BooleanExpression> BooleanExpression = s_comparisonParser
        .Or<BooleanExpression>(s_booleanLiteral)
        .Or<BooleanExpression>(Variable);
}

class Parsing
{

    private static Predicate<Person> Interpret(BooleanExpression exp)
    {
        return person =>
                {
                    if (exp is BooleanLiteral b)
                    {
                        return b.Value;
                    }
                    else if (exp is Variable v && v.Name == "employed")
                    {
                        return person.Employed;
                    }
                    else if (exp is Comparison c
                        && c.value is StringLiteral s
                        && c.op == ComparisonOperator.Eq
                        && c.variable.Name == "firstName")
                    {
                        return person.FirstName == s.Value;

                    }
                    else if (exp is Comparison c1
                        && c1.value is StringLiteral s1
                        && c1.op == ComparisonOperator.Eq
                        && c1.variable.Name == "lastname")
                    {
                        return person.LastName == s1.Value;

                    }
                    else if (exp is Comparison c2
                        && c2.value is IntegerLiteral i
                        && c2.variable.Name == "age")
                    {
                        return c2.op switch
                        {
                            ComparisonOperator.Eq => person.Age == i.Value,
                            ComparisonOperator.Lt => person.Age < i.Value,
                            ComparisonOperator.Gt => person.Age > i.Value,
                            _ => throw new ArgumentException("unsupported operator")
                        };

                    }
                    else
                    {
                        throw new ArgumentException("unsupported expression");
                    }
                };
    }

    static void Main(string[] args)
    {

        var People = new List<Person>();
        People.Add(new Person("John", "Doe", 47, false));
        People.Add(new Person("Jane", "Doe", 35, false));

        BooleanExpression Query = BooleanExpressionGrammar.BooleanExpression.Parse("firstName eq 'Jane'");
        Predicate<Person> Predicate = Interpret(Query);

        People.FindAll(Predicate).ForEach(Console.WriteLine);

    }
}