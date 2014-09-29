package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task
import models.User

object Tasks extends Controller {

  val taskForm = Form(
    "label" -> nonEmptyText
  )

  def index = Action {
    Ok(Json.toJson(Task.all()))
  }

  def userTasks(user: String) = Action {
    if(User.exists(user)) {
      Ok(Json.toJson(Task.findByUser(user)))
    } else {
      NotFound(Json.toJson(user))
    }
  }

  def create(user: String) = Action { implicit request => 
    taskForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      label => {
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
    if(Task.exists(id)){
      Task.delete(id)
      NoContent
    } else {
      NotFound(Json.obj())
    }
  }

}