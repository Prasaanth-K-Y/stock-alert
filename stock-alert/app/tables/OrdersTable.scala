package tables

import slick.jdbc.MySQLProfile.api._
import models.Orders

class OrdersTable(tag: Tag)  extends Table[Orders](tag,"orders"){
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def item = column[Long]("item")
    def qty = column[Long]("qty")
    def customerId = column[Long]("customerId")

// Relates MySQL table and Scala case class
def * = (id.?, item, qty, customerId).mapTo[Orders]
}