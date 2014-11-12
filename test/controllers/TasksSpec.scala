import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import java.util.{Date}
import java.text.SimpleDateFormat
import play.api.libs.json._

class TasksSpec() extends Specification {
  import models.Task

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
    "return created HTTP status and a json of the task if created correctly" in new WithApplication(fakeApp) {
      setUp()
      val result = controllers.Tasks.create("anonymous")(
        FakeRequest().withFormUrlEncodedBody("label" -> "HelloWorld")
      )

      status(result) mustEqual CREATED
      contentAsString(result) must contain("HelloWorld")
    }
    "return not found HTTP status if the user doesn't exist" in new WithApplication(fakeApp) {
      setUp()
      val result = controllers.Tasks.create("usuarioNoExistente")(
        FakeRequest().withFormUrlEncodedBody("label" -> "HelloWorld")
      )

      status(result) mustEqual 404
    }
  }

  "Searching" should {
    "return a json with the task when searching by id" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.details(anonymousTask.id)(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      contentAsString(result) must contain(anonymousTask.label)
      contentAsString(result) must contain(anonymousTask.user)
    }
    "return a list with all tasks" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.index()(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual tasks.length
    }
    "return all tasks from a user" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.userTasks("pedro")(FakeRequest())
      val pedroTasks = tasks.filter(t => t.user.equals("pedro")).length

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual pedroTasks
    }
    "return not found HTTP status if the task doesn't exist" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.details(-1)(FakeRequest())

      status(result) mustEqual 404
    }
    "return not found HTTP status if the user doesn't exist" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.userTasks("usuarioNoExistente")(FakeRequest())

      status(result) mustEqual 404
    }
    "return a list with a user expired tasks" in new WithApplication(fakeApp) {
      setUp()
      val pedroExpiredTasks = tasks.filter(t => t.user.equals("pedro") && !t.dueTo.isEmpty && t.dueTo.get.compareTo(today) < 0).length
      val result = controllers.Tasks.expired("pedro")(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual pedroExpiredTasks
    }
    "return a list with tasks that expires in a certain year" in new WithApplication(fakeApp) {
      setUp()
      val lastYear = Task.formatter.format(aYearAgo).split("-")(0).toInt

      val tasksExpiredLastYear = tasks.filter(
        t => 
          !t.dueTo.isEmpty && 
          Task.formatter.format(t.dueTo.get).split("-")(0).toInt == lastYear
      ).length

      val result = controllers.Tasks.expiresInYear(lastYear)(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual tasksExpiredLastYear
    }
    "return all tasks that expire on a certain month" in new WithApplication(fakeApp) {
      setUp()

      val tasksExpiringThisMonth = tasks.filter( 
        t => 
          !t.dueTo.isEmpty && 
          Task.formatter.format(t.dueTo.get).split("-")(1).toInt == thisMonth && 
          Task.formatter.format(t.dueTo.get).split("-")(0).toInt == thisYear
      ).length

      val result = controllers.Tasks.expiresInMonth(thisYear, thisMonth)(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual tasksExpiringThisMonth
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

      val result = controllers.Tasks.expiresInDay(thisYear, thisMonth, thisDay)(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual tasksExpiringToday
    }
  }

  "Deleting" should {
    "return a no content HTTP status if the task was deleted" in new WithApplication(fakeApp) {
      setUp()
      val result = controllers.Tasks.delete(anonymousTask.id)(FakeRequest())
      status(result) mustEqual 204
    }
    "return not found HTTP status if the task doesn't exist" in new WithApplication(fakeApp) {
      setUp()
      val result = controllers.Tasks.delete(-1)(FakeRequest())
      status(result) mustEqual 404
    }
  }

  "A task" should {
    "be editable" in new WithApplication(fakeApp) {
      setUp();
      val result = controllers.Tasks.update(anonymousTask.id)(
        FakeRequest().withFormUrlEncodedBody("label" -> "HelloWorld")
      )
    }
  }
}