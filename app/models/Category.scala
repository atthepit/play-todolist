package models
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Category(Id: Long, name: String, user: String)

object Category {
  def create(name: String, user: String) : Long = { 
    DB.withConnection { implicit c =>
      val id: Option[Long]  = 
        SQL("insert into category (name, user_login) values ({name}, {user})").on(
          'name -> name,
          'user  -> user
        ).executeInsert()
        
      id.getOrElse(-1)
    }
  }

  def tasks(category: Long) : List[Task] = {
    DB.withConnection { implicit conn =>
      SQL("select * from task where category = {category}").on(
        'category -> category
      ).as(Task.task *)
    }
  }
}