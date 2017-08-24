package fpweb

import doobie.imports.Transactor
import org.http4s
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{Service, Request, Headers, AuthedService}
import org.http4s.dsl._
import org.slf4j.LoggerFactory
import scalaz.concurrent.Task
import scalaz._
import Scalaz._

import org.mindrot.jbcrypt.BCrypt

object Auth {
  val log = LoggerFactory.getLogger(getClass)

  def extract(auth: Authorization): Throwable \/ (String, String) = {
    for {
      b64 <- \/.fromTryCatchNonFatal{auth.value.replace("Basic ", "")}
      decoded <- \/.fromTryCatchNonFatal { java.util.Base64.getDecoder.decode(b64) }
      .map(bytes => new String(bytes))
      .map(_.split(":", 2))
      .map(_.toList)
      unpw <- decoded match {
        case un :: pw :: Nil => \/-((un, pw))
        case _ => -\/(new Exception("Invalid basic authorization"))
      }

    } yield unpw
  }

  def authenticate(xa: Transactor[Task])(headers: Headers): Task[String \/ Model.User] = {
    val unpw = for {
      auth <- headers.get(Authorization).toRightDisjunction("Missing authorization header")
      unpw <- extract(auth).leftMap(_.toString)
    } yield unpw
    unpw.traverse { case (un, pw) =>
      log.info(s"Validating user $un")
      // TODO: Read user + salted pw from db
      (Model.User(1, un), BCrypt.hashpw("password", BCrypt.gensalt), pw).pure[Task]
    }.map {
      _.map { case (user, hashed, candidate) =>
        (user, BCrypt.checkpw(candidate, hashed))
      }.flatMap {
        case (user, true) =>
          log.info(s"User ${user.userName} logged in")
          \/-(user)
        case (_, false) =>
          -\/("Invalid password")
        }
    }
  }

  val authUser: Service[(Request, Transactor[Task]), String \/ Model.User] =
    Kleisli { case (req, xa) => authenticate(xa)(req.headers) }

  val onFailure: AuthedService[String] = Kleisli { req => Forbidden(req.authInfo) }

  def middleware(transactor: Transactor[Task]) =
    AuthMiddleware(authUser.local((_, transactor)), onFailure)
}

