import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.User

class UserSpec() extends Specification {
  def fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase())

  val pedro = new User("pedro")
  val sergio = new User("sergio")
  val pablo = new User("pablo")
  val users = List(pedro, pablo, sergio)

  "Searching" should {
    "return a user when searching by login" in new WithApplication(fakeApp) {
      var user = User.find(pedro.login)
      user.isEmpty must beFalse
      user.get.login mustEqual pedro.login
    }

    "return an empty user when the login doesn't exist" in new WithApplication(fakeApp) {
      var user = User.find("")
      user.isEmpty must beTrue
    }
  }
}