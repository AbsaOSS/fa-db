### How to set things up in ZIO application

Add the following dependencies to your `build.sbt` file.

```scala
"za.co.absa.fa-db" %% "doobie" % "<version>"
// if not already included as transitive dependency
"dev.zio" %% "zio-interop-cats" % "23.0.0.8"
```

Create a class for your database function and ZLayer in its companion object.

```scala
class GetActorById(implicit schema: DBSchema, dbEngine: DoobieEngine[Task])
  extends DoobieOptionalResultFunction[Int, Actor, Task] {

  override def sql(values: Int)(implicit read: Read[Actor]): Fragment =
    sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}($values)"
}

object GetActorById {
  val layer: ZLayer[PostgresDatabaseProvider, Nothing, GetActorById] = ZLayer {
    for {
      dbProvider <- ZIO.service[PostgresDatabaseProvider]
    } yield new GetActorById()(Runs, dbProvider.dbEngine)
  }
}
```

Create a ZLayer for your doobie's Transactor. 
Please note that HikariTransactor is a managed resource, and it needs to be closed when the application is shutting down.
Notice that the ZLayer requires a Scope to be provided. The Scope data type is the foundation of safe and composable resources handling in ZIO.
More on the topic can be found here https://zio.dev/reference/resource/scope/.


```scala
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import zio.Runtime.defaultBlockingExecutor
import zio._
import zio.interop.catz._ // required for ZIO.toScopedZIO, provides also Async for zio.Task

object TransactorProvider {

  val layer: ZLayer[Any with Scope, Throwable, HikariTransactor[Task]] = ZLayer {
    for {
      postgresConfig <- ZIO.config[PostgresConfig](PostgresConfig.config)
      hikariConfig = {
        val config = new HikariConfig()
        config.setDriverClassName(postgresConfig.dataSourceClass)
        config.setJdbcUrl(
          s"jdbc:postgresql://${postgresConfig.serverName}:${postgresConfig.portNumber}/${postgresConfig.databaseName}"
        )
        config.setUsername(postgresConfig.user)
        config.setPassword(postgresConfig.password)
        // configurable pool size
        config.setMaximumPoolSize(postgresConfig.maxPoolSize)
        config
      }
      // notice we are using the default blocking executor from zio.Runtime
      // .toScopedZIO is an extension method that converts a managed resource to a scoped ZIO
      xa <- HikariTransactor.fromHikariConfig[Task](hikariConfig, defaultBlockingExecutor.asExecutionContext).toScopedZIO
    } yield xa
  }
}
```

Transactor can then be provided as a dependency to other ZLayers.

```scala
class PostgresDatabaseProvider(val dbEngine: DoobieEngine[Task])

object PostgresDatabaseProvider {
  val layer: RLayer[Transactor[Task], PostgresDatabaseProvider] = ZLayer {
    for {
      // access the transactor from the environment
      transactor <- ZIO.service[Transactor[Task]]
      doobieEngine <- ZIO.succeed(new DoobieEngine[Task](transactor))
    } yield new PostgresDatabaseProvider(doobieEngine)
  }
}
```

Provide default Scope for your application.

```scala
object Main extends ZIOAppDefault with Server {
  
  override def run: ZIO[Any, Throwable, Unit] =
    server
        .provide(
          // provided layers ...
          // ...
          // provided default scope
          zio.Scope.default
        )
}
```

This way we can establish a connection to a database with managed connection pooling and rely on the default ZIO's blocking execution context and also properly clean up resources when application shuts down.
