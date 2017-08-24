package fpweb

import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.argonaut._
import org.http4s.server._
import org.http4s.dsl._

import Model._

object UserService {
  val service = HttpService {
    case GET -> Root / "users" =>
      for {
        users <- Core.runService(Core.getAllUsers)
        result <- Ok(users.asJson)
      } yield result

    case POST -> Root / "users" / IntVar(userId) =>
      for {
        user <- Core.runService(Core.addAndGetUser(User(userId, "Test user")))
        result <- Ok(user.asJson)
      } yield result
  }
}
