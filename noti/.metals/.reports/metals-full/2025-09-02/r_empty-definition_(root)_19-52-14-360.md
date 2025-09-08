error id: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala:
file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -profile/api/dbConfigProvider.
	 -dbConfigProvider.
	 -scala/Predef.dbConfigProvider.
offset: 742
uri: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala
text:
```scala
package shared.notification

import io.grpc.{Server, ServerBuilder}
import scala.concurrent.ExecutionContext
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import repositories.OrderAlertsRepo
import services.StringServiceImpl
import slick.basic.DatabaseConfig
import slick.lifted
import repositories.OrderAlertsTable

class OrderAlertsRepo(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  private val db = dbConfig.db
  private val profile = dbConfig.profile
  import profile.api._

  private val alerts = lifted.TableQuery[OrderAlertsTable]
  def start(): Unit = {
    // 1. Get Slick DB from injected config provider
    val dbConfig = @@dbConfigProvider.get[JdbcProfile]
    val repo = new OrderAlertsRepo(dbConfig)(ec)

    // 2. Initialize table
    repo.init().foreach(_ => println("✅ Table created/checked"))

    // 3. gRPC service
    val serviceImpl = new StringServiceImpl(repo)

    // 4. Start gRPC server
    val port = 50052
    val server: Server = ServerBuilder
      .forPort(port)
      .addService(StringServiceGrpc.bindService(serviceImpl, ec))
      .build()
      .start()

    println(s"🚀 Notification gRPC server running on port $port")

    sys.addShutdownHook {
      println("Shutting down gRPC server...")
      server.shutdown()
    }

    server.awaitTermination()
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 