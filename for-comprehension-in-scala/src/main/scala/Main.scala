object Main extends App {
  def sumWithFor(): Int = {
    var sum = 0

    for (i <- (1 until 10)) sum += i

    return sum
  }
  def sumWithHOF(): Int = {
    var sum = 0

    (1 until 10).foreach(i => sum += i)

    return sum
  }

  def filteringWithFor() = for {
    i <- 1 to 10
    if i % 2 == 0
  } yield i
  def filteringWithHOF() = (1 to 10).filter(i => i % 2 == 0)

  def mappingWithFor() = for (i <- 1 to 5) yield i * 2
  def mappingWithHOF() = (1 to 5).map(i => i * 2)

  // Sample code for flatMap
  def quotient(a: Int, b: Int) = if (b == 0) None else Some(a / b)

  def flatMapWithFor(list: List[(Int, Int)]) = for {
    tuple <- list
    res <- quotient(tuple._1, tuple._2)
  } yield res
  def flatMapWithHOF(list: List[(Int, Int)]) =
    list.flatMap(tuple => quotient(tuple._1, tuple._2))
}
