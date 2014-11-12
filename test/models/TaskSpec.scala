import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.Task
import java.util.{Date}
import java.text.SimpleDateFormat

class TaskSpec() extends Specification {
  def fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase())

  val today = Task.formatter.parse(Task.formatter.format(new Date()))
  val todayArray = Task.formatter.format(today).split("-")
  val thisYear = todayArray(0).toInt
  val thisMonth = todayArray(1).toInt
  val thisDay = todayArray(2).toInt

  val aYearAgo = Task.formatter.parse((thisYear - 1) + "-" + thisMonth + "-" + thisDay)

  var anonymousTask = new Task(-1, "Tarea anonima", "anonymous", None)
  var pedroTask = new Task(-1, "Tarea de pedro", "pedro", None)
  var sergioTask = new Task(-1, "Tarea de sergio", "sergio", None)
  var pabloTask = new Task(-1, "Tarea de pablo", "pablo", None)

  var anonymousTodayTask = new Task(-1, "Tarea anonima con fecha", "anonymous", Some(today))
  var pedroTodayTask = new Task(-1, "Tarea de pedro con fecha", "pedro", Some(today))
  var sergioTodayTask = new Task(-1, "Tarea de sergio con fecha", "sergio", Some(today))
  var pabloTodayTask = new Task(-1, "Tarea de pablo con fecha", "pablo", Some(today))

  var anonymousExpiredTask = new Task(-1, "Tarea anonima de hace un año", "anonymous", Some(aYearAgo))
  var pedroExpiredTask = new Task(-1, "Tarea de pedro con fecha de hace un año", "pedro", Some(aYearAgo))
  var sergioExpiredTask = new Task(-1, "Tarea de sergio con fecha de hace un año", "sergio", Some(aYearAgo))
  var pabloExpiredTask = new Task(-1, "Tarea de pablo con fecha de hace un año", "pablo", Some(aYearAgo))

  var tasks : List[Task] = List()

  def setUp() = {
    anonymousTask = Task.save(anonymousTask)
    pedroTask = Task.save(pedroTask)
    sergioTask = Task.save(sergioTask)
    pabloTask = Task.save(pabloTask)

    anonymousTodayTask = Task.save(anonymousTodayTask)
    pedroTodayTask = Task.save(pedroTodayTask)
    sergioTodayTask = Task.save(sergioTodayTask)
    pabloTodayTask = Task.save(pabloTodayTask)

    anonymousExpiredTask = Task.save(anonymousExpiredTask)
    pedroExpiredTask = Task.save(pedroExpiredTask)
    sergioExpiredTask = Task.save(sergioExpiredTask)
    pabloExpiredTask = Task.save(pabloExpiredTask)

    tasks = List(
      anonymousTask, pedroTask, sergioTask, pabloTask, 
      anonymousTodayTask, pedroTodayTask, sergioTodayTask, pabloTodayTask,
      anonymousExpiredTask, pedroExpiredTask, sergioExpiredTask, pabloExpiredTask
    ) 
  }

  def setDown() = { }

  "Create" should {
    "return the id of the new task" in new WithApplication(fakeApp) {
      setUp()
      var id = Task.create("Hola mundo", "pedro")
      id mustNotEqual -1
    }

    "add anonymous user if no user is specified" in new WithApplication(fakeApp) {
      setUp()
      var id = Task.create("Hola mundo")
      var Some(task) = Task.find(id)

      task.user mustEqual "anonymous"
    }

    "not add a date if it is not specified" in new WithApplication(fakeApp) {
      setUp()
      var id = Task.create("Hola mundo")
      var Some(task) = Task.find(id)

      task.dueTo.isEmpty must beTrue
    }

    "should add a date if specified" in new WithApplication(fakeApp) {
      setUp()
      var id = Task.create("Hola mundo", "pedro", Some(new Date()))
      var Some(task) = Task.find(id)

      task.dueTo.isEmpty must beFalse
    }
  }
  
  "Searching" should {
    "return a task when searching by id" in new WithApplication(fakeApp) {
      setUp()
      Task.find(anonymousTask.id) mustEqual Some(anonymousTask)
    }
    "return an empty task when the id doesn't exists" in new WithApplication(fakeApp) {
      setUp()
      Task.find(-1).isEmpty must beTrue
    }
    "return all tasks" in new WithApplication(fakeApp) {
      setUp()
      Task.all().length mustEqual tasks.length
    }
    "return all tasks from a user" in new WithApplication(fakeApp) {
      setUp()
      val pedroTasks = tasks.filter(t => t.user.equals("pedro")).length
      Task.all("pedro").length mustEqual pedroTasks
    }
    "return all tasks that have already expired" in new WithApplication(fakeApp) {
      setUp()
      val expiredTasks = tasks.filter(t => !t.dueTo.isEmpty && t.dueTo.get.compareTo(today) < 0).length
      Task.expired(None).length mustEqual expiredTasks
    }

    "return all user tasks that have already expired" in new WithApplication(fakeApp) {
      setUp()
      val pedroExpiredTasks = tasks.filter(t => t.user.equals("pedro") && !t.dueTo.isEmpty && t.dueTo.get.compareTo(today) < 0).length
      Task.expired(Some("pedro")).length mustEqual pedroExpiredTasks
    }

    "return all tasks that expire on a certain year" in new WithApplication(fakeApp) {
      setUp()
      val lastYear = Task.formatter.format(aYearAgo).split("-")(0).toInt

      val tasksExpiredLastYear = tasks.filter(
        t => 
          !t.dueTo.isEmpty && 
          Task.formatter.format(t.dueTo.get).split("-")(0).toInt == lastYear
      ).length
      
      Task.expiresInYear(lastYear).length mustEqual tasksExpiredLastYear
    }

    "return all tasks that expire on a certain month" in new WithApplication(fakeApp) {
      setUp()

      val tasksExpiringThisMonth = tasks.filter( 
        t => 
          !t.dueTo.isEmpty && 
          Task.formatter.format(t.dueTo.get).split("-")(1).toInt == thisMonth && 
          Task.formatter.format(t.dueTo.get).split("-")(0).toInt == thisYear
      ).length

      Task.expiresInMonth(thisYear, thisMonth).length mustEqual tasksExpiringThisMonth
    }

    "return all tasks that expire on a certain day" in new WithApplication(fakeApp) {
      setUp()

      val tasksExpiringToday = tasks.filter( 
        t => 
          !t.dueTo.isEmpty && 
          Task.formatter.format(t.dueTo.get).split("-")(2).toInt == thisDay && 
          Task.formatter.format(t.dueTo.get).split("-")(1).toInt == thisMonth && 
          Task.formatter.format(t.dueTo.get).split("-")(0).toInt == thisYear
      ).length

      Task.expiresInDay(thisYear, thisMonth, thisDay).length mustEqual tasksExpiringToday
    }
  }

  "Deleting" should {
    "return true if the task was deleted" in new WithApplication(fakeApp){
      setUp()
      Task.delete(anonymousTask.id) must beTrue
    }
    "return false if the task doesn't exists" in new WithApplication(fakeApp){
      setUp()
      Task.delete(-1) must beFalse
    }
  }

  /*"A task"  should {
    "be editable" in new WithApplication(fakeApp) {
      setUp()
      newTask = Task.update(anonymousTask.id, "Hello world", anonymousTask.user, anonymousTask.dueTo);
      newTask.label mustEqual "Hello world"
    }
  }*/
}