package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import data.{ Completed, Failed }

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

import data.Request

class Delegator extends Actor {
  import Delegator._
  import Manager.Result
  import Runner.Run
  
  var lookup: Map[Long, ActorRef] = Map()
  
  implicit val timeout = Timeout(20.seconds)
  
  def receive() = {
    case Compute(req) =>
      println("Got the request")
      lookup.get(req.session) match {
        case Some(compiler) => delegateAndRespond(compiler, req, sender)
        case None => {
          val actor = context.actorOf(Props[Runner])
          val newPair = (req.session, actor)
          lookup = lookup + newPair
          delegateAndRespond(actor, req, sender)
        }
      }
  }
  
  def delegateAndRespond(worker: ActorRef, request: Request, sender: ActorRef) = {
    val response = (worker ? Run(request.code))(20.seconds).mapTo[String]
    response onComplete {
      case Success(result) => sender ! Result(result, request, Completed)
      case Failure(result) => sender ! Result("Failed to computer in required time", request, Failed)
    }
  }
}

object Delegator {
  case class Compute(req: Request)
}