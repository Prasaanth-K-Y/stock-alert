package shared.notification

import io.grpc.{Server, ServerBuilder}
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._
import repositories.OrderAlertsRepo
import services.StringServiceImpl
import io.grpc.protobuf.services.ProtoReflectionService

// The main object to run the gRPC notification server.
object NotificationServer {

  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global // Use a global execution context for futures.
    println("DB URL: " + sys.env.get("DB_HOST") + " / " + sys.env.get("DB_NAME")) // Prints database connection info from environment variables.

    val db = Database.forConfig("slick.dbs.default.db") // Creates a Slick database instance from application configuration.
    val repo = new OrderAlertsRepo(db) // Creates a new repository for order alerts.
    repo.init() // Initializes the database schema for the alerts table.

    val serviceImpl = new StringServiceImpl(repo) // Creates an instance of the gRPC service implementation.

    val port = 50052 // Defines the port for the gRPC server.
    val server: Server = ServerBuilder // Builds the gRPC server.
      .forPort(port) // Sets the server port.
      .addService(StringServiceGrpc.bindService(serviceImpl, ec)) // Binds the implemented service to the server.
      .addService(ProtoReflectionService.newInstance()) // Adds reflection service for gRPC clients to discover services.
      .build() // Builds the server instance.
      .start() // Starts the server.

    println(s" Notification gRPC server running on port $port") // Confirms the server has started.

    sys.addShutdownHook { // Adds a hook to gracefully shut down the server.
      println("Shutting down gRPC server...") // Logs the shutdown process.
      server.shutdown() // Shuts down the gRPC server.
    }

    server.awaitTermination() // Blocks the main thread until the server is shut down.
  }
}