package repositories

import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import models.Orders
import tables.OrdersTable

class OrdersRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbc.get[JdbcProfile]
    private val db = dbConfig.db

  private val o = TableQuery[OrdersTable]


  def newOrder(ord: Orders): Future[Long] =db.run((o returning o.map(_.id)) += ord)
  def getOrder(id: Long): Future[Option[Orders]]=db.run(o.filter(_.id===id).result.headOption)
  def cleanTables : Future[Int]= db.run(o.delete)

}
