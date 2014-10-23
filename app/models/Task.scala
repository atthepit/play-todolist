package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.{Date}
import java.text.SimpleDateFormat

case class Task(id: Long, label: String, user: String, dueTo: Option[Date] = None)

object Task {

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

  def all(user: String) : List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task where user_login = {user}").on(
      'user -> user
    ).as(task *)
  }

  def create(label: String, user: String, dueTo: Option[Date] = None) : Long =  {
    DB.withConnection { implicit c =>
      val id: Option[Long]  = 
        SQL("insert into task (label, user_login, due_to) values ({label}, {user}, {dueTo})").on(
          'label -> label,
          'user  -> user,
          'dueTo -> dueTo
        ).executeInsert()
        
      id.getOrElse(-1)
    }
  }

  def find(id: Long) : Option[Task] = DB.withConnection { implicit c =>
    SQL("select * from task where id = {id}").on(
      'id -> id
    ).as(Task.task.singleOpt)
  }
  
  def delete(id: Long) : Boolean = {
    DB.withConnection { implicit c =>
      var deleted = SQL("delete from task where id = {id}").on(
        'id -> id
      ).executeUpdate()
      deleted match {
        case 1 => true
        case _ => false
      }
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
    var today = formatter.format(new Date())

    implicit c =>
      SQL("select * from task where user_login = {user} and due_to < {today}").on(
        'user -> user,
        'today -> today
      ).as(task *)
  }
  def expiresInYear(year: Int) : List[Task] = DB.withConnection {
    var minDate = formatter.parse(year + "/" + 1 + "/" + 1);
    var maxDate = formatter.parse(year + "/" + 12 + "/" + 31);

    implicit c =>
      SQL("select * from task where due_to > {minDate} and due_to < {maxDate}").on(
        'minDate -> minDate,
        'maxDate -> maxDate
      ).as(task *)
  }

  def expiresInMonth(year: Int, month: Int) : List[Task] = DB.withConnection {
    var minDate = formatter.parse(year + "/" + month + "/" + 1);
    var maxDate = formatter.parse(year + "/" + month + "/" + getMaxDayOfMonth(month));

    implicit c =>
      SQL("select * from task where due_to > {minDate} and due_to < {maxDate}").on(
        'minDate -> minDate,
        'maxDate -> maxDate
      ).as(task *)
  }

  def expiresInDay(year: Int, month: Int, day: Int) : List[Task] = DB.withConnection {
    var dateStr = year + "-" + month + "-" + day
    var date = Some(formatter.parse(dateStr))
    implicit c =>
      SQL("select * from task where due_to = {date}").on(
        'date -> date
      ).as(task *)
  }

  private def getMaxDayOfMonth(month: Int) : Int = {
    if(month == 4 || month == 6 || month == 9 || month == 11) {
      return 30
    } else if(month == 2){
      return 28
    } else {
      return 31
    }
  }

  val formatter = new SimpleDateFormat("yyyy-MM-dd")
  val dateWrite = Writes.dateWrites("yyyy-MM-dd")
  implicit val taskWrites : Writes[Task] = ( 
    (JsPath \ "id").write[Long] and
    (JsPath \ "label").write[String] and
    (JsPath \ "user").write[String] and
    (JsPath \ "due_to").writeNullable[Date](dateWrite)
  )(unlift(Task.unapply))

}