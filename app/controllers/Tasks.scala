package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models.Task
import models.User

import java.util.{Date}
case class TaskData(label: String, dueTo: Option[Date] = None, category: Option[Long] = None)

object Tasks extends Controller {

  val taskForm = Form(
    mapping(
      "label" -> nonEmptyText,
      "dueTo" -> optional(date),
      "category" -> optional(longNumber)
    )(TaskData.apply)(TaskData.unapply)
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

  def create(user: String, category: Long = -1) = Action { implicit request => 
    taskForm.bindFromRequest.fold(
      formWithErrors => BadRequest(formWithErrors.errorsAsJson),
      taskForm => {
        try { 
          if(User.exists(user)) {
            var label : String = taskForm.label
            var dueTo : Option[Date] = taskForm.dueTo
            val categoryId : Option[Long] = if(category == -1) { taskForm.category } else { Some(category) }
            val id = Task.create(label, user, dueTo, categoryId)
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

  def update(id: Long) = Action { implicit request => 
    taskForm.bindFromRequest.fold (
      formWithErrors => BadRequest( formWithErrors.errorsAsJson ),
      taskForm => {
        var task : Option[Task] = Task.find(id)
        if(task.isEmpty) {
          NotFound
        } else {
          var label : String = taskForm.label
          var dueTo : Option[Date] = if (taskForm.dueTo.isEmpty) task.get.dueTo; else taskForm.dueTo;
          var category : Option[Long] = if (taskForm.category.isEmpty) task.get.category; else taskForm.category
          try { 
            Ok(Json.toJson(Task.save(new Task(task.get.id, label, task.get.user, dueTo, category))))
          } catch {
            case e: Exception => InternalServerError
          }
        }
      }
    )
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