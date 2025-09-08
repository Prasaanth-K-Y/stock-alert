error id: file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/app/tables/ItemsTable.scala:`<error>`#`<error>`.
file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/app/tables/ItemsTable.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slick/jdbc/MySQLProfile.api.column.
	 -slick/jdbc/MySQLProfile.api.column#
	 -slick/jdbc/MySQLProfile.api.column().
	 -column.
	 -column#
	 -column().
	 -scala/Predef.column.
	 -scala/Predef.column#
	 -scala/Predef.column().
offset: 296
uri: file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/app/tables/ItemsTable.scala
text:
```scala
package tables
import slick.jdbc.MySQLProfile.api._
import models.Items

class ItemsTable(tag:Tag)extends Table[Items](tag,"items"){
    def id =  column[Long]("id",O.PrimaryKey,O.AutoInc)
    def name =column[String]("name")
    def stock = column[Long]("stock")
    def minStock = colum@@n[Long]("minStock")

    def * =  (id.?, name,stock, minStock).mapTo[Items]
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 