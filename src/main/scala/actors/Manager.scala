package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorIdentity
import akka.actor.Identify
import akka.actor.ReceiveTimeout

import scala.sys.process._
import scala.concurrent.duration._
import data.Request
import data.{ PostrunStatus, Completed, Failed }

class Manager(pathToDelegator: String) extends Actor {
  import Manager._
  import RequestSteque._
  import Delegator._
  import Archiver._
  
  var process: Process = null
  
  val archiver = context.actorOf(Props[Archiver])
  val requestSteque = context.actorOf(Props[RequestSteque])
  
  def receive = identifying
  
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(pathToDelegator) ! Identify(pathToDelegator)
    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }
  
  def identifying: Receive = {
    case ActorIdentity(`pathToDelegator`, Some(actor)) =>
      context.become(working(actor))
    case ActorIdentity(`pathToDelegator`, None) => println(s"Remote actor not available")
    case ReceiveTimeout => sendIdentifyRequest()
    case _ => println("Not ready yet")
  }
 
  def working(delegator: ActorRef): Receive = {
    case req: Request => requestSteque ! PushBack(req)
    case Popped(req) => 
      archiver ! Add(req)
      delegator ! Compute(req)
    case Result(result, req, status) => status match {
      case Completed => 
        archiver ! Complete(req)
        println(s"${req.code} returned with \n\t${result}")
      case Failed => 
        archiver ! Fail(req)
        println(s"${req.code} failed with \n\t${result}")
        process.destroy
        process = createRunnerSystem
        context.become(identifying)
    }
  }
  
  def createRunnerSystem: Process = Process("sbt run").run
}

object Manager {
  case class Result(result: String, request: Request, status: PostrunStatus)
}