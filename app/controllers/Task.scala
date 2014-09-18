package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task

object Tasks extends Controller {
  
  def index = Action {
    Ok(Json.toJson(Task.all()))
  }

  def newTask = TODO

  def details(id : Long) = TODO

  def delete(id : Long) = TODO

}