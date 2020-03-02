import cats.effect.{ IO, ExitCode, IOApp }
import cats.implicits._

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  implicit val grammarDecoder: QueryParamDecoder[List[(NonTerminal, List[Symbol])]] = {
    QueryParamDecoder[String].map{ str =>
      val listRuleStr = str.split("ENDLINE").toList
      val listNonTerminal = listRuleStr.map( _.split(" -> ").head ).map( nonTermString => NonTerminal(nonTermString) )
      val listRule = listRuleStr.map( _.split(" -> ").tail.headOption.getOrElse("") )
      def recursiveParse(s: String): List[Symbol] =
        if(s.length != 0) listNonTerminal.find( str => s.take(str.render.length) == str.render ) match {
            case Some(nt) => nt +: recursiveParse(s.drop(nt.render.length))
            case None => Terminal(s.head) +: recursiveParse(s.tail)
          }
        else List.empty[Symbol]
      listNonTerminal.zip(listRule.map(recursiveParse).map{
        case Nil => List(lambda)
        case other => other
      })
    }
  }

  implicit val typeRegularDecoder: QueryParamDecoder[TypeRegularGrammatic] = QueryParamDecoder[String].map{
    case "leftLinear" => LeftLinear
    case "rightLinear" => RightLinear
  }

  object AlphabetMatcher extends QueryParamDecoderMatcher[String]("alphabet")
  object InitStrMatcher extends QueryParamDecoderMatcher[String]("initStr")
  object FinalStrMatcher extends QueryParamDecoderMatcher[String]("finalStr")
  object MultiplicityMatcher extends QueryParamDecoderMatcher[Int]("multiplicity")
  object MinSizeMatcher extends QueryParamDecoderMatcher[Int]("minSize")
  object MaxSizeMatcher extends QueryParamDecoderMatcher[Int]("maxSize")
  object GrammarMatcher extends QueryParamDecoderMatcher[List[(NonTerminal, List[Symbol])]]("grammar")
  object TypeGrammarMatcher extends QueryParamDecoderMatcher[TypeRegularGrammatic]("typeRegularGrammar")

  val service = new ServiceImpl

  val routes = HttpRoutes.of[IO] {
    case request @ GET -> Root => StaticFile.fromResource("/index.html", blockingEC, Some(request)).getOrElseF(NotFound())
    case GET -> Root / "regularGrammar" :? AlphabetMatcher(alphabet) +& InitStrMatcher(initStr) +& FinalStrMatcher(finalStr) +& MultiplicityMatcher(multiplicity) +& TypeGrammarMatcher(typeRegularGrammatic) => Ok(service.regularGrammar(alphabet.toList, initStr, finalStr, multiplicity, typeRegularGrammatic).map( _.map(a => s"${a._1.render} -> ${a._2.map(_.render).mkString("")}").mkString("\n")))
    case GET -> Root / "stringsGrammar" :? GrammarMatcher(grammar) +& MinSizeMatcher(minSize) +& MaxSizeMatcher(maxSize) => Ok(service.stringsGrammar(grammar, minSize, maxSize).map( list => list.length.toString + "\n" + list.mkString("\n")))
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = {
      BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

sealed trait Symbol {
  def render: String
}
case class NonTerminal(symbol: String) extends Symbol {
  val render = symbol
}
case class Terminal(symbol: Char) extends Symbol {
  val render = symbol.toString
}
object lambda extends Terminal('*') {
  override val render = ""
}

sealed trait TypeRegularGrammatic
case object LeftLinear extends TypeRegularGrammatic
case object RightLinear extends TypeRegularGrammatic
