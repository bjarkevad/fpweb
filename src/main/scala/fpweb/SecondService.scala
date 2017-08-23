package fpweb

import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.dsl._

object SecondService {
  val service = HttpService {
    case GET -> Root / "echo" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }
}
