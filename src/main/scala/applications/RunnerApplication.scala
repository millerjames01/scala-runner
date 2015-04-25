package applications

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object RunnerApplication {
  def startRemoteWorkerSystem(): Unit = {
    val system =
      ActorSystem("LookupSystem", ConfigFactory.load("remotelookup"))
    val remotePath =
      "akka.tcp://CalculatorSystem@127.0.0.1:2552/user/calculator"
    val actor = system.actorOf(Props(classOf[LookupActor], remotePath), "lookupActor")
  }
}