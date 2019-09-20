import org.scalatest.FlatSpec


class TestFraction extends FlatSpec {
  import Fraction.Implicits.fractional
  import Fractional.Implicits._
  import Ordering.Implicits._

  "1/2 + 1/2" should "1/1" in {
    assert(Fraction(1, 2) + Fraction(1, 2) == Fraction(1, 1))
  }

  "1/2 * 1/2" should "1/4" in {
    assert(Fraction(1, 2) * Fraction(1, 2) == Fraction(1, 4))
  }

  "1/2 / 1/2" should "1/1" in {
    assert(Fraction(1, 2) / Fraction(1, 2) == Fraction(1, 1))
  }

  "1/2 - 1/2" should "0/1" in {
    assert(Fraction(1, 2) - Fraction(1, 2) == Fraction(0, 1))
  }

  "1/-2" should "-1/2" in {
    assert(Fraction(1, -2) == Fraction(-1, 2))
  }


  "1/2 > 1/4" should "true" in {
    assert(Fraction(1, 2) > Fraction(1, 4))
  }

  "-1/2 < 1/2" should "true" in {
    assert(Fraction(-1, 2) < Fraction(1, 2))
  }

  "-1/2 - 1/2" should "-1/1" in {
    assert(Fraction(-1, 2) - Fraction(1, 2) == Fraction(-1/1))
  }
}
