package models
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Task(id: Long, label: String, user: String)

object Task {

  val parser : RowParser[Task] = {
    get[Long]("task.id") ~
    get[String]("task.label") ~
    get[String]("task.user") map {
      case id~label~user => Task(id, label, user)
    }
  }
  val task = {
    get[Long]("id") ~ 
    get[String]("label") ~
    get[String]("user") map {
      case id~label~user => Task(id, label, user)
    }
  }

  def all() : List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task").as(task *)
  }

  def create(label: String, user: String) : Long =  {
    DB.withConnection { implicit c =>
      SQL("insert into task (label, user) values ({label}, {user})").on(
        'label -> label,
        'user  -> user
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
    SQL("select * from task where user = {user}").on('user -> user).as(task *)
  }

  def exists(id: Long) : Boolean = {
    return !Task.find(id).isEmpty
  }

  implicit val taskWrites = new Writes[Task] {
    def writes(task : Task) = Json.obj(
      "id" -> task.id,
      "label" -> task.label,
      "user" -> task.user
    )
  }
}