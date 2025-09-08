error id: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala:shared/notification/`<error: <none>>`.
file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slick/jdbc/MySQLProfile.api.repositories.
	 -repositories.
	 -scala/Predef.repositories.
offset: 164
uri: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/NotificationServer.scala
text:
```scala
package shared.notification

import io.grpc.{Server, ServerBuilder}
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._
import reposi@@tories.OrderAlertsRepo
import services.StringServiceImpl

object NotificationServer {

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    // Database
    val db = Database.forConfig("slick.dbs.default.db")
    val repo = new OrderAlertsRepo(db)
    repo.init() // create table if not exists

    // gRPC service implementation
    val serviceImpl = new StringServiceImpl(repo)

    // gRPC server
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