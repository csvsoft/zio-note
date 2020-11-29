package zio.notes.create

import zio.ZIO

class EnvironmentSpec extends BasicSpec {

  case class User(id:String, name:String)
  object DBModule{
    trait DBService{
      def getUserById(id:String):ZIO[DBService,Throwable,User]
    }
  }
  class MockDBService extends DBModule.DBService{
    override def getUserById(id: String): ZIO[DBModule.DBService, Throwable, User] = {
      ZIO.succeed(User(id,"TestUser"))
    }
  }

  it("ZIO access/accessM"){
    val userId = "testUserId"
    // accessM
    val user:ZIO[DBModule.DBService,Throwable,User] = ZIO.accessM[DBModule.DBService](dbService => dbService.getUserById(userId))

    // provide
    val user2: ZIO[Any,Throwable,User] = user.provide(new MockDBService)
    eval(user2) shouldBe User(userId,"TestUser")
  }

}
