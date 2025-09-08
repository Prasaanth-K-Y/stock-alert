error id: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/repositories/OrderAlertsRepo.scala:
file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/repositories/OrderAlertsRepo.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slick/jdbc/MySQLProfile.api.MySQLProfile#
	 -MySQLProfile#
	 -scala/Predef.MySQLProfile#
offset: 722
uri: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/repositories/OrderAlertsRepo.scala
text:
```scala
package shared.notification.repositories

import slick.jdbc.MySQLProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// --- Model ---
case class OrderAlert(id: Option[Long] = None, orderId: String)

// --- Table ---
class OrderAlertsTable(tag: Tag) extends Table[OrderAlert](tag, "notification") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def orderId = column[String]("order_id")

  def * = (id.?, orderId).mapTo[OrderAlert]
}

// --- Repository ---
@Singleton
class OrderAlertsRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val db = dbc.get[M@@ySQLProfile].db
  private val alerts = TableQuery[OrderAlertsTable]

  def init(): Future[Unit] = db.run(alerts.schema.createIfNotExists)

  def insertAlert(orderId: String): Future[Long] =
    db.run((alerts returning alerts.map(_.id)) += OrderAlert(orderId = orderId))

  def getAllAlerts(): Future[Seq[OrderAlert]] =
    db.run(alerts.result)

  def deleteAll(): Future[Int] =
    db.run(alerts.delete)
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 