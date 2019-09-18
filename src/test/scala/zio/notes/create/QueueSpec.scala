package zio.notes.create

class QueueSpec extends BasicSpec{

  it("Queue example : creation, operation: offer/take, map/contramap"){

    val qOps = for{
      q <- scalaz.zio.Queue.bounded[Int](1) // queue could be created as bounded, or dropping new item when full or sliding to drop oldest item
      fiberConsumer <- q.map(_.toString).take.fork // map when dequeue, contraMap will map on enqueuing
      _ <- q.offer(1)
      v <- fiberConsumer.join
    } yield v
    eval(qOps) shouldBe "1"

  }

}
