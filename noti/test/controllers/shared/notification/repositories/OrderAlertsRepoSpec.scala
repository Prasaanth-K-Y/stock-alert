// package shared.notification.repositories

// import org.scalatest.wordspec.AnyWordSpec
// import org.scalatest.matchers.must.Matchers
// import slick.jdbc.MySQLProfile.api._
// import scala.concurrent.{Await, ExecutionContext}
// import scala.concurrent.duration._
// import scala.util.Random

// class OrderAlertsRepoSpec extends AnyWordSpec with Matchers {

//   implicit val ec: ExecutionContext = ExecutionContext.global

  
//   val db = Database.forURL(
//     url = "jdbc:mysql://localhost:3309/testdb",
//     user = "notiuser",
//     password = "notipass",
//     driver = "com.mysql.cj.jdbc.Driver"
//   )

//   val repo = new OrderAlertsRepo(db)

  
//   Await.result(repo.init(), 5.seconds)

//   "OrderAlertsRepo" should {

//     "insert an alert and return its id" in {
//       val orderId = s"order-${Random.nextInt(1000)}"
//       val id = Await.result(repo.insertAlert(orderId), 5.seconds)
//       id must be > 0L

      
//       val inserted = Await.result(repo.getAlertById(id), 5.seconds)
//       inserted.map(_.orderId) mustBe Some(orderId)
//     }
//   }
// }
