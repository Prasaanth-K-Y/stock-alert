package repositories

import models.Items
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import tables.RefTable
import models.Ref
import utils.JwtUtils

// ItemsRepo handles all database operations for the 'Items' table using Slick.
class RefRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  
  private val dbConfig = dbc.get[JdbcProfile] // Get database configuration.
  private val db = dbConfig.db // Get the database instance.
  private val r = TableQuery[RefTable] // Define a query for the Items table.


  // Retrieves a ref by its token.
  def getRef(ref: String): Future[Option[Ref]] = db.run(r.filter(_.ref === ref).result.headOption)
  
  // Adds a new item, ensuring no duplicates by name; returns the new ref's ID or an error message.
  def addRef(ref: Ref): Future[Long] = db.run((r returning r.map(_.id)) += ref)
  // Deletes ref from the table.
  def deleteRef(ref: String): Future[Int] = db.run(r.filter(_.ref === ref).delete)

  def getAllRefs(): Future[Seq[String]] =
    db.run(r.map(_.ref).result)
    
  // Deletes all items from the table.  
def garbageCollector(): Future[Int] = {
  db.run(r.result).flatMap { allRefs =>
    // Filter expired refs
    val expiredRefs = allRefs.filter(ref => JwtUtils.refValidateToken(ref.ref).isEmpty)

    if (expiredRefs.isEmpty) Future.successful(0)
    else {
      // Create delete actions for all expired refs
      val deleteActions = expiredRefs.map(ref => r.filter(_.ref === ref.ref).delete)

      // Combine all delete actions and run them in one DBIO
      db.run(DBIO.sequence(deleteActions).map(_.sum))
    }
  }
}



}