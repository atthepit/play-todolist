package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models.Task
import models.User

import java.util.{Date}

object Users extends Controller { 
  def tasks(user: String) = Action {
    if(User.exists(user)) {
      Ok(Json.toJson(Task.all(user)))
    } else {
      NotFound
    }
  }
}