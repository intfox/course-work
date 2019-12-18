import cats.effect.{ IO, ExitCode, IOApp }
import cats.implicits._
import cats.{ UnorderedTraverse, CommutativeApplicative, Applicative }
import cats.effect.concurrent.Ref

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.staticcontent._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  object AlphabetMatcher extends QueryParamDecoderMatcher[String]("alphabet")
  object InitStrMatcher extends QueryParamDecoderMatcher[String]("initStr")
  object FinalStrMatcher extends QueryParamDecoderMatcher[String]("finalStr")
  object MultiplicityMatcher extends QueryParamDecoderMatcher[Int]("multiplicity")
  object MinSizeMatcher extends QueryParamDecoderMatcher[Int]("minSize")
  object MaxSizeMatcher extends QueryParamDecoderMatcher[Int]("maxSize")

  case class AppState(alphabet: Option[List[Char]] = None, initStr: Option[String] = None, finalStr: Option[String] = None, multiplicity: Option[Int] = None, typeGrammatic: TypeRegularGrammatic = RightLinear, regularGrammatic: Option[List[(NonTerminal, List[Symbol])]] = None, stringsGrammatic: Option[StringsGrammatic] = None, minSize: Option[Int] = None, maxSize: Option[Int] = None)

  def alphabetFromString(str: String): Either[String, List[Char]] = Right(str.toList)

  def routes(refAppState: Ref[IO, Map[String, AppState]]) = HttpRoutes.of[IO] {
    case request @ GET -> Root => StaticFile.fromResource("/index.html", blockingEC, Some(request)).getOrElseF(NotFound())
    case POST -> Root / id / "set" :? AlphabetMatcher(alphabet) => Ok(alphabetFromString(alphabet) match {
      case Right(list) => refAppState.get.flatMap{ _.get( id ) match {
        case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(alphabet = Some(list))) ).map( _ => "OK" )
        case None => IO.pure(s"Not found by id $id")
      }}
      case Left(error) => IO.pure(error)
    })
    case POST -> Root / id / "set" :? InitStrMatcher(str) => Ok(refAppState.get.flatMap{ _.get( id ) match {
        case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(initStr = Some(str))) ).map( _ => "OK" )
        case None => IO.pure(s"Not found by id $id")
      }})
    case POST -> Root / id / "set" :? FinalStrMatcher(str) => Ok(refAppState.get.flatMap{ _.get( id ) match {
      case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(finalStr = Some(str))) ).map( _ => "OK" )
      case None => IO.pure(s"Not found by id $id")
    }})
    case POST -> Root / "create" => Ok(
      for {
        newId <- IO{ java.util.UUID.randomUUID().toString }
        _ <- IO{ println(s"generate id: $newId") }
        _ <- refAppState.update( map => map + (newId -> AppState()))
      } yield newId)
    case POST -> Root / id / "set" :? MultiplicityMatcher(multiplicity) => Ok(refAppState.get.flatMap{ _.get( id ) match {
      case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(multiplicity = Some(multiplicity))) ).map( _ => "OK" )
      case None => IO.pure(s"Not found by id $id")
    }})
    case POST -> Root / id / "generateGrammatic" => Ok(refAppState.get.flatMap{ _.get(id) match {
      case Some(state) => (for{
        alphabet <- state.alphabet
        initStr <- state.initStr
        finalStr <- state.finalStr
        multiplicity <- state.multiplicity
        typeGrammatic = state.typeGrammatic
        rg = regularGrammatic(alphabet, initStr, finalStr, multiplicity, typeGrammatic)
      } yield (StringsGrammatic(rg).flatMap( sg => refAppState.update( stateApp =>  stateApp.updated( id, stateApp(id).copy( regularGrammatic = Some(rg), stringsGrammatic = Some(sg) ) )).map(_ => rg.map(a => s"${a._1.render} -> ${a._2.map(_.render).mkString("")}").mkString("\n"))))).getOrElse(IO.pure("Not all field input!"))
      case None => IO.pure(s"not found id: $id")
    }})
    case POST -> Root / id / "set" :? MinSizeMatcher(size) => Ok(refAppState.get.flatMap{ _.get( id ) match {
      case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(minSize = Some(size))) ).map( _ => "OK" )
      case None => IO.pure(s"Not found by id $id")
    }})
    case POST -> Root / id / "set" :? MaxSizeMatcher(size) => Ok(refAppState.get.flatMap{ _.get( id ) match {
      case Some(_) => refAppState.update( map => map.updated(id, map(id).copy(maxSize = Some(size))) ).map( _ => "OK" )
      case None => IO.pure(s"Not found by id $id")
    }})
    case GET -> Root / id / "generateStr" => Ok(refAppState.get.flatMap{ _.get( id ) match {
      case Some(state) => (for {
        sg <- state.stringsGrammatic
        min <- state.minSize
        max <- state.maxSize 
      } yield (sg(min, max).map( _.mkString("\n")))).getOrElse(IO.pure("Not all!"))
      case None => IO.pure(s"Not found by id $id")
    }})
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    Ref.of[IO, Map[String, AppState]](Map.empty).flatMap{ appState => 
      BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes(appState))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
    }
  }
    

  lazy val listNonTerminalSymbol: Stream[NonTerminal] = new NonTerminal{ val render = "A" } #:: listNonTerminalSymbol.map( prevSymbol => new NonTerminal{ val render = prevSymbol.render.init + (prevSymbol.render.last + 1).toChar } )
  
  def regularGrammatic( alphabet: List[Char], initStr: String, finalStr: String, multiplicity: Int, typeGrammatic: TypeRegularGrammatic ): List[(NonTerminal, List[Symbol])] = typeGrammatic match {
    case RightLinear => 
      val initStrGrammatic = listNonTerminalSymbol.head -> (initStr.toList.map( Terminal ) :+ listNonTerminalSymbol(1))
      val numbBalancingRule = multiplicity - ((initStr.length + finalStr.length) % multiplicity)
      val characterBalancingGrammatic = listNonTerminalSymbol.drop(1).take(numbBalancingRule)
        .zip(listNonTerminalSymbol.drop(2).take(numbBalancingRule))
        .flatMap{ case (symbolRule, symbolNextRule) => alphabet.map( Terminal ).map( alphabetSymbol => symbolRule -> List(alphabetSymbol, symbolNextRule) ) }
        .++( alphabet.map( alphabetSymbol => listNonTerminalSymbol(numbBalancingRule) -> List( Terminal(alphabetSymbol), listNonTerminalSymbol(1 + numbBalancingRule + multiplicity) ) ) ) .toList
      val cicleGrammatic = listNonTerminalSymbol.drop(1 + numbBalancingRule).take(multiplicity - 1)
        .zip( listNonTerminalSymbol.drop(1 + numbBalancingRule + 1).take(multiplicity - 1))
        .flatMap{ case (symbolRule, symbolNextRule) => alphabet.map( alphabetSymbol => symbolRule -> List(Terminal(alphabetSymbol), symbolNextRule) ) }
        .++( alphabet.map( alphabetSymbol => listNonTerminalSymbol(1 + numbBalancingRule + (multiplicity - 1)) -> List( Terminal(alphabetSymbol), listNonTerminalSymbol(1 + numbBalancingRule + multiplicity) ) ) ) 
        .++( alphabet.map( alphabetSymbol => listNonTerminalSymbol(1 + numbBalancingRule + (multiplicity - 1)) -> List( Terminal(alphabetSymbol), listNonTerminalSymbol(1 + numbBalancingRule ) ) ) ).toList
      val finalStrGrammatic = listNonTerminalSymbol(1 + numbBalancingRule + multiplicity).->[List[Terminal]](finalStr.toList.map( Terminal ))

      (initStrGrammatic +: characterBalancingGrammatic) ++ cicleGrammatic :+ finalStrGrammatic
    case LeftLinear =>  
      regularGrammatic(alphabet, finalStr, initStr, multiplicity, RightLinear).map{ case (symbolRule, rule) => 
        val newRule = rule.last match {
          case symbol: NonTerminal => symbol +: rule.init
          case _ => rule
        }
        (symbolRule, newRule)
      }
  }
}

class StringsGrammatic(mapGrammatic: Map[NonTerminal, (List[List[Symbol]], StringsGrammatic.StringsCache)], start: NonTerminal) {
  def apply(minSize: Int, maxSize: Int): IO[List[String]] = generate(maxSize, 0, start).map( _.filter( _.length > minSize ) )

  implicit class richRule(rule: (List[List[Symbol]], StringsGrammatic.StringsCache)) {
    def cache: IO[List[String]] = rule._2.get.map( _._2 )
    def maxSize: IO[Int] = rule._2.get.map( _._1 )
    def listRules: List[List[Symbol]] = rule._1 
  }

  private def generate( maxSize: Int, thatSize: Int, thatTerminal: NonTerminal ): IO[List[String]] = if(thatSize + 1 > maxSize) IO.pure(List.empty[String]) else mapGrammatic(thatTerminal).maxSize.flatMap{ maxSizeInCache =>
    if(maxSize - thatSize > maxSizeInCache) {
      val rules = mapGrammatic(thatTerminal).listRules
      val result = rules.map( listSymbol => listSymbol.map{
        _ match {
          case Terminal(symbol) => IO.pure(List(symbol.toString))
          case nt: NonTerminal => generate( maxSize, thatSize + (listSymbol.size - 1), nt )
        }
      }.sequence.map( _.reduce( (a, b) => a.flatMap( as => b.map( bs => as + bs ) ) ) ) ).sequence.map( _.flatten )
      result.flatMap{ res =>
        mapGrammatic(thatTerminal)._2.update{ case (maxSizeInCache, list) => if(maxSize - thatSize > maxSizeInCache) (maxSize - thatSize, res) else (maxSizeInCache, list) }.map( _ => res.filter( _.length < maxSize - thatSize + 1 ) ) }
    } else mapGrammatic(thatTerminal).cache.map( _.filter( _.length < maxSize - thatSize + 1 ) )
  } 
}

object StringsGrammatic {

  type NonTerminalMap[T] = Map[NonTerminal, T]
  
  type StringsCache = Ref[IO, (MaxSize, List[String])]
  type MaxSize = Int

  implicit object FakeEvidence extends CommutativeApplicative[IO] {
    override def pure[A](x: A): IO[A] = IO(x)
    override def ap[A, B](ff: IO[A => B])(fa: IO[A]): IO[B] =
      fa.flatMap( a => ff.map( _(a) ) )
  }

  def apply(grammatic: List[(NonTerminal, List[Symbol])]): IO[StringsGrammatic] = {
    val map: Map[NonTerminal, IO[(List[List[Symbol]], StringsCache)]] = grammatic.groupBy( _._1 ).mapValues( _.map( _._2 ) ).mapValues{ matrixSymbol =>
      Ref.of[IO, (MaxSize, List[String])]((0, List.empty)).map{ refCache =>
        (matrixSymbol, refCache)
      }
    }
    implicitly[UnorderedTraverse[NonTerminalMap]].unorderedSequence(map).map( thatMap => new StringsGrammatic(thatMap, grammatic.head._1) )
  }
    

}

sealed trait Symbol {
  def render: String
}
trait NonTerminal extends Symbol
case class Terminal(symbol: Char) extends Symbol {
  val render = symbol.toString
}

sealed trait TypeRegularGrammatic
case object LeftLinear extends TypeRegularGrammatic
case object RightLinear extends TypeRegularGrammatic
