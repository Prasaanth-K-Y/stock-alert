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
  private val dbConfig = dbc.get[JdbcProfile]
  private val db       = dbConfig.db
  private val r        = TableQuery[RefTable]

  def getRef(refStr: String): Future[Option[Ref]] =
    db.run(r.filter(_.ref === refStr).result.headOption)

  def getRefByUserId(userId: Long): Future[Option[Ref]] =
    db.run(r.filter(_.userId === userId).result.headOption)

  def addRef(ref: Ref): Future[Long] =
    db.run((r returning r.map(_.id)) += ref)

  def updateRef(id: Long, newRef: String): Future[Int] =
    db.run(r.filter(_.id === id).map(_.ref).update(newRef))

  def deleteRef(refStr: String): Future[Int] =
    db.run(r.filter(_.ref === refStr).delete)

  def getAllRefs(): Future[Seq[String]] =
    db.run(r.map(_.ref).result)

  def garbageCollector(): Future[Int] = {
    db.run(r.result).flatMap { allRefs =>
      val expiredRefs = allRefs.filter(ref => JwtUtils.refValidateToken(ref.ref).isEmpty)

      if (expiredRefs.isEmpty) Future.successful(0)
      else {
        val deleteActions = expiredRefs.map(ref => r.filter(_.ref === ref.ref).delete)
        db.run(DBIO.sequence(deleteActions).map(_.sum))
      }
    }
  }
}
