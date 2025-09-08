error id: 8CB68E83F1E932F6AFA9DC13B88736D2
file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/test/Repositories/RepoTest.scala
### java.lang.AssertionError: assertion failed: (List(Stamp(2023-10-12T15:31:34Z,8865678,java.lang.Object@4bac35e0)),0)

occurred in the presentation compiler.



action parameters:
uri: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/test/Repositories/RepoTest.scala
text:
```scala
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




        // "insert invalidArg" in{
        //    val i =  Items(None,11, 21, 1)
        //     val f = irepo.addItem(i)
        //     intercept[IllegalArgumentException]{
        //         Await.result(f,2.seconds)
        //     }
        // }


    }
 
    }
}
```


presentation compiler configuration:
Scala version: 2.13.16
Classpath:
<WORKSPACE>\.bloop\stock-alert\bloop-bsp-clients-classes\test-classes-Metals-Sq093QMwSo2UTn5koW0A1g== [exists ], <HOME>\AppData\Local\bloop\cache\semanticdb\com.sourcegraph.semanticdb-javac.0.11.0\semanticdb-javac-0.11.0.jar [exists ], <WORKSPACE>\.bloop\stock-alert\bloop-bsp-clients-classes\classes-Metals-Sq093QMwSo2UTn5koW0A1g== [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.16\scala-library-2.13.16.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\twirl\twirl-api_2.13\2.0.9\twirl-api_2.13-2.0.9.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-server_2.13\3.0.8\play-server_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-test_2.13\3.0.8\play-test_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-logback_2.13\3.0.8\play-logback_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-pekko-http-server_2.13\3.0.8\play-pekko-http-server_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-filters-helpers_2.13\3.0.8\play-filters-helpers_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-guice_2.13\3.0.8\play-guice_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatestplus\play\scalatestplus-play_2.13\7.0.2\scalatestplus-play_2.13-7.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick_2.13\5.4.0\play-slick_2.13-5.4.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick-evolutions_2.13\5.4.0\play-slick-evolutions_2.13-5.4.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thesamet\scalapb\scalapb-runtime-grpc_2.13\0.11.14\scalapb-runtime-grpc_2.13-0.11.14.jar [exists ], <HOME>\.ivy2\local\grpc-app\grpc-app_2.13\1.0-SNAPSHOT\jars\grpc-app_2.13.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-netty-shaded\1.64.0\grpc-netty-shaded-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-services\1.64.0\grpc-services-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-xml_2.13\2.2.0\scala-xml_2.13-2.2.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play_2.13\3.0.8\play_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\junit\junit\4.13.2\junit-4.13.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\github\sbt\junit-interface\0.13.3\junit-interface-0.13.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\guava\32.1.3-jre\guava-32.1.3-jre.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-classic\1.5.18\logback-classic-1.5.18.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\fluentlenium\fluentlenium-core\6.0.0\fluentlenium-core-6.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\htmlunit-driver\4.13.0\htmlunit-driver-4.13.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-beanutils\commons-beanutils\1.11.0\commons-beanutils-1.11.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-io\commons-io\2.19.0\commons-io-2.19.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-api\4.14.1\selenium-api-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-support\4.14.1\selenium-support-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-firefox-driver\4.14.1\selenium-firefox-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\guice\6.0.0\guice-6.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\extensions\guice-assistedinject\6.0.0\guice-assistedinject-6.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-streams_2.13\3.0.8\play-streams_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-http-core_2.13\1.0.1\pekko-http-core_2.13-1.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ahc-ws_2.13\3.0.8\play-ahc-ws_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest_2.13\3.2.17\scalatest_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatestplus\mockito-4-11_2.13\3.2.17.0\mockito-4-11_2.13-3.2.17.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatestplus\selenium-4-12_2.13\3.2.17.0\selenium-4-12_2.13-3.2.17.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-java\4.14.1\selenium-java-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\sourceforge\htmlunit\htmlunit-cssparser\1.14.0\htmlunit-cssparser-1.14.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick_2.13\3.6.0\slick_2.13-3.6.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick-hikaricp_2.13\3.6.0\slick-hikaricp_2.13-3.6.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play_2.13\2.9.7\play_2.13-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-api_2.13\2.9.7\play-jdbc-api_2.13-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-evolutions_2.13\2.9.7\play-jdbc-evolutions_2.13-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thesamet\scalapb\scalapb-runtime_2.13\0.11.14\scalapb-runtime_2.13-0.11.14.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-collection-compat_2.13\2.11.0\scala-collection-compat_2.13-2.11.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-stub\1.64.0\grpc-stub-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-protobuf\1.64.0\grpc-protobuf-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-netty\1.62.2\grpc-netty-1.62.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-util\1.64.0\grpc-util-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-core\1.64.0\grpc-core-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\errorprone\error_prone_annotations\2.23.0\error_prone_annotations-2.23.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\perfmark\perfmark-api\0.26.0\perfmark-api-0.26.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-api\1.64.0\grpc-api-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\protobuf\protobuf-java-util\3.25.1\protobuf-java-util-3.25.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\j2objc\j2objc-annotations\2.8\j2objc-annotations-2.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-build-link\3.0.8\play-build-link-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-configuration_2.13\3.0.8\play-configuration_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\slf4j-api\2.0.17\slf4j-api-2.0.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jul-to-slf4j\2.0.17\jul-to-slf4j-2.0.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jcl-over-slf4j\2.0.17\jcl-over-slf4j-2.0.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-actor_2.13\1.0.3\pekko-actor_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-actor-typed_2.13\1.0.3\pekko-actor-typed_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-slf4j_2.13\1.0.3\pekko-slf4j_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-serialization-jackson_2.13\1.0.3\pekko-serialization-jackson_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-core\2.14.3\jackson-core-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-annotations\2.14.3\jackson-annotations-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.14.3\jackson-datatype-jdk8-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.14.3\jackson-datatype-jsr310-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-databind\2.14.3\jackson-databind-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\dataformat\jackson-dataformat-cbor\2.14.3\jackson-dataformat-cbor-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-parameter-names\2.14.3\jackson-module-parameter-names-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-scala_2.13\2.14.3\jackson-module-scala_2.13-2.14.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt-api\0.11.5\jjwt-api-0.11.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt-impl\0.11.5\jjwt-impl-0.11.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt-jackson\0.11.5\jjwt-jackson-0.11.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-json_2.13\3.0.5\play-json_2.13-3.0.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\inject\javax.inject\1\javax.inject-1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\ssl-config-core_2.13\0.6.1\ssl-config-core_2.13-0.6.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-parser-combinators_2.13\1.1.2\scala-parser-combinators_2.13-1.1.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-sbt\test-interface\1.0\test-interface-1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\checkerframework\checker-qual\3.37.0\checker-qual-3.37.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-core\1.5.18\logback-core-1.5.18.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-remote-driver\4.14.1\selenium-remote-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\atteo\classindex\classindex\3.13\classindex-3.13.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\commons\commons-text\1.10.0\commons-text-1.10.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\httpcomponents\httpclient\4.5.14\httpclient-4.5.14.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\maven\maven-model\3.9.1\maven-model-3.9.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\sourceforge\htmlunit\htmlunit\2.70.0\htmlunit-2.70.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\appium\java-client\8.2.1\java-client-8.2.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\bytebuddy\byte-buddy\1.14.5\byte-buddy-1.14.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-logging\commons-logging\1.3.5\commons-logging-1.3.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\auto\service\auto-service-annotations\1.1.1\auto-service-annotations-1.1.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-json\4.14.1\selenium-json-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-devtools-v85\4.14.1\selenium-devtools-v85-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-http\4.14.1\selenium-http-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-manager\4.14.1\selenium-manager-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\inject\jakarta.inject-api\2.0.1\jakarta.inject-api-2.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\aopalliance\aopalliance\1.0\aopalliance-1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-stream_2.13\1.0.3\pekko-stream_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-parsing_2.13\1.0.1\pekko-parsing_2.13-1.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\parboiled\parboiled_2.13\2.5.0\parboiled_2.13-2.5.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ws_2.13\3.0.8\play-ws_2.13-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ahc-ws-standalone_2.13\3.0.7\play-ahc-ws-standalone_2.13-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\shaded-asynchttpclient\3.0.7\shaded-asynchttpclient-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\shaded-oauth\3.0.7\shaded-oauth-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\cache\cache-api\1.1.1\cache-api-1.1.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-core_2.13\3.2.17\scalatest-core_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-featurespec_2.13\3.2.17\scalatest-featurespec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-flatspec_2.13\3.2.17\scalatest-flatspec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-freespec_2.13\3.2.17\scalatest-freespec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-funsuite_2.13\3.2.17\scalatest-funsuite_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-funspec_2.13\3.2.17\scalatest-funspec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-propspec_2.13\3.2.17\scalatest-propspec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-refspec_2.13\3.2.17\scalatest-refspec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-wordspec_2.13\3.2.17\scalatest-wordspec_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-diagrams_2.13\3.2.17\scalatest-diagrams_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-matchers-core_2.13\3.2.17\scalatest-matchers-core_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-shouldmatchers_2.13\3.2.17\scalatest-shouldmatchers_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-mustmatchers_2.13\3.2.17\scalatest-mustmatchers_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-reflect\2.13.16\scala-reflect-2.13.16.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\mockito\mockito-core\4.11.0\mockito-core-4.11.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-chrome-driver\4.14.1\selenium-chrome-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-devtools-v116\4.14.1\selenium-devtools-v116-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-devtools-v117\4.14.1\selenium-devtools-v117-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-devtools-v118\4.14.1\selenium-devtools-v118-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-edge-driver\4.14.1\selenium-edge-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-ie-driver\4.14.1\selenium-ie-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-safari-driver\4.14.1\selenium-safari-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.3\config-1.4.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\zaxxer\HikariCP\6.2.1\HikariCP-6.2.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-build-link\2.9.7\play-build-link-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-streams_2.13\2.9.7\play-streams_2.13-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-configuration_2.13\2.9.7\play-configuration_2.13-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\twirl-api_2.13\1.6.9\twirl-api_2.13-1.6.9.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.21\akka-actor_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor-typed_2.13\2.6.21\akka-actor-typed_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-slf4j_2.13\2.6.21\akka-slf4j_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-serialization-jackson_2.13\2.6.21\akka-serialization-jackson_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-json_2.13\2.10.6\play-json_2.13-2.10.6.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\protobuf\protobuf-java\3.25.1\protobuf-java-3.25.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thesamet\scalapb\lenses_2.13\0.11.14\lenses_2.13-0.11.14.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\api\grpc\proto-google-common-protos\2.29.0\proto-google-common-protos-2.29.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-protobuf-lite\1.64.0\grpc-protobuf-lite-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-codec-http2\4.1.100.Final\netty-codec-http2-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-handler-proxy\4.1.100.Final\netty-handler-proxy-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-transport-native-unix-common\4.1.100.Final\netty-transport-native-unix-common-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\codehaus\mojo\animal-sniffer-annotations\1.23\animal-sniffer-annotations-1.23.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\android\annotations\4.1.1.4\annotations-4.1.1.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\grpc\grpc-context\1.64.0\grpc-context-1.64.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-exceptions\3.0.8\play-exceptions-3.0.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-functional_2.13\3.0.5\play-functional_2.13-3.0.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-api\1.28.0\opentelemetry-api-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-context\1.28.0\opentelemetry-context-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-exporter-logging\1.28.0\opentelemetry-exporter-logging-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-common\1.28.0\opentelemetry-sdk-common-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-extension-autoconfigure-spi\1.28.0\opentelemetry-sdk-extension-autoconfigure-spi-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-extension-autoconfigure\1.28.0\opentelemetry-sdk-extension-autoconfigure-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-trace\1.28.0\opentelemetry-sdk-trace-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk\1.28.0\opentelemetry-sdk-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-semconv\1.28.0-alpha\opentelemetry-semconv-1.28.0-alpha.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-os\4.14.1\selenium-os-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\httpcomponents\httpcore\4.4.16\httpcore-4.4.16.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-codec\commons-codec\1.15\commons-codec-1.15.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\codehaus\plexus\plexus-utils\3.5.1\plexus-utils-3.5.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\httpcomponents\httpmime\4.5.14\httpmime-4.5.14.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\sourceforge\htmlunit\htmlunit-core-js\2.70.0\htmlunit-core-js-2.70.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\sourceforge\htmlunit\neko-htmlunit\2.70.0\neko-htmlunit-2.70.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\sourceforge\htmlunit\htmlunit-xpath\2.70.0\htmlunit-xpath-2.70.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\brotli\dec\0.1.2\dec-0.1.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\shapesecurity\salvation2\3.0.1\salvation2-3.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\websocket\websocket-client\9.4.50.v20221201\websocket-client-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\cglib\cglib\3.3.0\cglib-3.3.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-validator\commons-validator\1.7\commons-validator-1.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-net\commons-net\3.9.0\commons-net-3.9.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\dev\failsafe\failsafe\3.3.2\failsafe-3.3.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\pekko\pekko-protobuf-v3_2.13\1.0.3\pekko-protobuf-v3_2.13-1.0.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ws-standalone_2.13\3.0.7\play-ws-standalone_2.13-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ws-standalone-xml_2.13\3.0.7\play-ws-standalone-xml_2.13-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\play-ws-standalone-json_2.13\3.0.7\play-ws-standalone-json_2.13-3.0.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\playframework\cachecontrol_2.13\3.0.1\cachecontrol_2.13-3.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalatest\scalatest-compatible\3.2.17\scalatest-compatible-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scalactic\scalactic_2.13\3.2.17\scalactic_2.13-3.2.17.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\net\bytebuddy\byte-buddy-agent\1.12.19\byte-buddy-agent-1.12.19.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\objenesis\objenesis\3.3\objenesis-3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\seleniumhq\selenium\selenium-chromium-driver\4.14.1\selenium-chromium-driver-4.14.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-exceptions\2.9.7\play-exceptions-2.9.7.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-stream_2.13\2.6.21\akka-stream_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.0\scala-java8-compat_2.13-1.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-functional_2.13\2.10.6\play-functional_2.13-2.10.6.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-common\4.1.100.Final\netty-common-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-buffer\4.1.100.Final\netty-buffer-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-transport\4.1.100.Final\netty-transport-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-codec\4.1.100.Final\netty-codec-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-handler\4.1.100.Final\netty-handler-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-codec-http\4.1.100.Final\netty-codec-http-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-codec-socks\4.1.100.Final\netty-codec-socks-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-metrics\1.28.0\opentelemetry-sdk-metrics-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-sdk-logs\1.28.0\opentelemetry-sdk-logs-1.28.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-api-events\1.28.0-alpha\opentelemetry-api-events-1.28.0-alpha.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\apache\commons\commons-exec\1.3\commons-exec-1.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\jetty-client\9.4.50.v20221201\jetty-client-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\jetty-util\9.4.50.v20221201\jetty-util-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\jetty-io\9.4.50.v20221201\jetty-io-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\websocket\websocket-common\9.4.50.v20221201\websocket-common-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\ow2\asm\asm\7.1\asm-7.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\commons-digester\commons-digester\2.1\commons-digester-2.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-protobuf-v3_2.13\2.6.21\akka-protobuf-v3_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\netty\netty-resolver\4.1.100.Final\netty-resolver-4.1.100.Final.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\opentelemetry\opentelemetry-extension-incubator\1.28.0-alpha\opentelemetry-extension-incubator-1.28.0-alpha.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\jetty-http\9.4.50.v20221201\jetty-http-9.4.50.v20221201.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\eclipse\jetty\websocket\websocket-api\9.4.50.v20221201\websocket-api-9.4.50.v20221201.jar [exists ]
Options:
-deprecation -unchecked -encoding utf8 -Yrangepos -Xplugin-require:semanticdb




#### Error stacktrace:

```
scala.tools.nsc.classpath.FileBasedCache.getOrCreate(ZipAndJarFileLookupFactory.scala:282)
	scala.tools.nsc.classpath.ZipAndJarFileLookupFactory.create(ZipAndJarFileLookupFactory.scala:47)
	scala.tools.nsc.classpath.ZipAndJarFileLookupFactory.create$(ZipAndJarFileLookupFactory.scala:38)
	scala.tools.nsc.classpath.ClassPathFactory$.newClassPath(ClassPathFactory.scala:80)
	scala.tools.nsc.classpath.ClassPathFactory.newClassPath(ClassPathFactory.scala:29)
	scala.tools.nsc.classpath.ClassPathFactory.$anonfun$classesInPathImpl$1(ClassPathFactory.scala:64)
	scala.tools.nsc.classpath.ClassPathFactory.classesInPathImpl(ClassPathFactory.scala:59)
	scala.tools.nsc.classpath.ClassPathFactory.classesInExpandedPath(ClassPathFactory.scala:48)
	scala.tools.util.PathResolver$Calculated$.basis(PathResolver.scala:268)
	scala.tools.util.PathResolver$Calculated$.containers$lzycompute(PathResolver.scala:275)
	scala.tools.util.PathResolver$Calculated$.containers(PathResolver.scala:275)
	scala.tools.util.PathResolver.containers(PathResolver.scala:291)
	scala.tools.util.PathResolver.computeResult(PathResolver.scala:313)
	scala.tools.util.PathResolver.result(PathResolver.scala:296)
	scala.tools.nsc.backend.JavaPlatform.classPath(JavaPlatform.scala:30)
	scala.tools.nsc.backend.JavaPlatform.classPath$(JavaPlatform.scala:29)
	scala.tools.nsc.Global$GlobalPlatform.classPath(Global.scala:133)
	scala.tools.nsc.Global.classPath(Global.scala:158)
	scala.tools.nsc.Global$GlobalMirror.rootLoader(Global.scala:68)
	scala.reflect.internal.Mirrors$Roots$RootClass.<init>(Mirrors.scala:309)
	scala.reflect.internal.Mirrors$Roots.RootClass$lzycompute(Mirrors.scala:323)
	scala.reflect.internal.Mirrors$Roots.RootClass(Mirrors.scala:323)
	scala.reflect.internal.Mirrors$Roots$EmptyPackageClass.<init>(Mirrors.scala:332)
	scala.reflect.internal.Mirrors$Roots.EmptyPackageClass$lzycompute(Mirrors.scala:338)
	scala.reflect.internal.Mirrors$Roots.EmptyPackageClass(Mirrors.scala:338)
	scala.reflect.internal.Mirrors$Roots.EmptyPackageClass(Mirrors.scala:278)
	scala.reflect.internal.Mirrors$RootsBase.init(Mirrors.scala:252)
	scala.tools.nsc.Global.rootMirror$lzycompute(Global.scala:75)
	scala.tools.nsc.Global.rootMirror(Global.scala:73)
	scala.tools.nsc.Global.rootMirror(Global.scala:45)
	scala.reflect.internal.Definitions$DefinitionsClass.ObjectClass$lzycompute(Definitions.scala:295)
	scala.reflect.internal.Definitions$DefinitionsClass.ObjectClass(Definitions.scala:295)
	scala.reflect.internal.Definitions$DefinitionsClass.init(Definitions.scala:1667)
	scala.tools.nsc.Global$Run.<init>(Global.scala:1263)
	scala.tools.nsc.interactive.Global$TyperRun.<init>(Global.scala:1352)
	scala.tools.nsc.interactive.Global.newTyperRun(Global.scala:1375)
	scala.tools.nsc.interactive.Global.<init>(Global.scala:295)
	scala.meta.internal.pc.MetalsGlobal.<init>(MetalsGlobal.scala:49)
	scala.meta.internal.pc.ScalaPresentationCompiler.newCompiler(ScalaPresentationCompiler.scala:627)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$compilerAccess$1(ScalaPresentationCompiler.scala:147)
	scala.meta.internal.pc.CompilerAccess.loadCompiler(CompilerAccess.scala:40)
	scala.meta.internal.pc.CompilerAccess.retryWithCleanCompiler(CompilerAccess.scala:182)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withSharedCompiler$1(CompilerAccess.scala:155)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:154)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withInterruptableCompiler$1(CompilerAccess.scala:92)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

java.lang.AssertionError: assertion failed: (List(Stamp(2023-10-12T15:31:34Z,8865678,java.lang.Object@4bac35e0)),0)