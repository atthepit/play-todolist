package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task

object Tasks extends Controller {

  val taskForm = Form(
    tuple(
      "label" -> nonEmptyText,
      "user"  -> nonEmptyText
    )
  )

  def index = Action {
    Ok(Json.toJson(Task.all()))
  }

  def create = Action { implicit request => 
    taskForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      task => {
        val label = task._1
        val user = task._2
        try { 
          val id = Task.create(label, user)
          val task = Task.find(id)
          Created(Json.toJson(task)).withHeaders(LOCATION -> routes.Tasks.details(id).url)
        } catch {
          case e: Exception => NotFound(Json.toJson(user))
        }
      }
    )
  }

  def details(id : Long) = Action {
    var task = Task.find(id)
    if(task.isEmpty) {
      NotFound(Json.obj())
    } else {
      Ok(Json.toJson(task))
    }
  }

  def delete(id : Long) = Action {
    try { 
      Task.delete(id)
      NoContent
    } catch {
      case e: Exception => InternalServerError
    }
  }

}