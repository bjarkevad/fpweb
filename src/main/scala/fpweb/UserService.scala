package fpweb

import argonaut._
import Argonaut._
import doobie.imports.Transactor
import org.http4s._
import org.http4s.argonaut._
import org.http4s.server._
import org.http4s.dsl._
import doobie.imports.DriverManagerTransactor
import scalaz.Kleisli
import scalaz.concurrent.Task
import scalaz.syntax.monad._

import Model._

object UserService {
  val xa = DriverManagerTransactor[Task]("org.postgresql.Driver", "jdbc:postgresql:fpweb", "postgres", "")

  val service = HttpService {
    case req @ POST -> Root / "users" =>
      for {
        newUser <- req.as(jsonOf[NewUser])
        user <- Core.runService(Core.addAndGetUser(newUser))(xa)
        result <- Ok(user.asJson)
      } yield result
  }
}
