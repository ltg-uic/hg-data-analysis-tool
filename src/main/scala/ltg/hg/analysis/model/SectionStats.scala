package ltg.hg.analysis.model

import spray.json.{JsValue, DefaultJsonProtocol}

/**
 * Created by tebemis on 5/27/14.
 */
case class SectionStats(_id: JsValue, bout_stats: JsValue, user_stats: List[JsValue],
                        run_id: String, habitat_configuration: String, bout_id : String)

object SectionStatsProtocol extends DefaultJsonProtocol {
  implicit val sectionStatsFormat = jsonFormat6(SectionStats)
}
