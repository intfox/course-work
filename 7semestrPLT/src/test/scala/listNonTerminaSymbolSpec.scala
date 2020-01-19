import org.scalatest.FreeSpec

class ListNonTermianSymbolSpec extends FreeSpec {
    "test generate" in {
        assert(Stream("A", "B", "C", "D", "E") == (new ServiceImpl).listNonTerminalSymbol.take(5).map( _.render ) )
    }
}