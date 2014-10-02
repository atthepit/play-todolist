package models
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import models.Task

case class User(login: String)

object User {

  val parser : RowParser[User] = {
    get[String]("task_user.login") map {
      case login => User(login)
    }
  }

  def find(login: String) : Option[User] = {
    DB.withConnection { implicit c =>
      SQL("select * from task_user where login = {login}").on(
        'login -> login
      ).as(parser.singleOpt)
    }
  }

  def exists(login: String) : Boolean = {
    return !User.find(login).isEmpty
  }
}