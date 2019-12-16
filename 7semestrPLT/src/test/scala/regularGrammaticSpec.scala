import org.scalatest.FreeSpec

class regularGrammaticSpec extends FreeSpec {
    
    "test grammatic" - {
        val testGrammatic = Main.regularGrammatic(List('a', 'b', 'c'), "aa", "cba", 3, RightLinear)
        info("Generated grammatic: \n" +  testGrammatic.map(a => s"${a._1.render} -> ${a._2.map(_.render).mkString("")}").mkString("\n") )
        "is not empty" in {
            assert(!testGrammatic.isEmpty)
        }
        "is regular" in {
            assert(testGrammatic.map( _._2.count( _ match {
                case _: NonTerminal => true
                case _: Terminal => false
            } ) ).forall( n => n == 1 || n == 0) ) 
        }
        "is right linear" in {
            def test( symbols: List[Symbol] ): Boolean = symbols match {
                case Nil => true
                case (_: NonTerminal) :: Nil => true
                case (_: Terminal) :: tail => test(tail)
                case _ => false  
            } 
            assert(testGrammatic.map( a => test(a._2) ).forall( identity _ ))
        }
    }

    "left test grammatic" - {
        val testGrammatic = Main.regularGrammatic(List('a', 'b', 'c'), "aa", "cba", 3, LeftLinear)
        info("Generated grammatic: \n" +  testGrammatic.map(a => s"${a._1.render} -> ${a._2.map(_.render).mkString("")}").mkString("\n") )
        "is not empty" in {
            assert(!testGrammatic.isEmpty)
        }
        "is regular" in {
            assert(testGrammatic.map( _._2.count( _ match {
                case _: NonTerminal => true
                case _: Terminal => false
            } ) ).forall( n => n == 1 || n == 0) ) 
        }
        "is left linear" in {
            def isTerminal = (_: Symbol) match {
                case _: Terminal => true
                case _: NonTerminal => false
            }
            assert(testGrammatic.map{ case (_, rule) => rule.tail.forall(isTerminal) }.forall( identity _ ))
        }
    }
}