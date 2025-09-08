package shared.notification

import io.grpc.{Server, ServerBuilder}
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._
import repositories.OrderAlertsRepo
import services.StringServiceImpl
import io.grpc.protobuf.services.ProtoReflectionService

object NotificationServer {

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    println("DB URL: " + sys.env.get("DB_HOST") + " / " + sys.env.get("DB_NAME"))

    val db = Database.forConfig("slick.dbs.default.db")
    val repo = new OrderAlertsRepo(db)
    repo.init()

    val serviceImpl = new StringServiceImpl(repo)

    
    val port = 50052
    val server: Server = ServerBuilder
      .forPort(port)
      
      .addService(StringServiceGrpc.bindService(serviceImpl, ec))
      .addService(ProtoReflectionService.newInstance())
      .build()
      .start()

    println(s" Notification gRPC server running on port $port")

    sys.addShutdownHook {
      println("Shutting down gRPC server...")
      server.shutdown()
    }

    server.awaitTermination()
    
  }
}