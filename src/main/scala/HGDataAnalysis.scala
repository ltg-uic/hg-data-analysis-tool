import ltg.hg.analysis.model.{Results, SectionStats, Data, Entry}
import scala.io.Source
import spray.json._
import ltg.hg.analysis.model.EntryProtocol._
import ltg.hg.analysis.model.SectionStatsProtocol._

/**
 * Created by tebemis on 5/26/14.
 */

object HGDataAnalysis extends App {

  // Main script

  print(
    """----------------------------------
      |Hunger Games Data Analysis script!
      |----------------------------------
      |""".stripMargin)

  val run = "hg_fall_13_bzaeds"

  val results = run match {
    case "hg_fall_12_pilot" =>
    case "hg_fall_13_bzaeds" => analyzeBZAEDS
  }

  // Support functions

  def analyzeBZAEDS = {
    val data = importData(run)
    analyzeData(data)
  }


  def importData(run: String) = {

    def importSectionData(section: String) = {
      Source.fromFile("src/main/resources/data/" + run + "/" + section + "_log.json").getLines().map { line =>
        line.parseJson.convertTo[Entry]
      }.toList
    }

    def importStats() = {
      Source.fromFile("src/main/resources/data/" + run + "/stats.json").getLines().map { line =>
        line.parseJson.convertTo[SectionStats]
      }.toList
    }

    Data(
      importSectionData("5ag"),
      importSectionData("5at"),
      importSectionData("5bj"),
      importStats()
    )
  }


  def analyzeData(data: Data): Results = {
    Results()
  }

}
