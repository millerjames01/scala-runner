package actors

import akka.actor.Actor
import akka.actor.Props

import interpreter.Interpreter
import data.Request

class Runner extends Actor {
  import Runner._
  
  val interp = new Interpreter
  
  def receive = {
    case Run(code) => interp { code }
  }
}

object Runner {
  case class Run(code: String)
}