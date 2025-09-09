package shared.notification.repositories

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Future, ExecutionContext}

// Case class representing an order alert record.
case class OrderAlert(id: Long = 0L, orderId: String)

// Slick table definition for the 'order_alerts' table.
class OrderAlertsTable(tag: Tag) extends Table[OrderAlert](tag, "order_alerts") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // Column for the primary key, auto-incrementing ID.
  def orderId = column[String]("order_id") // Column to store the order ID as a string.
  def * = (id, orderId) <> (OrderAlert.tupled, OrderAlert.unapply) // Maps the table columns to the OrderAlert case class.
}

// Repository class for performing database operations on OrderAlerts.
class OrderAlertsRepo(db: Database)(implicit ec: ExecutionContext) {
  val alerts = TableQuery[OrderAlertsTable] // A query object for the OrderAlertsTable.

  def init(): Future[Unit] = db.run(alerts.schema.createIfNotExists) // Initializes the database schema for the alerts table if it doesn't exist.

  def insertAlert(orderId: String): Future[Long] = // Inserts a new order alert and returns the generated ID.
    db.run((alerts returning alerts.map(_.id)) += OrderAlert(orderId = orderId))

  def getAlertById(id: Long): Future[Option[OrderAlert]] = // Retrieves an order alert by its ID.
    db.run(alerts.filter(_.id === id).result.headOption)
}