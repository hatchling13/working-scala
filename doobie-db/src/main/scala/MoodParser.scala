case class Mood(name: String, score: Int)

object MoodParser {
    def parseInput(score: String) =
        score match {
        case "10" => Mood("GOOD", 10)
        case "5" => Mood("SOSO", 5)
        case "0" => Mood("BAD", 0)
        case _ => Mood("NONE", -1)
  }

}
