import org.scalatest.FreeSpec

class StringsGrammaticSpec extends FreeSpec {
    object A extends NonTerminal {
        val render = "A"
    }
    object B extends NonTerminal {
        val render = "B"
    }
    val a = Terminal('a')
    val b = Terminal('b')
    val c = Terminal('c')
    "from grammatic: \nA -> aaB\nB -> cc\nB -> bb" - {
        val stringsGrammatic = StringsGrammatic(List(A -> List(a, a, B), B -> List(c, c), B -> List(b, b)))
        "test generate min = 1, max = 4" in {
            val test = stringsGrammatic.flatMap( _.apply(1, 4) ).unsafeRunSync()
            assert(List("aabb", "aacc").sorted == test.sorted)
        }
        "test generate min = 4, max = 4" in {
            val test = stringsGrammatic.flatMap( _.apply(1, 4) ).unsafeRunSync()
            assert(List("aabb", "aacc").sorted == test.sorted)
        }
        "test generate min = 0, max = 3" in {
            val test = stringsGrammatic.flatMap( _.apply(0, 3) ).unsafeRunSync()
            assert(List.empty[String] == test)
        }
        "test generate min = 5, max = 10" in {
            val test = stringsGrammatic.flatMap( _.apply(5, 10) ).unsafeRunSync()
            assert(List.empty[String] == test)
        }
        
    }
    def bruteForce( alphabet: List[Char], size: Int ): List[String] = List.fill(size)(alphabet.map(_.toString)).reduce( (a, b) => a.flatMap(ac => b.map(bc => ac + bc)) )
    "from grammatic: \nA -> aaB\nB -> ccB\nB -> bb" - {
        val stringsGrammatic = StringsGrammatic(List(A -> List(a, a, B), B -> List(c, c, B), B -> List(b, b)))
        "test generate min 0 max 24" in {
            val test = stringsGrammatic.flatMap( _.apply(0, 24) ).unsafeRunSync()
            assert(List("aabb", "aaccbb", "aaccccbb", "aaccccccbb", "aaccccccccbb", "aaccccccccccbb", "aaccccccccccccbb", "aaccccccccccccccbb", "aaccccccccccccccccbb", "aaccccccccccccccccccbb", "aaccccccccccccccccccccbb").sorted == test.sorted)
        }
    }
    val testGrammatic = Main.regularGrammatic(List('a', 'b', 'c'), "aa", "cba", 3, RightLinear)
    s"from grammatic: \n${testGrammatic.map(a => s"${a._1.render} -> ${a._2.map(_.render).mkString("")}").mkString("\n")}" - {
        val stringsGrammatic = StringsGrammatic(testGrammatic)
        "test generate min 0 max 10" in {
            val test = stringsGrammatic.flatMap( _.apply(0, 10) ).unsafeRunSync()
            assert((List("aaacba", "aabcba", "aaccba") ++ bruteForce(List('a', 'b', 'c'), 4).map( "aa"+ _ +"cba" )).sorted == test.sorted)
        }
    }
}
