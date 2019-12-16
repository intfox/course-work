import cats.effect.IO
import cats.implicits._

import scala.concurrent.ExecutionContext

object Main extends App {

  lazy val listNonTerminalSymbol: Stream[NonTerminal] = new NonTerminal{ val render = "A" } #:: listNonTerminalSymbol.map( prevSymbol => new NonTerminal{ val render = prevSymbol.render.init + (prevSymbol.render.last + 1).toChar } )
  
  def regularGrammatic( alphabet: List[Char], initStr: String, finalStr: String, multiplicity: Int, typeGrammatic: TypeRegularGrammatic ): List[(NonTerminal, List[Symbol])] = typeGrammatic match {
    case RightLinear => 
      val initStrGrammatic = listNonTerminalSymbol.head -> (initStr.toList.map( Terminal ) :+ listNonTerminalSymbol(1))
      val numbBalancingRule = multiplicity - ((initStr.length + finalStr.length) % multiplicity)
      val characterBalancingGrammatic = listNonTerminalSymbol.drop(1).take(numbBalancingRule)
        .zip(listNonTerminalSymbol.drop(2).take(numbBalancingRule))
        .flatMap{ case (symbolRule, symbolNextRule) => alphabet.map( Terminal ).map( alphabetSymbol => symbolRule -> List(alphabetSymbol, symbolNextRule) ) }.toList
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

sealed trait DerivationTree {
  def strings: IO[List[String]]
}
object DerivationTree {
  val executor = ExecutionContext.fromExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
  implicit val contextShift = IO.contextShift(executor)
  class Node(nodes: List[DerivationTree]) extends DerivationTree {
    // def strings = nodes( _.strings ).map{ listOfList =>
    //   listOfList.reduce( (a, b) => a.flatMap(astr => b.map( bstr => astr + bstr )) )
    // }
    def strings = ???
  }
  class SymbolNode(symb: Char) extends DerivationTree {
    def strings = IO.pure(List(symb.toString))
  }
  class Stop extends DerivationTree {
    def strings = IO.pure(List.empty[String])
  }

  def generate(grammatic: List[(NonTerminal, List[Symbol])]): DerivationTree = 
    generate(grammatic.groupBy( _._1 ).map{case (nt, list) => (nt, list.map( _._2 )) })

  def generate(grammatic: Map[NonTerminal, List[List[Symbol]]]): DerivationTree = ???
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
