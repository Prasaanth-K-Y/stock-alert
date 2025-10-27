package tables

import slick.jdbc.MySQLProfile.api._
import models.Ref

class RefTable(tag: Tag) extends Table[Ref](tag, "refes") {
  def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("user_id")      
  def ref    = column[String]("ref")

  def * = (id.?, userId, ref) <> ((Ref.apply _).tupled, Ref.unapply)
}
