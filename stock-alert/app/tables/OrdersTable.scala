package tables

import slick.jdbc.MySQLProfile.api._
import models.Orders

class OrdersTable(tag: Tag)  extends Table[Orders](tag,"orders"){
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def item = column[Long]("item")
    def qty = column[Long]("qty")

    def * = (id.?,item, qty).mapTo[Orders]
}