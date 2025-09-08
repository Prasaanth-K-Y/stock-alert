error id: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/stock/StockServer.scala:
file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/stock/StockServer.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb

found definition using fallback; symbol CompletionStage
offset: 579
uri: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/stock/StockServer.scala
text:
```scala
package stock

import play.api._
import play.core.server._
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
import scala.jdk.FutureConverters._       // <-- ADD THIS
import java.util.concurrent.{Callable, CompletionStage}

object StockServer {
  def main(args: Array[String]): Unit = {
    val env = Environment(new java.io.File("."), this.getClass.getClassLoader, Mode.Prod)
    val config = Configuration.load(env)

    val lifecycle: ApplicationLifecycle = new ApplicationLifecycle {
  override def addStopHook(hook: Callable[_ <: Comp@@letionStage[_]]): Unit = {
    // Just call it to satisfy interface (ignore result)
    hook.call()
  }

  override def stop(): CompletionStage[Void] = {
    java.util.concurrent.CompletableFuture.completedFuture(null)
  }
}


    val context = ApplicationLoader.Context(
      environment = env,
      initialConfiguration = config,
      lifecycle = lifecycle,
      devContext = None
    )

    val loader = ApplicationLoader(context)
    val application = loader.load(context)

    val serverConfig = ServerConfig(
      port = Some(9000),
      address = "0.0.0.0",
      rootDir = new java.io.File(".")
    )

    val server = PekkoHttpServer.fromApplication(application, serverConfig)
    println(s"[StockServer] started on http://0.0.0.0:9000")

    sys.addShutdownHook {
      println("[StockServer] shutting down...")
      server.stop()
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 