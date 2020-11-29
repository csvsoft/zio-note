package zio.notes.zlayer

import zio.{Has, IO, Task, ZIO, ZLayer, ZManaged}

class ZLayerExample {

}

final case class User(userId: String, name: String, email: String)

final object EntityNotFoundException

final object DBException

final case class DBConfig(jdbcURL: String, user: String, password: String)

class ConnectionPool(dbConfig: DBConfig) {
  def close(): Unit = () // dummy implementation
}

object ConnectionPool {
  type HasDBConfig = Has[DBConfig]

  def createConnectionPool(dbConfig: DBConfig): ZIO[Any, Throwable, ConnectionPool] = {
    ZIO.effect(new ConnectionPool(dbConfig))
  }

  def closeConnectionPool(connectionPool: ConnectionPool): ZIO[Any, Nothing, Unit] = {
    ZIO.effect(connectionPool.close()).catchAll(_ => ZIO.unit)
  }

  def makeManagedPool(dbConfig: DBConfig): ZManaged[Any, Throwable, ConnectionPool] = {
    ZManaged.make(createConnectionPool(dbConfig))(closeConnectionPool)
  }

  val live: ZLayer[HasDBConfig, Throwable, Has[ConnectionPool]] = ZLayer.fromServiceManaged(makeManagedPool)

}

// 0. Object as namespace
object DBModule {

  //1. Create service
  trait Service {
    def executeDML(sql: String): zio.Task[Unit]
    def selectSingle(sql: String): zio.Task[Map[String, Any]]
  }

  class MockService(cp:ConnectionPool) extends Service{
    override def executeDML(sql: String): zio.Task[Unit] = IO.effect(println(s"executing $sql on connection pool:$cp"))
    override def selectSingle(sql: String): Task[Map[String, Any]] = Task.succeed(Map("key" -> "value"))
  }

  // 2. Create type Alias
  type HasDBService = Has[Service]
  type HasConnectionPool = Has[ConnectionPool]

  //3. Create layer
  val mock: ZLayer[Has[ConnectionPool], Throwable, HasDBService] = {
    ZLayer.fromService { cp =>new MockService(cp)}
  }

  // 4. Accessor : client API
  def executeDML(sql: String): ZIO[HasDBService, Throwable, Unit] = ZIO.accessM((db: HasDBService) => db.get.executeDML(sql))

}

object UserModule {

  //1. Service API
  trait Service {
    def getUserById(userId: String): zio.Task[User]
  }

  // 2. Type Alias
  type HasUserSerivce = Has[UserModule.Service]

  // 3. Service Implementations
  class LiveServiceImpl(db: DBModule.Service) extends Service {
    override def getUserById(userId: String): Task[User] = {
      for {
        map <- db.selectSingle("select user from Users where user_id = $user")
        email = map.get("email") match {
          case Some(e: String) => e
          case _ => "defaultEmail"
        }
      } yield User("testUserId", "testUserName", email)

    }
  }

  class MockServiceImpl(db: DBModule.Service) extends Service {
    override def getUserById(userId: String): Task[User] = Task {
      User("testUserId", "testUserName", "testEmail")
    }
  }

  // 4. Layer implementation for DI
  val live: ZLayer[DBModule.HasDBService, Throwable, HasUserSerivce] = ZLayer.fromService(db => {
    new LiveServiceImpl(db)
  })

  val mock: ZLayer[DBModule.HasDBService, Throwable, HasUserSerivce] = ZLayer.fromService(db => {
    new MockServiceImpl(db)
  })

  // 5. concrete API to be used by app
  def getUserById(userId: String): ZIO[HasUserSerivce, Throwable, User] = ZIO.accessM { userSerivce =>
    userSerivce.get.getUserById(userId)
  }

}

import zio._

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val userId = "testUserId"
    val program = UserModule.getUserById(userId)

    // Build dependency graph
    val dbLayer = ZLayer.succeed(DBConfig("jdbcURL", "user", "password")) >>>
      ConnectionPool.live >>>
      DBModule.mock
    val userLayer: ZLayer[Any, Throwable, UserModule.HasUserSerivce] = dbLayer >>> UserModule.mock

    program.provideLayer(userLayer)
      .catchAll(t => ZIO.succeed(t.printStackTrace()).map(_ => ExitCode.failure))
      .map(u => {
        println(s"Got user: $u")
        ExitCode.success
      })

  }
}