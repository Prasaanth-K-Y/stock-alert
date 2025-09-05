package shared.notification.repositories


import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Future, ExecutionContext}

case class OrderAlert(id: Long = 0L, orderId: String)

class OrderAlertsTable(tag: Tag) extends Table[OrderAlert](tag, "order_alerts") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def orderId = column[String]("order_id")
  def * = (id, orderId) <> (OrderAlert.tupled, OrderAlert.unapply)
}

class OrderAlertsRepo(db: Database)(implicit ec: ExecutionContext) {
  private val alerts = TableQuery[OrderAlertsTable]

  def init(): Future[Unit] = db.run(alerts.schema.createIfNotExists)

  def insertAlert(orderId: String): Future[Long] =
    db.run((alerts returning alerts.map(_.id)) += OrderAlert(orderId = orderId))
}
