package repositories

import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import models.Orders
import tables.OrdersTable

// The OrdersRepo class handles all database interactions for the 'Orders' table.
class OrdersRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  
  private val dbConfig = dbc.get[JdbcProfile] // Get the database configuration.
  private val db = dbConfig.db // Get the database instance for running queries.

  private val o = TableQuery[OrdersTable] // Define a queryable object for the Orders table.

  // Creates a new order and returns its generated ID.
  def newOrder(ord: Orders): Future[Long] = db.run((o returning o.map(_.id)) += ord)
  
  // Retrieves a single order by its ID.
  def getOrder(id: Long): Future[Option[Orders]] = db.run(o.filter(_.id === id).result.headOption)
  
  // Deletes all records from the orders table, useful for cleanup or testing.
  def cleanTables: Future[Int] = db.run(o.delete)
}