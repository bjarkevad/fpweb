package fpweb

import argonaut._, Argonaut._

object Model {
  case class User(userId: Int, userName: String)

  implicit def UserJsonCodec =
    casecodec2(User.apply, User.unapply)("user_id", "user_name")
}
