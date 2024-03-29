package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import tellmemore.userfacts.{UserFact, UserFactModel}
import tellmemore.events.EventModel


/**
 * Controller that contains in itself all the possible handlers
 * for metadata needed for client application.
 */
case class MetadataService(eventModel: EventModel, factsModel: UserFactModel) extends Controller {
  import UserFact._

  implicit val factsWrites = new Writes[UserFact] {
    def writes(f: UserFact): JsValue = {
      val factType = f match {
        case StringFact(_,_,_) => "String"
        case NumericFact(_,_,_) => "Numeric"
      }
      Json.obj(
        "name" -> f.name,
        "fact_type" -> factType
      )
    }
  }

  def index = Action {
    // TODO get email by auth token of request (discuss with Misha how to better do for now)
    val facts = factsModel.getUserFactsForClient("bestclient@example.com").values.toList
    val events = eventModel.getEventNames("bestclient@example.com")
    Ok(Json.obj("facts" -> Json.toJson(facts),
                "events" -> Json.toJson(events)))
  }
}



