package ltg.hg.analysis.model

import spray.json.{JsValue, DefaultJsonProtocol}

/**
 * Created by tebemis on 5/27/14.
 */
case class Entry(_id: JsValue, event: String, payload: JsValue)

object EntryProtocol extends DefaultJsonProtocol {
  implicit val entryFormat = jsonFormat3(Entry)
}
