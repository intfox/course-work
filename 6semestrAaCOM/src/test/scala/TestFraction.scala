import org.scalatest.FlatSpec


class TestFraction extends FlatSpec {
  import Fraction.fractionalFraction
  import Fractional.Implicits._

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

}
