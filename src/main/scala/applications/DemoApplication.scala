package applications

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object DemoApplication {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Calculator")
      startRemoteCalculatorSystem()
    if (args.isEmpty || args.head == "Lookup")
      startRemoteLookupSystem()
  }

  def startRemoteCalculatorSystem(): Unit = {
    val system = ActorSystem("CalculatorSystem",
      ConfigFactory.load("calculator"))
    system.actorOf(Props[CalculatorActor], "calculator")

    println("Started CalculatorSystem - waiting for messages")
  }

  
    println("Started LookupSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 1.second) {
      if (Random.nextInt(100) % 2 == 0)
        actor ! Add(Random.nextInt(100), Random.nextInt(100))
      else
        actor ! Subtract(Random.nextInt(100), Random.nextInt(100))
    }
  }
}
