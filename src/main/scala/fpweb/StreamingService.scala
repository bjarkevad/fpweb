package fpweb

import org.http4s.dsl._
import org.http4s._
import org.postgresql.{PGNotification}
import org.slf4j.LoggerFactory
import scalaz.concurrent.Task
import scalaz.stream._
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz._
import Scalaz._
import scalaz.stream._, Process.{ eval, eval_, repeatEval, emitAll}

import doobie.imports._
import doobie.postgres.imports._

object StreamingService {
  val xa = DriverManagerTransactor[Task]("org.postgresql.Driver", "jdbc:postgresql:fpweb", "postgres", "")
  val log = LoggerFactory.getLogger(getClass)

  def sleep(ms: Long): ConnectionIO[Unit] =
    HC.delay(Thread.sleep(ms))

  def notificationStream(channel: String, ms: Long): Process[ConnectionIO, PGNotification] =
    (for {
       _  <- eval(PHC.pgListen(channel) *> HC.commit)
       ns <- repeatEval(sleep(ms) *> PHC.pgGetNotifications <* HC.commit)
       n  <- emitAll(ns)
     } yield n)
      .onComplete {
        log.info(s"Unlistening $channel")
        eval_(PHC.pgUnlisten(channel) *> HC.commit)
      }

  val service =
    Auth.middleware(xa) {
      AuthedService[Model.User] {
        case GET -> Root / "stream" as user =>
          Ok {
            io.linesR("backup.sql")
              .map(_.toLowerCase)
          }

        case GET -> Root / "events" as user =>
          Ok {
            notificationStream("events", 100)
              .map(_.getParameter)
              .intersperse("\n")
              .transact(xa)
          }
      }
    }

}
