import org.scalatest.FlatSpec
import Fraction.Implicits._

class TestSimplexMethod extends FlatSpec {

  "Simplex method" should "correct 1 " in {
    val matrix = List(
      List(2, 1, -1, 0, 0, 10),
      List(3, 4, 0, -1, 0, 30),
      List(3, 8, 0, 0, -1, 42)
    ).map( _.map( Fraction(_) ) )
    val C = List(-10, -3, 0, 0, 0, 0).map( Fraction(_) )

    val result = SimplexMethod.SimplexMethod(matrix, C, List(2, 3, 4))
    val expected = (List(0, 10, 0, 10, 38).map(Fraction(_)), Fraction(-30))
    assert(expected == result)
  }

  it should "correct 2" in { //как же мне лень писать одекватные названия тестов
    val matrix = List(
      List(2, 5, -1, 0, 0, 16),
      List(4, 1, 0, -1, 0, 9),
      List(3, 2, 0, 0, -1, 13)
    ).map( _.map( Fraction(_) ) )
    val C = List(-6, -1, 0, 0, 0, 0).map( Fraction(_) )

    val result = SimplexMethod.SimplexMethod(matrix, C, List(2, 3, 4))
    val expected = (List(0, 9, 29, 0, 5).map(Fraction(_)), Fraction(-9))
    assert(expected == result)
  }

}
