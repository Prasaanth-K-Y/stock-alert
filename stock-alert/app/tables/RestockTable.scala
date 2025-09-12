package tables

import slick.jdbc.MySQLProfile.api._
import models.Restock

class RestockTable(tag: Tag) extends Table[Restock](tag, "restock") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def itemId = column[Long]("itemId")
  def customerId = column[Long]("customerId")

  // Relates MySQL table and Scala case class
  def * = (id.?, itemId, customerId).mapTo[Restock]
}
