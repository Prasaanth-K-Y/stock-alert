// package shared.notification.services

// import org.scalatest.wordspec.AnyWordSpec
// import org.scalatest.matchers.must.Matchers
// import shared.notification.repositories.OrderAlertsRepo
// import shared.notification.StringMessage
// import slick.jdbc.MySQLProfile.api._
// import scala.concurrent.{Await, ExecutionContext}
// import scala.concurrent.duration._
// import scala.util.Random

// class StringServiceImplSpec extends AnyWordSpec with Matchers {

//   implicit val ec: ExecutionContext = ExecutionContext.global

  
//   val db = Database.forURL(
//     url = "jdbc:mysql://localhost:3309/testdb",
//     user = "notiuser",
//     password = "notipass",
//     driver = "com.mysql.cj.jdbc.Driver"
//   )

//   val repo = new OrderAlertsRepo(db)
//   Await.result(repo.init(), 5.seconds)

//   val service = new StringServiceImpl(repo)

//   "StringServiceImpl" should {

//     "insert order ID into DB and return success message" in {
//       val orderId = s"order-${Random.nextInt(1000)}"
//       val response = Await.result(service.sendString(StringMessage(orderId)), 5.seconds)

//       response.value must include(orderId)

      
//       val inserted = Await.result(repo.getAlertById(
//         Await.result(repo.insertAlert(orderId), 5.seconds)
//       ), 5.seconds)

//       inserted.map(_.orderId) mustBe Some(orderId)
//     }
//   }
// }
