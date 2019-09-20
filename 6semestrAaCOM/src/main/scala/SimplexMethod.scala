import Fractional.Implicits._
import scala.reflect.ClassTag

object SimplexMethod {
  import MatrixFractionalSyntax._

  def SimplexMethod[T: Fractional](matrix: List[List[T]]): List[List[T]] = {
    matrix
  }

  /** Метод Жордана-Гауса для нахождения опорного решения
   *
   *  @param matrix матрица
   *  @return индексы базисных переменных и мартица опорного решения
   */
  def GaussJordanMethod[T: Fractional : ClassTag](matrix: List[List[T]]) :(List[Int], List[List[T]]) = {
    val arrayMatrix = matrix.map( _.toArray ).toArray
    val arrayBasicIndex = Array.fill(arrayMatrix.length)(0)
    //прямой ход Гауса
    for (indexLineBasic <- arrayMatrix.indices) {
      val indexColumnBasic = arrayMatrix(indexLineBasic).indexWhere( _ != implicitly[Fractional[T]].fromInt(0) )
      arrayBasicIndex(indexLineBasic) = indexColumnBasic
      for (indexLine <- (indexLineBasic + 1) until arrayMatrix.length)
        arrayMatrix(indexLine) = arrayMatrix(indexLine) - arrayMatrix(indexLineBasic) * (arrayMatrix(indexLine)(indexColumnBasic) / arrayMatrix(indexLineBasic)(indexColumnBasic))
    }

    //обратный ход Гауса
    for {
      (indexLineBasic, indexColumnBasic) <- arrayMatrix.indices.reverse.zip(arrayBasicIndex.reverse)
      indexLine <- (indexLineBasic-1).to(0, -1)
    } arrayMatrix(indexLine) = arrayMatrix(indexLine) - arrayMatrix(indexLineBasic) * (arrayMatrix(indexLine)(indexColumnBasic) / arrayMatrix(indexLineBasic)(indexColumnBasic))

    //приводим базисные переменные к единице
    for {
      (indexLineBasic, indexColumnBasic) <- arrayBasicIndex.zipWithIndex
    } arrayMatrix(indexLineBasic) = arrayMatrix(indexLineBasic) / arrayMatrix(indexLineBasic)(indexColumnBasic)

    (arrayBasicIndex.toList, arrayMatrix.toList.map( _.toList ))
  }

}

object MatrixFractionalSyntax {
  type Line[T] = Array[T]
  type Matrix[T] = Array[Line[T]]

  implicit class LineOps[T : Fractional : ClassTag](line: Line[T]) {
    def +(that: Line[T]): Line[T] = line.zip(that).map{ case (a, b) => a + b }
    def unary_- : Line[T] = line.map( a => -a )
    def -(that: Line[T]): Line[T] = line + (-that)
    def *(numb: T): Line[T] = line.map( _ * numb )
    def /(numb: T): Line[T] = line.map( _ / numb )
  }

  def printMatrix[T](matrix: Seq[Seq[T]]): Unit = println(toStringMatrix(matrix))
  def toStringMatrix[T](matrix: Seq[Seq[T]]): String = matrix.map(str => str.mkString(" ")).mkString("\n")
}
