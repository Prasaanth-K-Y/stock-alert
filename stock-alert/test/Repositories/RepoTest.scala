package repositories

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.BeforeAndAfterEach
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration._

import models.{Items, Orders, User}
import repositories.{ItemsRepo, OrdersRepo, UserRepo}

class RepoTest
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach {

  // Inject DB config
  val dbConfig = app.injector.instanceOf[DatabaseConfigProvider]
  val db = dbConfig.get[JdbcProfile].db
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override protected def afterEach(): Unit = {
    super.afterEach()
    Await.result(
      db.run(
        DBIO.seq(
          sqlu"DELETE FROM restock",
          sqlu"DELETE FROM orders",
          sqlu"DELETE FROM users",
          sqlu"DELETE FROM items"
        )
      ),
      5.seconds
    )
  }

  val itemsRepo = new ItemsRepo(dbConfig)
  val ordersRepo = new OrdersRepo(dbConfig)
  val userRepo = new UserRepo(dbConfig)

 
  "ItemsRepo" should {
    "add and fetch an item" in {
      val item = Items(None, "Pen", 10, 2)
      val res = Await.result(itemsRepo.addItem(item), 5.seconds).fold(
        err => fail(s"Failed to add item: $err"),
        id => id
      )
      res must be > 0L
      val fetched = Await.result(itemsRepo.getAllItems(), 5.seconds)
      fetched.map(_.name) must contain("Pen")
    }
  }

  "UserRepo" should {
    "add and fetch a user" in {
      val user = User(None, "Alice", "alice@example.com", "Wonderland Street", Some("1234567890"), Some("email"), false, "customer")
      val res = Await.result(userRepo.addUser(user), 5.seconds).fold(
        err => fail(s"Failed to add user: $err"),
        id => id
      )
      res must be > 0L
      val fetched = Await.result(userRepo.getAll(), 5.seconds)
      fetched.map(_.name) must contain("Alice")
    }

    // Prevent duplicate email registration
    "not allow duplicate email registration" in {
      val user1 = User(None, "Bob", "bob@example.com", "Ocean View", Some("9999999999"), None, false, "customer")
      val user2 = User(None, "Bob2", "bob@example.com", "Mountain Top", Some("8888888888"), None, false, "customer")

      Await.result(userRepo.addUser(user1), 5.seconds)
      val duplicateResult = Await.result(userRepo.addUser(user2), 5.seconds)

      duplicateResult.isLeft mustBe true
      duplicateResult.left.get must include("already exists")
    }

    // Update notifications field
    "update user notifications correctly" in {
      val user = User(None, "Charlie", "charlie@example.com", "Skyline Street", None, None, false, "customer")
      val id = Await.result(userRepo.addUser(user), 5.seconds).right.get

      val updateRes = Await.result(userRepo.updateNotifications(id, "order-shipped"), 5.seconds)
      updateRes mustEqual 1

      val updatedUser = Await.result(userRepo.getById(id), 5.seconds).get
      updatedUser.notifications must contain("order-shipped")
    }

    // Fetch user by email
    "fetch user by email correctly" in {
      val user = User(None, "Daisy", "daisy@example.com", "Flower Road", None, None, false, "customer")
      val id = Await.result(userRepo.addUser(user), 5.seconds).right.get

      val fetched = Await.result(userRepo.fetchByEmail("daisy@example.com"), 5.seconds)
      fetched.map(_.name) must contain("Daisy")
    }

    // Delete user by ID
    "delete user successfully" in {
      val user = User(None, "Elon", "elon@example.com", "Space Street", None, None, false, "customer")
      val id = Await.result(userRepo.addUser(user), 5.seconds).right.get

      val deletedCount = Await.result(userRepo.deleteUser(id), 5.seconds)
      deletedCount mustEqual 1

      val afterDelete = Await.result(userRepo.getById(id), 5.seconds)
      afterDelete mustBe None
    }

    // Return None for non-existent user
    "return None for invalid user ID" in {
      val fetched = Await.result(userRepo.getById(9999L), 5.seconds)
      fetched mustBe None
    }
  }

  "OrdersRepo" should {
    "create and fetch an order" in {
      val itemId: Long = Await.result(itemsRepo.addItem(Items(None, "Book", 5, 1)), 5.seconds).fold(
        err => fail(s"Failed to add item: $err"),
        id => id
      )
      val order = Orders(None, itemId, 3)
      val res = Await.result(ordersRepo.newOrder(order), 5.seconds)
      res must be > 0L
      val fetched = Await.result(ordersRepo.getOrder(res), 5.seconds)
      fetched.map(_.qty) must contain(3L)
    }
  }
}
