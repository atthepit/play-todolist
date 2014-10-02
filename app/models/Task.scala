package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import java.util.{Date}

case class Task(id: Long, label: String, user: String, dueTo: Option[Date])

object Task {

  val parser : RowParser[Task] = {
    get[Long]("task.id") ~
    get[String]("task.label") ~
    get[String]("task.user_login") ~
    get[Option[Date]]("task.due_to") map {
      case id~label~user~dueTo => Task(id, label, user, dueTo)
    }
  }

  val task = {
    get[Long]("id") ~ 
    get[String]("label") ~
    get[String]("user_login") ~
    get[Option[Date]]("task.due_to") map {
      case id~label~user~dueTo => Task(id, label, user, dueTo)
    }
  }

  def all() : List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task").as(task *)
  }

  def create(label: String, user: String, dueTo: Option[Date]) : Long =  {
    DB.withConnection { implicit c =>
      SQL("insert into task (label, user_login, due_to) values ({label}, {user}, {dueTo})").on(
        'label -> label,
        'user  -> user,
        'dueTo -> dueTo
      ).executeInsert()
    } match {
        case Some(long) => long // The Primary Key
      }
  }

  def find(id: Long) : Option[Task] = DB.withConnection { implicit c =>
    SQL("select * from task where id = {id}").on(
      'id -> id
    ).as(parser.singleOpt)
  }
  
  def delete(id: Long) {
    DB.withConnection { implicit c =>
      SQL("delete from task where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

  def findByUser(user: String) : List[Task] = DB.withConnection { 
    implicit c =>
      SQL("select * from task where user_login = {user}").on('user -> user).as(task *)
  }

  def exists(id: Long) : Boolean = {
    return !Task.find(id).isEmpty
  }

  def expired(user: Option[String]) : List[Task] = DB.withConnection {
    var today = new Date()
    implicit c =>
      SQL("select * from task where user_login = {user} and due_to < {today}").on(
        'user -> user,
        'today -> today
      ).as(task *)
  }

  implicit val taskWrites = new Writes[Task] {
    def writes(task : Task) : JsValue = {
      var json = Json.obj(
        "id" -> task.id,
        "label" -> task.label,
        "user" -> task.user
      )

      if(!task.dueTo.isEmpty) {
        json = json ++ Json.obj("due_to" -> task.dueTo.get.toString)
      }

      return json
    }
  }
}