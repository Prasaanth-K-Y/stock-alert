package repositories

import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Future, ExecutionContext}
import tables.RestockTable
import models.Restock

// Repository for managing Restock-related database operations
class RestockRepo @Inject()(dbConfigProvider: play.api.db.slick.DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val db = dbConfigProvider.get.db
  val r = TableQuery[RestockTable]

  // Adds a new restock entry to the database and returns the generated ID
  def add(restock: Restock): Future[Long] = db.run((r returning r.map(_.id)) += restock)

  // Fetches all restock entries for a given item ID
  def getByItemId(itemId: Long): Future[Seq[Restock]] = db.run(r.filter(_.itemId === itemId).result)
}
