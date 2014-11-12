package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

/** Uncomment the following lines as needed **/
import play.api.Play.current
/**
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
**/
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Category


object Categories extends Controller {
  
  val categoriesForm = Form(
    "name" -> nonEmptyText
  )

  def create(user: String) = Action { implicit request =>
    categoriesForm.bindFromRequest.fold (
      formWithErrors => BadRequest( "You need to pass a 'xxx' value!" ),
      name => {
          val id : Long = Category.create(name, user)
          Created(Json.toJson(new Category(id, name, user)))
      }
    )  
  }

  implicit val categoryWrites : Writes[Category] = ( 
    (JsPath \ "id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "user").write[String]
  )(unlift(Category.unapply))
}