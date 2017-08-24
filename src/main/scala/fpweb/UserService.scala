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
  val xa = DriverManagerTransactor[Task]("org.sqlite.JDBC", "jdbc:sqlite:fpweb.db")
  val i = Database.init(xa)

  val service = Auth.middleware(xa)(
    AuthedService[Model.User] {
      case GET -> Root / "me"  as user =>
        Ok(user.asJson)

      case GET -> Root / "users" as user =>
        for {
          users <- Core.runService(Core.getAllUsers)(xa)
          result <- Ok(users.asJson)
        } yield result

      case POST -> Root / "users" / IntVar(userId) as user =>
        for {
          user <- Core.runService(Core.addAndGetUser(User(userId, "Test user")))(xa)
          result <- Ok(user.asJson)
        } yield result
  })
}
