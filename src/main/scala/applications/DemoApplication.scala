package applications

import scala.concurrent.duration._
import scala.language.implicitConversions
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

import actors.Delegator
import actors.Manager
import data.Request

object DemoApplication {
  def main(args: Array[String]): Unit = {
    if (!args.isEmpty && args.head == "Demo") startRemoteManagementSystem()
    else startRemoteRunnerSystem()
  }

  def startRemoteRunnerSystem(): Unit = {
    val system = ActorSystem("RunnerSystem",
      ConfigFactory.load("remoterunner"))
    system.actorOf(Props[Delegator], "delegator")

    println("Started DelegatorSystem - waiting for messages")
  }

  def startRemoteManagementSystem(): Unit = {
    val system =
      ActorSystem("ManagementSystem", ConfigFactory.load("remotemanagement"))
    val remotePath =
      "akka.tcp://RunnerSystem@127.0.0.1:2552/user/delegator"
    val manager = system.actorOf(Props(classOf[Manager], remotePath), "manager")
    
    import system.dispatcher
    
    manager ! Request("val x = 2 + 3", 2, System.currentTimeMillis)
    manager ! Request("val y = x + 2", 2, System.currentTimeMillis)
    manager ! Request("def f(z: Int): Int = f(z - 1)", 2, System.currentTimeMillis())
    manager ! Request("f(2)", 2, System.currentTimeMillis())
    manager ! Request("x + y", 2, System.currentTimeMillis())
  }
}
