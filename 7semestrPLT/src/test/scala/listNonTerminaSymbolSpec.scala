import org.scalatest.FreeSpec

class ListNonTermianSymbolSpec extends FreeSpec {
    "test generate" in {
        assert(Stream("A", "B", "C", "D", "E") == Main.listNonTerminalSymbol.take(5).map( _.render ) )
    }    
}