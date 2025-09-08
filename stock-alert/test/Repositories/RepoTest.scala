package Repositories

import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import org.scalatest.concurrent.ScalaFutures
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import models.Items
import scala.concurrent.Await
import org.scalatest.BeforeAndAfterEach
import scala.concurrent.duration._
import models.Orders


class RepoTest extends PlaySpec
  with GuiceOneAppPerSuite
  with ScalaFutures 
  with BeforeAndAfterEach{
    //=------------------ORDER
    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
    lazy val irepo = app.injector.instanceOf[repositories.ItemsRepo]
    lazy val orepo = app.injector.instanceOf[repositories.OrdersRepo]
    
    override def beforeEach(): Unit = {
        Await.result(orepo.cleanTables, 2.seconds)
        Await.result(irepo.cleanTables(), 2.seconds)

        }

    //=------------------ORDER
    "ItemsRepoTest" should {
        "item insert in" in{
            val i = Items(Some(1),"soap",21,1)

           whenReady( irepo.addItem(i))
           {res =>
                res must be > 0L

           }

        }
        "item GET id" in {
            val i =  Items(None,"soap", 21, 1)
            whenReady(irepo.addItem(i)){res=>
                res must be > 0L
           
            }
        }
        
        "item insert no id" in {
            val i =  Items(Some(3),"soap", 21, 1)
            whenReady(irepo.addItem(i)){res=>
                res must be > 0L

            whenReady(irepo.getItem(res)){res1=>
                res1 mustBe defined 
                val ite = res1.get
                ite.minStock mustBe i.minStock
                ite.name mustBe i.name
                ite.stock mustBe i.stock
                }
            }
        }

    //=------------------ITEMS
    "OrderRepo" should {
       "order insert in" in{
            whenReady(irepo.addItem(Items(None,"soap",21,1))){it_id=>{
            
            val o = Orders(Some(1),it_id,21)

           whenReady( orepo.newOrder(o)){res =>
                res must be > 0L
                
           }
            }
            }

        }
        "order insert None" in {

           whenReady(irepo.addItem(Items(None,"soap",33,3))){it_id=>{
            
                    val o = Orders(None,it_id,21)

                whenReady( orepo.newOrder(o)){res =>
                        res must be > 0L

                }
            }
        }
    }
        
        "order Get id" in {
            whenReady(irepo.addItem(Items(None,"soap",33,3))){it_id=>{
            
                    val o = Orders(Some(1),it_id,21)

                whenReady( orepo.newOrder(o)){res =>
                        res must be > 0L
                        whenReady(orepo.getOrder(res)){ord=>
                            ord mustBe defined
                            val ord1= ord.get
                            ord1.item mustBe o.item
                            ord1.item mustBe o.item
                        }

                }
            }
        }
    }


    }
 
    }
}