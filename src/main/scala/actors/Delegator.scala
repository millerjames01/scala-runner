package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

import data.Request

class Delegator extends Actor {
  import Delegator._
  import Runner.Run
  
  var lookup: Map[Long, ActorRef] = Map()
  
  implicit val timeout = Timeout(5 seconds)
  
  def receive() = {
    case Compute(req) => lookup.get(req.session) match {
      case Some(compiler) => delegateAndRespond(compiler, req.code, sender)
      case None => {
        val actor = context.actorOf(Props[Runner])
        val newPair = (req.session, actor)
        lookup = lookup + newPair
        delegateAndRespond(actor, req.code, sender)
      }
    }
  }
  
  def delegateAndRespond(worker: ActorRef, code: String, sender: ActorRef) = {
    val response = worker ? Run(code)
    response onComplete {
      case Success(result) => sender ! result
      case Failure(_) => sender ! ""
    }
  }
}

object Delegator {
  case class Compute(req: Request)
}