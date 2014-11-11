import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import java.util.{Date}
import java.text.SimpleDateFormat
import play.api.libs.json._

class UsersSpec() extends Specification {
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

  "A user" should {
    "be able of listing his/her tasks" in new WithApplication(fakeApp) {
      setUp()
      val pedroTasks = tasks.filter(t => t.user.equals("pedro")).length
      val result = controllers.Users.tasks("pedro")(FakeRequest())

      status(result) mustEqual 200
      contentType(result) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(result).as[JsArray].value
      jsonArray.size mustEqual pedroTasks
    }
    "be able of creating new categories" in new WithApplication(fakeApp) {
      setUp()
      val result = controllers.Categories.create("pedro")(
        FakeRequest().withFormUrlEncodedBody("name" -> "HelloWorld")
      )

      status(result) mustEqual 201
      contentType(result) must beSome.which(_ == "application/json")
      contentAsString(result) must contain("HelloWorld")
    }
  }
}