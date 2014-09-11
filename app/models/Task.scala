import models

case class Task(id: Long, label: string)

object Task {
   def all() : List[Task] = Nil
   def create(label: string) {}
   def delete(id: Long) {}
}