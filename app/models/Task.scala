package models
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

case class Task(id: Long, label: String)

object Task {

  val parser : RowParser[Task] = {
    get[Long]("task.id") ~
    get[String]("task.label") map {
      case id~label => Task(id, label)
    }
  }
  val task = {
    get[Long]("id") ~ 
    get[String]("label") map {
      case id~label => Task(id, label)
    }
  }

  def all() : List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task").as(task *)
  }

  def create(label: String) : Long =  {
    DB.withConnection { implicit c =>
      SQL("insert into task (label) values ({label})").on(
        'label -> label
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

  implicit val taskWrites = new Writes[Task] {
    def writes(task : Task) = Json.obj(
      "id" -> task.id,
      "label" -> task.label
    )
  }
}