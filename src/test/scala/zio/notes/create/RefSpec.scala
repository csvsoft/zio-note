package zio.notes.create

import zio.{IO, Ref}

class RefSpec extends BasicSpec {

  it("Ref models a mutable reference to a value of A with a atomicity and thread-safe,, operations: set/get"){
   val refOps =   for {
      aRef <- Ref.make[Int](1)
      _ <- aRef.set(2)
      v <- aRef.get
    } yield v
    eval(refOps) shouldBe 2
  }

  it("Repeat with idRef update"){

    def repeat[E,A](n:Int)(io:IO[E,A]) = {
      Ref.make[Int](0).flatMap { idRef =>
        def loop: IO[E, Unit] = idRef.get.flatMap { i =>
          if (i < n)
            io *> idRef.update(_ + 1) *> loop
          else
            IO.unit
        }
        loop
      }

    }
    val ops = IO.effect(println("hello"))
    val repeatOps = repeat(10)(ops)
    eval(repeatOps)

  }

  it("modify"){
   val newValue =  Ref.make[Int](0).flatMap( (idCounter:zio.Ref[Int]) =>{
      def increase = idCounter.modify[String](id=>(s"counter: ${id+1}",id + 1))
      for{
        _ <- increase
        _ <- increase
        v3 <- increase
      }yield v3
    })
    eval(newValue)
  }

}
