package fpweb

import argonaut._, Argonaut._

object Model {
  case class NewUser(userName: String, password: String)
  case class User(userId: Int, userName: String)

  implicit def NewUserJsonCodec =
    casecodec2(NewUser.apply, NewUser.unapply)("user_name", "password")

  implicit def UserJsonCodec =
    casecodec2(User.apply, User.unapply)("user_id", "user_name")
}
