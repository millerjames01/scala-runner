package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorIdentity
import akka.actor.Identify
import akka.actor.ReceiveTimeout

import scala.sys.process._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import data.Request
import data.{ PostrunStatus, Completed, Failed }

class Manager(pathToDelegator: String) extends Actor {
  import Manager._
  import RequestSteque._
  import Delegator._
  import Archiver._
  
  var process: Process = createRunnerSystem
  
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
      println("identified")
      context.become(working(actor))
      archiver ! Reboot
    case ActorIdentity(`pathToDelegator`, None) => println("looking out")
    case ReceiveTimeout => sendIdentifyRequest()
    case req: Request => requestSteque ! PushBack(req)
  }
 
  def working(delegator: ActorRef): Receive = {
    case req: Request => requestSteque ! PushBack(req)
    case Popped(req) =>
      println(s"Popped Request ${req.code}")
      archiver ! Add(req)
      delegator ! Compute(req)
    case Result(result, req, Completed) =>
      archiver ! Complete(req)
      println(s"(${req.code}) returned with \n\t${result}")
    case Result(result, req, Failed) => 
      import ExecutionContext.Implicits.global
      archiver ! Fail(req)
      println(s"(${req.code}) failed with \n\t${result}")
      Future { process.destroy } andThen { case _ =>
        println("starting here")
        process = createRunnerSystem
        context.become(identifying)
        sendIdentifyRequest()
      }
    case RebootInstructions(complete, incomplete) =>
      println("Sending reboot instructions")
      val sortedComps = complete sortBy (_.timestamp)
      val sortedNotComps = incomplete sortBy (_.timestamp)
      sortedComps foreach ( delegator ! Compute(_) )
      sortedNotComps.reverse foreach ( requestSteque ! PushFront(_) )
      requestSteque ! PopAll
  }
  
  def createRunnerSystem: Process = Process("sbt run").run
  
  def killProcess: Unit = {
    val processInfo = (Process("sudo netstat -tulpn") #| Process("grep 2552")).!!
    val index = processInfo.indexOf("/java")
    val processId = processInfo.substring(index - 4, index)
    Process(s"kill ${processId}").run.exitValue
  }
}

object Manager {
  case class Result(result: String, request: Request, status: PostrunStatus)
}