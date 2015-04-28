package actors

import akka.actor.Actor
import akka.actor.Props

import data.{ Status, NotCompleted, Completed, Failed }
import data.Request

class Archiver extends Actor {
  import Archiver._
  var archive: Map[Request, Status] = Map()
  
  def receive = {
    case Add(req) => archive = archive + ((req, NotCompleted))
    case Complete(req) => archive = archive.updated(req, Completed)
    case Fail(req) => archive = archive.updated(req, Failed)
    case Reboot => 
      val completeds = (archive.toList filter complete) map (_._1)
      val notCompleteds = (archive.toList filter incomplete) map (_._1)
      sender ! RebootInstructions(completeds, notCompleteds)
  }
  
  def incomplete: (((Request, Status)) => Boolean) = {
    case (_, NotCompleted) => true
    case _ => false
  }
  
  def complete: (((Request, Status)) => Boolean) = {
    case (_, Completed) => true
    case _ => false
  }
}

object Archiver {
  case class Add(req: Request)
  case class Complete(req: Request)
  case class Fail(req: Request)
  case class Query(pred: ((Request, Status)) => Boolean)
  case object Reboot
  case class RebootInstructions(completeReqs: List[Request], 
                                incompleteReqs: List[Request])
}