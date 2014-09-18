package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task

object Tasks extends Controller {

  val taskForm = Form(
    "label" -> nonEmptyText
  )

  def index = Action {
    Ok(Json.toJson(Task.all()))
  }

  def create = Action { implicit request => 
    taskForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      label => {
        val id = Task.create(label)
        Redirect(routes.Tasks.details(id))
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