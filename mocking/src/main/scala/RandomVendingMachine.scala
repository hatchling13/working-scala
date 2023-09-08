import zio._

object RandomVendingMachine extends ZIOAppDefault
 {
  val drinks: List[String] = List("콜라", "콤부차", "물")
  val soda: List[String] = List("콜라", "사이다", "제로콜라", "탄산수")

  def getOne = for {
    n <- Random.nextIntBetween(0, drinks.size)
  } yield drinks(n)

  def warnSoda = for {
    item <- getOne
    result = if (soda.contains(item)) "[삐빅!!! 탄산음료에요...]" else "[좋은 선택입니다]"
  } yield (result)

    def run = for {
      _ <- ZIO.unit
    } yield()
}
