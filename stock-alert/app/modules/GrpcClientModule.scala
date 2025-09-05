package modules

import javax.inject.{Inject, Provider, Singleton}
import play.api.inject.{ApplicationLifecycle, Binding, Module}
import play.api.{Configuration, Environment}
import scala.concurrent.Future
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import shared.notification.StringServiceGrpc

class GrpcClientModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[ManagedChannel].toProvider[GrpcChannelProvider].in[Singleton],
      bind[StringServiceGrpc.StringServiceStub].toProvider[StringServiceStubProvider].in[Singleton]
    )
  }
}

@Singleton
class GrpcChannelProvider @Inject() (
  config: Configuration,
  lifecycle: ApplicationLifecycle
) extends Provider[ManagedChannel] {

  private val host = config.get[String]("grpc.alert-service.host")
  private val port = config.get[Int]("grpc.alert-service.port")

  lazy val get: ManagedChannel = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    lifecycle.addStopHook(() => Future.successful(channel.shutdown()))
    channel
  }
}

@Singleton
class StringServiceStubProvider @Inject()(channel: ManagedChannel)
  extends Provider[StringServiceGrpc.StringServiceStub] {

  lazy val get: StringServiceGrpc.StringServiceStub = StringServiceGrpc.stub(channel)
}