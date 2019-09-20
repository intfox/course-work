case class Fraction(num: Int, denom: Int) {
  override def toString: String = s"$num/$denom"
}

object Fraction {
  def apply(num: Int, denom: Int): Fraction = {
    val gcdFraction = math.abs(gcd(num, denom))
    new Fraction((if (denom < 0) -num else num)/gcdFraction, (if (denom < 0) -denom else denom)/gcdFraction)
  }

  def apply(num: Int): Fraction = new Fraction(num, 1)

  object Implicits {
    implicit val fractional: Fractional[Fraction] = new Fractional[Fraction] {
      override def div(x: Fraction, y: Fraction): Fraction = times(x, Fraction(y.denom, y.num))
      override def plus(x: Fraction, y: Fraction): Fraction = Fraction(x.num * y.denom + y.num * x.denom, x.denom * y.denom)
      override def minus(x: Fraction, y: Fraction): Fraction = Fraction(x.num * y.denom - y.num * x.denom, x.denom * y.denom)
      override def times(x: Fraction, y: Fraction): Fraction = Fraction(x.num * y.num, x.denom * y.denom)
      override def negate(x: Fraction): Fraction = Fraction(-x.num, x.denom)
      override def fromInt(x: Int): Fraction = Fraction(x)
      override def toInt(x: Fraction): Int = x.num / x.denom
      override def toLong(x: Fraction): Long = toInt(x).toLong
      override def toFloat(x: Fraction): Float = x.num.toFloat / x.denom.toFloat
      override def toDouble(x: Fraction): Double = x.num.toDouble / x.denom.toDouble
      override def compare(x: Fraction, y: Fraction): Int = x.num * y.denom - y.num * x.denom
    }
  }

  @scala.annotation.tailrec
  private def gcd(a: Int, b: Int): Int = b match {
    case 0 => a
    case _ => gcd(b, a % b)
  }
}
