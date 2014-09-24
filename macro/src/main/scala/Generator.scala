/**
 * Created by user on 9/24/14.
 */

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.util.parsing.combinator.RegexParsers


class generator(source: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro typeConstructorMacro.impl
}

case class Header(name: String, typ: String)
case class Line(l: List[Substitution])
case class Substitution(before: Option[String], name: String, typ: String, after: Option[String])
case class Instrument(h: Header, l: List[Line])
case class Grammar(l: Map[String, List[Instrument]])

trait GrammarParser extends RegexParsers {
  override val whiteSpace = """[ \t]+""".r
  def grammar = rep(instrument) <~ "\n" ^^ {case l => Grammar(l.groupBy(_.h.typ))}
  def instrument = header ~ "\n" ~ rep(line) ^^ { case a ~ _ ~ b => Instrument(a, b)}
  def header = "instrument {" ~ "[^\\}]*".r ~ "}" ~ "is" ~ token ^^ { case _ ~ a ~_ ~ _ ~ b => Header(a, b)}
  def line = rep(substitution) <~ "\n" ^^ Line
  def substitution = token.? ~ ("{" ~> token ~ ":" ~ token <~ "}") ~ token.? ^^ { case a ~ (b ~ _ ~ c) ~ d => Substitution(a, b, c, d)}
  def token = "[^\\{\\}\n:]*".r
  def parse(s: String) = parseAll(instrument, s)
}

object typeConstructorMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import scala.io.Source
    val grammarRaw = Source.fromURL(this.getClass.getResource("/option.template")).getLines().mkString("\n")

    val grammar = new GrammarParser {}.parse(grammarRaw).get
    import c.universe._
    val inputs = annottees.map(_.tree).toList

    def bigStart(s: String) = s.updated(0, s.head.toUpper)
    def lines = grammar.l.map(_.l).filter(_.nonEmpty)
    def params = lines.map(_.head.name).map(x => q"val ${newTermName(x)}: ${newTypeName(bigStart(x))}")
    val classes = lines.map(l => q"case class ${newTypeName(bigStart(l.head.name))} (..${l.map(x => q"val ${newTermName(x.name)}: ${newTypeName("String")}")})")

    val defs = List(q"case class ${newTypeName(grammar.h.typ)}( ..$params)") ++ classes

    case class ParserConfiguration(regexp: String, number: Int, className: String)
    val parsers = for (l <- lines) yield {
        implicit class fromOption(o: Option[String]) { def s = o.getOrElse("")}
        val separators = (l.head.before.s :: l.zip(l.tail).map(x => x._1.after.s + x._2.before.s)) :+ l.last.after.s
        val regexp = " *" + separators.mkString(" *([^ ]*?) *")
        ParserConfiguration(regexp, separators.size - 1, bigStart(l.head.name))
    }

    val output = inputs.head match {
      case q"object $name extends $parent { ..$body }" =>
        q"""
            object $name extends $parent {
              ..$defs

                def parse(what: String) = {
                  val TypRegexp = "instrument (.*?) is .*".r
                  val split = what.split("\n")
                  val TypRegexp(typ) = split.head
                  val body = split.tail
                  new ${newTypeName(grammar.h.typ)}(..${for(conf <- parsers) yield {
                    val members = (1 to conf.number).map("m" + _)
                    val from = members.map(x => Bind(newTermName(x), Ident(nme.WILDCARD)))
                    val to = members.map(newTermName(_)).map(x => q"$x.trim")
                    q"""{
                         val Regexp = ${conf.regexp}.r
                         val className: String = ${conf.className}
                         val count: Int = ${conf.number}
                         body collect {
                           case Regexp(..$from) => new ${newTypeName(conf.className)}(..$to)
                         } head
                        }
                     """
                      }
                   }
                  )
                }
            }
          """
      case x => sys.error(x.toString)
    }

    println(output)
    c.Expr[Any](output)
  }
}
