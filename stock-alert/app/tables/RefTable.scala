package tables
import slick.jdbc.MySQLProfile.api._
import models.Ref

class RefTable(tag:Tag)extends Table[Ref](tag,"refs"){
    def id =  column[Long]("id",O.PrimaryKey,O.AutoInc) 
    def ref =column[String]("ref")
    def userId = column[Long]("userId")

    // Relates MySQL table and Scala case class
    def * =  (id.?, ref, userId).mapTo[Ref]
}