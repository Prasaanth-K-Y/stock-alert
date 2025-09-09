package modules
// ===========================  Any query related to grpc setup , Feel free to add an issue (github: Prasaanth-K-Y)
import javax.inject.{Inject, Provider, Singleton}
import play.api.inject.{ApplicationLifecycle, Binding, Module}
import play.api.{Configuration, Environment}
import scala.concurrent.Future
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import shared.notification.StringServiceGrpc

/**
 * GrpcClientModule is a standard Play Framework module that provides dependency injection bindings for our gRPC client components.
 * our application can automatically inject a `ManagedChannel` and a `StringServiceStub`
 */
class GrpcClientModule extends Module {


  /** ============---------==========-----------=========-----------================
   * The `bindings` method defines how Play's dependency injection container should
   * @param environment The application's environment (e.g., Dev, Test, Prod).
   * @param configuration The application's configuration settings.
   * @return A sequence of `Binding` objects.
   * ==============-----------===================--------=============--------=====
   */


  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      // Binds the ManagedChannel type to a provider, ensuring a single instance is created.
      bind[ManagedChannel].toProvider[GrpcChannelProvider].in[Singleton],
      
      // This stub is the primary interface for making gRPC calls.
      bind[StringServiceGrpc.StringServiceStub].toProvider[StringServiceStubProvider].in[Singleton]
    )
  }
}


 // GrpcChannelProvider is a custom provider that creates and manages a `ManagedChannel`.
@Singleton
class GrpcChannelProvider @Inject() (
    config: Configuration,
    lifecycle: ApplicationLifecycle
) extends Provider[ManagedChannel] {

  // Reads the gRPC server's host from the application.conf file.
  private val host = config.get[String]("grpc.alert-service.host")

  // Reads the gRPC server's port from the application.conf file.
  private val port = config.get[Int]("grpc.alert-service.port")

  // The `get` method is where the channel is actually created. `lazy val` ensures and also the channel is only created once, when it's first requested.
  lazy val get: ManagedChannel = {

    // Uses ManagedChannelBuilder to configure the connection.
    val channel = ManagedChannelBuilder.forAddress(host, port)

      .usePlaintext() // For development; disables TLS. In production, this should be removed for secure communication.
      .build()
    

    // Adds a hook to Play's application lifecycle to gracefully shut down the channel
    // when the application stops, preventing resource leaks.
    lifecycle.addStopHook(() => Future.successful(channel.shutdown()))
    
    channel // Returns the created channel.
  }
}


// StringServiceStubProvider is a provider that creates the gRPC client stub.
@Singleton
class StringServiceStubProvider @Inject()(channel: ManagedChannel)
    extends Provider[StringServiceGrpc.StringServiceStub] {

  
  // The `get` method creates the stub. It takes the `ManagedChannel` (which is automatically injected by Play) and uses it to build the stub.
  
  lazy val get: StringServiceGrpc.StringServiceStub = StringServiceGrpc.stub(channel)
} 



