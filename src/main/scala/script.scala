import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import Futures._


object Example extends App {

  def wait[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  /**
   * sequential execution
   */
  println("a1")
  wait(a1())

  /**
   * if you look at the two examples,
   * a6 will take (3 * 500) millis longer to execute than a2,
   * they look as if they should print in the same order,
   * they don't.
   *
   * this is because of race conditions.
   */
  println()
  println("a2")
  Await.result(a2(), Duration.Inf)
  println()
  println("a6")
  Await.result(a6(), Duration.Inf)

  /**
    * sequential execution
    */
  println()
  println("a3")
  Await.result(a3(), Duration.Inf)

  println()
  println("a4")
  println(a4())

  println()
  println("a5")
  Await.result(a5(), Duration.Inf)



//  throw new RuntimeException("Exceptions should be handled in async code")
}

object Futures {

  def a1()= {
    for {
      r1 <- f1()
      _ = println(r1)
      r2 <- f2()
      _ = println(r2)
      r3 <- f3()
      _ = println(r3)
    } yield ()
  }

  def a2()= {
    val x = f1()
    val y = f2()
    val z = f3()
    for {
      r1 <- x
      _ = println(r1)
      r2 <- y
      _ = println(r2)
      r2 <- z
      _ = println(r2)
    } yield ()
  }

  def a3() = {
    Future {
      f1().map(_ =>
        throw new RuntimeException()
      )
      println("wait a sec, there should have been an exception right?!")
    }
  }

  def a4() = {
    Try {
      Future {
        throw new RuntimeException()
      }
    } match {
      case Success(_) => "how am I successful? the only route is to an exception?!"
      case Failure(_) => "is this expected?"
    }
  }

  def a5() = {
    Future {
      Try {
        Future {
          throw new RuntimeException()
        }
      } match {
        case Success(_) => "how am I successful? the only route is to an exception?!"
        case Failure(_) => "is this expected?"
      }
    }
  }

  def a6()= {
    val x = f4(f1)
    val y = f4(f2)
    val z = f4(f3)
    for {
      r1 <- x
      _ = println(r1)
      r2 <- y
      _ = println(r2)
      r2 <- z
      _ = println(r2)
    } yield ()
  }

  //Future dummy functions

  def f1() = {
    Future {
      Thread.sleep(200)
      println("f1 finished")
      "f1"
    }
  }

  def f2() = {
    Future {
      Thread.sleep(200)
      println("f2 finished")
      "f2"
    }
  }

  def f3() = {
    Future {
      Thread.sleep(30)
      println("f3 finished")
      "f3"
    }
  }

  def f4(fn: () => Future[String]) = {
    Future {
      Thread.sleep(500)
    }.flatMap(_ => fn())
  }
}
