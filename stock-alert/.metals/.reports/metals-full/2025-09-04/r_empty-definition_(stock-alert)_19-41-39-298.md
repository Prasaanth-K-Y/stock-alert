error id: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/repositories/ItemsRepo.scala:
file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/repositories/ItemsRepo.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slick/jdbc/MySQLProfile.api.db.
	 -slick/jdbc/MySQLProfile.api.db#
	 -slick/jdbc/MySQLProfile.api.db().
	 -db.
	 -db#
	 -db().
	 -scala/Predef.db.
	 -scala/Predef.db#
	 -scala/Predef.db().
offset: 458
uri: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/repositories/ItemsRepo.scala
text:
```scala
package repositories

import models.Items
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import tables.ItemsTable
import models.Orders

class ItemsRepo @Inject()(dbc: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbc.get[JdbcProfile]
  private val db@@ = dbConfig.db

  private val It = TableQuery[ItemsTable]

  def getItem(id: Long): Future[Option[Items]] = db.run(It.filter(_.id === id).result.headOption)
  def upd(i: Items, qty: Long): Future[Int] = db.run(It.filter(_.id === i.id).update(i.copy(stock = qty)))
  def addItem(i: Items): Future[Long] = db.run((It returning It.map(_.id)) += i)
  def cleanTables(): Future[Int] = db.run(It.delete)
  def getAllItems(): Future[Seq[Items]] = db.run(It.result)
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 