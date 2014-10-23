package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task
import models.User

import java.util.{Date}

object Tasks extends Controller {

  val taskForm = Form(
    tuple(
      "label" -> nonEmptyText,
      "dueTo" -> optional(date)
    )
  )

  def index = Action {
    Ok(Json.toJson(Task.all()))
  }

  def userTasks(user: String) = Action {
    if(User.exists(user)) {
      Ok(Json.toJson(Task.all(user)))
    } else {
      NotFound
    }
  }

  def create(user: String) = Action { implicit request => 
    taskForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      taskForm => {
        try { 
          if(User.exists(user)) {
            var label : String = taskForm._1
            var dueTo : Option[Date] = taskForm._2
            val id = Task.create(label, user, dueTo)
            val task = Task.find(id)
            Created(Json.toJson(task)).withHeaders(LOCATION -> routes.Tasks.details(id).url)
          } else {
            NotFound
          }
        } catch {
          case e: Exception => InternalServerError(Json.toJson(e.getMessage()))
        }
      }
    )
  }

  def details(id : Long) = Action {
    var task = Task.find(id)
    if(task.isEmpty) {
      NotFound
    } else {
      Ok(Json.toJson(task))
    }
  }

  def delete(id : Long) = Action {
    if(Task.delete(id)){
      NoContent
    } else {
      NotFound
    }
  }

  def expired(user: String) = Action {
    if(User.exists(user)){
      Ok(Json.toJson(Task.expired(Some(user))))
    } else {
      NotFound
    }
  }

  def expiresInYear(year: Int) = Action {
    Ok(Json.toJson(Task.expiresInYear(year)))
  }

  def expiresInMonth(year: Int, month: Int) = Action {
    Ok(Json.toJson(Task.expiresInMonth(year, month)))
  }

  def expiresInDay(year: Int, month: Int, day: Int) = Action {
    Ok(Json.toJson(Task.expiresInDay(year, month, day)))
  }
}