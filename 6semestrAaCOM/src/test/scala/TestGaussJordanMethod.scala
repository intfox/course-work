import org.scalatest.FlatSpec


class TestGaussJordanMethod extends FlatSpec {
  import Fraction.Implicits.fractional
  import Fractional.Implicits._

  val matrix = List(
    List(Fraction(2), Fraction(1), Fraction(-1), Fraction(0), Fraction(0), Fraction(10)),
    List(Fraction(3), Fraction(4), Fraction(0), Fraction(-1), Fraction(0), Fraction(30)),
    List(Fraction(3), Fraction(8), Fraction(0), Fraction(0), Fraction(-1), Fraction(42))
  )

  val expectedResultMatrix = List(
    List(Fraction(1), Fraction(0), Fraction(0), Fraction(-2, 3), Fraction(1, 3), Fraction(6)),
    List(Fraction(0), Fraction(1), Fraction(0), Fraction(1, 4), Fraction(-1, 4), Fraction(3)),
    List(Fraction(0), Fraction(0), Fraction(1), Fraction(-13, 12), Fraction(5, 12), Fraction(5))
  )

  "Gaus Jordan Method" should MatrixFractionalSyntax.toStringMatrix(matrix) + " => " + MatrixFractionalSyntax.toStringMatrix(expectedResultMatrix) in {
    val realResultMatrix = SimplexMethod.GaussJordanMethod(SimplexMethod.GaussJordanMethod(SimplexMethod.GaussJordanMethod(matrix).apply(0, 0).matrix).apply(1, 1).matrix).apply(2, 2).matrix
    assert(expectedResultMatrix == realResultMatrix)
  }

  val matrix2 = List(
    List(Fraction(2), Fraction(1), Fraction(-1), Fraction(0), Fraction(0), Fraction(10)),
    List(Fraction(3), Fraction(4), Fraction(0), Fraction(-1), Fraction(0), Fraction(30)),
    List(Fraction(3), Fraction(8), Fraction(0), Fraction(0), Fraction(-1), Fraction(42)),
    List(Fraction(-10), Fraction(-3), Fraction(0), Fraction(0), Fraction(0), Fraction(0))
  )

  val expectedResultMatrix2 = List(
    List(Fraction(-2), Fraction(-1), Fraction(1), Fraction(0), Fraction(0), Fraction(-10)),
    List(Fraction(-3), Fraction(-4), Fraction(0), Fraction(1), Fraction(0), Fraction(-30)),
    List(Fraction(-3), Fraction(-8), Fraction(0), Fraction(0), Fraction(1), Fraction(-42)),
    List(Fraction(-10), Fraction(-3), Fraction(0), Fraction(0), Fraction(0), Fraction(0))
  )
  it should MatrixFractionalSyntax.toStringMatrix(matrix) + " => " + MatrixFractionalSyntax.toStringMatrix(expectedResultMatrix2) in {
    val realResultMatrix = SimplexMethod.GaussJordanMethod(matrix2).apply(0, 2)(1, 3)(2, 4).matrix
    SimplexMethod.GaussJordanMethod(matrix2).apply((0, 2), (1, 3), (2, 4))
    SimplexMethod.GaussJordanMethod(matrix2).apply(List((0, 2), (1, 3), (2, 4)): _*)
    assert(expectedResultMatrix2 == realResultMatrix)
  }
}
