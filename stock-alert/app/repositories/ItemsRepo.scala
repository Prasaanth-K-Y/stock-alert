package repositories

import models.Items
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import tables.ItemsTable
import models.Orders

// ItemsRepo handles all database operations for the 'Items' table using Slick.
class ItemsRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  
  private val dbConfig = dbc.get[JdbcProfile] // Get database configuration.
  private val db = dbConfig.db // Get the database instance.
  private val It = TableQuery[ItemsTable] // Define a query for the Items table.

  // Retrieves a single item by its ID.
  def getItem(id: Long): Future[Option[Items]] = db.run(It.filter(_.id === id).result.headOption)
  
  // Updates an item's stock quantity.
  def upd(i: Items, qty: Long): Future[Int] = db.run(It.filter(_.id === i.id).update(i.copy(stock = qty)))
  
  // Retrieves a single item by its name.
  def getItemByName(name: String): Future[Option[Items]] = db.run(It.filter(_.name === name).result.headOption)
  
  // Adds a new item, ensuring no duplicates by name; returns the new item's ID or an error message.
  def addItem(i: Items): Future[Either[String, Long]] = {
    getItemByName(i.name).flatMap {
      case Some(_) => Future.successful(Left(s"Item with name '${i.name}' already exists"))
      case None => db.run((It returning It.map(_.id)) += i).map(Right(_))
    }
  }

  def updateStock(id: Long, qty: Long): Future[Int] = {
    val q = for (i <- It if i.id === id) yield i.stock
    db.run(q.update(qty))
  }

  
  // Deletes all items from the table.
  def cleanTables(): Future[Int] = db.run(It.delete)
  
  // Retrieves all items from the database.
  def getAllItems(): Future[Seq[Items]] = db.run(It.result)
}