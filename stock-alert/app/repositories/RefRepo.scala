package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import tables.RefTable
import models.Ref
import utils.JwtUtils
import slick.jdbc.MySQLProfile.api._

@Singleton
class RefRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // Database configuration
  private val dbConfig = dbc.get[JdbcProfile]
  private val db       = dbConfig.db
  private val r        = TableQuery[RefTable]

  // Fetch a ref record by its ref string
  def getRef(refStr: String): Future[Option[Ref]] =
    db.run(r.filter(_.ref === refStr).result.headOption)

  // Fetch a ref record by associated user ID
  def getRefByUserId(userId: Long): Future[Option[Ref]] =
    db.run(r.filter(_.userId === userId).result.headOption)

  // Insert a new ref and return its auto-generated ID
  def addRef(ref: Ref): Future[Long] =
    db.run((r returning r.map(_.id)) += ref)

  // Update the ref value for a given ID
  def updateRef(id: Long, newRef: String): Future[Int] =
    db.run(r.filter(_.id === id).map(_.ref).update(newRef))

  // Delete a ref entry by its ref string
  def deleteRef(refStr: String): Future[Int] =
    db.run(r.filter(_.ref === refStr).delete)

  // Retrieve all refs as a sequence of strings
  def getAllRefs(): Future[Seq[String]] =
    db.run(r.map(_.ref).result)

  // Garbage collector to remove expired or invalid JWT refs
  def garbageCollector(): Future[Int] = {
    db.run(r.result).flatMap { allRefs =>
      // Filter out expired refs (where token validation fails)
      val expiredRefs = allRefs.filter(ref => JwtUtils.refValidateToken(ref.ref).isEmpty)

      if (expiredRefs.isEmpty) Future.successful(0)
      else {
        // Delete each expired ref and return total number of deletions
        val deleteActions = expiredRefs.map(ref => r.filter(_.ref === ref.ref).delete)
        db.run(DBIO.sequence(deleteActions).map(_.sum))
      }
    }
  }
}
