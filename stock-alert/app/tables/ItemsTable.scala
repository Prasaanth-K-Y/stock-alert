package tables
import slick.jdbc.MySQLProfile.api._
import models.Items

class ItemsTable(tag:Tag)extends Table[Items](tag,"items"){
    def id =  column[Long]("id",O.PrimaryKey,O.AutoInc)
    def name =column[String]("name")
    def stock = column[Long]("stock")
    def minStock = column[Long]("minStock")

    def * =  (id.?, name,stock, minStock).mapTo[Items]
}