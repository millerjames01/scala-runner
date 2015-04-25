package actors

import akka.actor.Actor
import akka.actor.Props

import data.{ Status, Uncompleted, Completed, Failed }
import data.Request

class Archiver extends Actor {
  import Archiver._
  
  var archive: Map[Request, Status] = Map()
  
  def receive = {
    case Add(req) => archive = archive + ((req, Uncompleted))
    case Complete(req) => archive = archive.updated(req, Completed)
    case Fail(req) => archive = archive.updated(req, Failed)
    case Query(pred) => {
      val result = archive filter pred
      sender ! result.keys.toList
    }
  }
}

object Archiver {
  case class Add(req: Request)
  case class Complete(req: Request)
  case class Fail(req: Request)
  case class Query(pred: ((Request, Status)) => Boolean)
}