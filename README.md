Scala-Runner
============

Motivation for Demo
-------------------
This project is a proof-of-concept for a way to run user-submitted Scala
code samples, that may possibly use too many resources or even infinitely recurse,
in a manner that would allow the server-side to persist and not leak any memory.

The Demo, contained in DemoApplication, is run by doing:

    sudo sbt "run Demo"

Note for your first time compiling and updating I would do

    sbt compile

without the sudo, as that may create nasty permission locks on
some of the files.

The Manager Actor
-----------------
The majority of the work done in the Demo is done by the Manager actor
in the management actor system.

The Manager actor is responsible for creating and supervising his two
child actors, Archiver and RequestSteque, and also for creating a
remote ActorSystem by using a Scala Process object to run it.

The Manager starts out by searching for the remote actor, and
once he is found, he pops all the requests submitted to him
(which, in the Demo, is done manually in DemoApplication.scala).
The results from compiling these few requests will return,
and on upon noticing that one takes too much time, the Manager
actor will attempt to destroy the process and create a new one.

The process is not currently destroyed correctly, and this 
causes the fault tolerance of the system to fail.
I am still working to correct this bug.
