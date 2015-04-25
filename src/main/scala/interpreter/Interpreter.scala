package interpreter

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results._
import java.io.StringWriter
import java.io.PrintWriter
import scala.language.postfixOps
import scala.tools.nsc.interpreter.{ Results => IntpResults }
import scala.tools.nsc.interpreter.IR.{ Result => IntpResult }

class Interpreter {
  protected lazy val theBigOut = new StringWriter()
  
  protected val theBigRepl = {
    val settings = new Settings
    settings.embeddedDefaults(new ReplClassLoader(settings.getClass().getClassLoader()))
    settings.bootclasspath.value += (
        scala.tools.util.PathResolver.Environment.javaBootClassPath + 
        java.io.File.pathSeparator + "lib/scala-library.jar"
    )
    val theRepl = new IMain(settings, new PrintWriter(theBigOut)) {
      override protected def parentClassLoader: ClassLoader = this.getClass.getClassLoader()
    }
    theRepl
  }
  
  def apply(code: String): String = {
    val len = theBigOut.getBuffer().length()
    theBigRepl.interpret(code) match {
      case IntpResults.Success => {
        val newVar = theBigRepl.mostRecentVar
        val value = theBigRepl.valueOfTerm(newVar)
        val typeOf = theBigRepl.typeOfTerm(newVar).toString
        val valString = value map (_.toString) getOrElse "Uh oh, no new values."
        newVar + ": " + typeOf + " = " + valString
      }
      case IntpResults.Error => {
        theBigOut.getBuffer().substring(len)
      }
      case IntpResults.Incomplete => {
        "Uh oh, so close! Finish the incomplete statement"
      }
    }
  }
  
  def resest = theBigRepl.reset
}