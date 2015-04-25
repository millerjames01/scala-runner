package interpreter

import java.net.URL
import java.io.File
import java.io.PrintWriter
import java.net.URLClassLoader


/**
 * To run the Scala compiler programatically, we need to provide it with a
 * classpath, as we would if we invoked it from the command line. We need
 * to introspect our classloader (accounting for differences between execution
 * environments like IntelliJ, SBT, or WebStart), and find the paths to JAR
 * files on disk.
 */
final class ReplClassLoader(parent: ClassLoader) extends ClassLoader(parent) {
  override def getResource(name: String): URL = {
    // Rather pass `settings.usejavacp.value = true` (which doesn't work
    // under SBT) we do the same as SBT and respond to a resource request
    // by the compiler for the magic name "app.classpath", write the JAR files
    // from our classloader to a temporary file, and return that as the resource.
    if (name == "app.class.path") {
      def writeTemp(content: String): File = {
        val f = File.createTempFile("classpath", ".txt")
        val pw = new PrintWriter(f)
        pw.print(content)
        pw.close()
        f
      }
      //logInfo("Attempting to configure Scala classpath based on classloader: " + getClass.getClassLoader)
      val superResource = super.getResource(name)
      if (superResource != null) superResource // In SBT, let it do it's thing
      else getClass.getClassLoader match {
        case u: URLClassLoader                                    =>
          // Rather pass `settings.usejavacp.value = true` (which doesn't work
          // under SBT) we do the same as SBT and respond to a resource request
          // by the compiler for the magic name "app.classpath"
          val files = u.getURLs.map(x => new java.io.File(x.toURI))
          val f = writeTemp(files.mkString(File.pathSeparator))
          f.toURI.toURL
        case _                                                    =>
          // We're hosed here.
          null
      }
    } else super.getResource(name)
  }
}
 