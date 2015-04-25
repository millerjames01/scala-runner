package actors

import akka.actor.Actor
import akka.actor.Props

import data.Request

class RequestSteque extends Actor {
  import RequestSteque._
  
  var steque: List[Request] = Nil
  
  def receive = {
    case PushBack(req) => steque = steque :+ req
    case PushFront(req) => steque = req :: steque
    case Pop(req) => {
      steque = steque.tail
      sender ! Popped(steque.head)
    }
  }
}

object RequestSteque {
  case class PushBack(req: Request)
  case class PushFront(req: Request)
  case class Pop(req: Request)
  case class Popped(req: Request)
}