import Fractional.Implicits._
import Ordering.Implicits._
import scala.reflect.ClassTag

object SimplexMethod {
  import MatrixFractionalSyntax._

  def SimplexMethod[T: Fractional : ClassTag](matrix: List[List[T]], rowC: List[T], basic: List[Int]): (List[T], T) = {
    implicit val numeric: Numeric[T] = implicitly[Fractional[T]]
    val zero = implicitly[Fractional[T]].zero
    val basicMatrix = GaussJordanMethod(matrix).apply(basic.zipWithIndex.map( _.swap ): _*)

    @scala.annotation.tailrec
    def basicSolution(matrix: List[List[T]], basic: List[Int]): (List[List[T]], List[Int]) = {
      val arrayMatrix = matrix.matrixToArray
      val basicArray = basic.toArray


      if(arrayMatrix.lastColumn.forall( _ >= zero)) (matrix, basic)
      else {
        val basicRow = arrayMatrix.lastColumn.map( mod(_)(numeric) ).indexOf(arrayMatrix.lastColumn.map( mod(_)(numeric) ).max)
        val basicColumn = arrayMatrix(basicRow).dropRight(1).map( mod(_)(numeric) ).indexOf(arrayMatrix(basicRow).dropRight(1).map( mod(_)(numeric) ).max)
        basicArray(basicRow) = basicColumn
        printMatrixWithPaintElem(matrix)(basic.zipWithIndex.map( _.swap ).map{ case(row, column) => (row, column, Console.BLUE)} /*:+ (basicRow, basicColumn, Console.GREEN) */: _*)
        basicSolution(GaussJordanMethod(matrix).apply(basicRow, basicColumn).matrix, basicArray.toList)
      }
    }

    @scala.annotation.tailrec
    def iterate(matrix: List[List[T]], basic: List[Int]): (List[List[T]], List[Int]) = {
      val arrayMatrix = matrix.matrixToArray
      val arrayBasic = basic.toArray

      val delta: Array[T] = (for {
        i <- rowC.indices
      } yield (for{
        (basicRow, basicColumn) <- basic.zipWithIndex.map(_.swap)
      } yield rowC(basicColumn) * arrayMatrix(basicRow)(i)).sum - rowC(i)).toArray

      printMatrixWithPaintElem(matrix :+ delta.toList)(basic.zipWithIndex.map( _.swap ).map{ case(row, column) => (row, column, Console.BLUE) }: _*)
      if (delta.dropRight(1).forall( _ >= zero )) (matrix, basic)
      else {
        val basicColumn = delta.dropRight(1).indexOf(delta.dropRight(1).min)
        val relationshipSimplexColumn = arrayMatrix.lastColumn.zip(arrayMatrix.column(basicColumn).map{
          case number if number <= zero => None
          case number => Some(number)
        }).map{
          case (a, optionB) => optionB.map( a / _ )
        }
        val basicRow = relationshipSimplexColumn.indexOf(Some(relationshipSimplexColumn.flatten.min))

        arrayBasic(basicRow) = basicColumn
        iterate(GaussJordanMethod(matrix).apply(basicRow, basicColumn).matrix, arrayBasic.toList)
      }
    }

    val (basicSolutionMatrix, basicSolutionBasic) = basicSolution(basicMatrix, basic)
    val (resultMatrix, resultBasic) = iterate(basicSolutionMatrix, basicSolutionBasic)
    val resultX = Array.fill(rowC.length - 1)(zero)
    for((indexRow, indexColumn) <- resultBasic.zipWithIndex.map(_.swap)) resultX(indexColumn) = resultMatrix.matrixToArray.lastColumn(indexRow)
    (resultX.toList, resultX.toList.zip(rowC.dropRight(1)).map{ case (a, b) => a * b }.sum)
  }

  /** Метод Жордана-Гауса для нахождения опорного решения
   *
   * @param matrix матрица
   * @return поиск базисного решения методом гауса
   */
  case class GaussJordanMethod[T: Fractional : ClassTag](matrix: List[List[T]]) extends ((Int, Int) => GaussJordanMethod[T]) {

    /** Базисное решение
     *
     * @param basicRow строка базиса
     * @param basicColumn столбец базиса
     * @return метод гауса над результирующей матрицей
     */
    override def apply(basicRow: Int, basicColumn: Int): GaussJordanMethod[T] = {
      val arrayMatrix = matrix.map( _.toArray ).toArray

      for{
        row <- arrayMatrix.indices if row != basicRow
      } arrayMatrix(row) = arrayMatrix(row) - arrayMatrix(basicRow) * (arrayMatrix(row)(basicColumn) / arrayMatrix(basicRow)(basicColumn))

      arrayMatrix(basicRow) = arrayMatrix(basicRow) / arrayMatrix(basicRow)(basicColumn)

      GaussJordanMethod(arrayMatrix.map( _.toList ).toList)
    }

    def apply(basic: (Int, Int)*): List[List[T]] = basic.foldLeft(GaussJordanMethod(matrix)){
      case (matrix, (basicRow, basicColumn)) => matrix(basicRow, basicColumn)
    }.matrix

  }

  import Numeric.Implicits._
  private def mod[T : Numeric](t: T): T = if( t < implicitly[Numeric[T]].zero ) -t else t

}

object MatrixFractionalSyntax {
  type Line[T] = Array[T]
  type Matrix[T] = Array[Line[T]]

  implicit class ListListOps[T : ClassTag](matrix: List[List[T]]) {
      def matrixToArray: Array[Array[T]] = matrix.map( _.toArray ).toArray
  }

  implicit class LineOps[T : Fractional : ClassTag](line: Line[T]) {
    def +(that: Line[T]): Line[T] = line.zip(that).map{ case (a, b) => a + b }
    def unary_- : Line[T] = line.map( a => -a )
    def -(that: Line[T]): Line[T] = line + (-that)
    def *(numb: T): Line[T] = line.map( _ * numb )
    def /(numb: T): Line[T] = line.map( _ / numb )
  }

  implicit class MatrixOps[T : Fractional : ClassTag](matrix: Array[Array[T]]) {
    def column(index: Int): Array[T] = matrix.map( _(index) )
    def lastColumn: Array[T] = matrix.column( matrix(0).length - 1)
  }

  def printMatrix[T](matrix: Seq[Seq[T]]): Unit = println(toStringMatrix(matrix))
  def toStringMatrix[T](matrix: Seq[Seq[T]]): String = matrix.map(str => str.mkString(" ")).mkString("\n")+"\n"
  def printMatrixWithPaintElem[T](matrix: Seq[Seq[T]])(elem: (Int, Int, String)*): Unit = {
    val matrixString = matrix.map( line => line.map( _.toString ))
    println(toStringMatrix(elem.foldLeft(matrixString){
      case (matrixOps, (index_line, index_row, color)) =>
        matrixOps.updated(index_line, matrixOps(index_line).updated(index_row, color + matrixOps(index_line)(index_row) + Console.RESET))
    }))
  }
}