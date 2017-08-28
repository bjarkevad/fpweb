package fpweb

import doobie.imports._
import scalaz._
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.std.list.listInstance
import scalaz.Traverse
import scalaz.concurrent.Task
import scalaz.syntax.functor._


object UserRepository {
  def init = Kleisli { xa: Transactor[Task] =>
    for {
      _ <- userSchema.update.run.transact(xa)
    } yield Unit
  }

  val userSchema =
  sql"""
CREATE TABLE IF NOT EXISTS users (
  user_id SERIAL PRIMARY KEY,
  user_name TEXT NOT NULL UNIQUE,
  hashed_password TEXT NOT NULL,
  CONSTRAINT u_user_name UNIQUE (user_name)
  )
    """

  def insert(userName: String, hashedPassword: String) =
    sql"INSERT INTO users (user_name, hashed_password) VALUES ($userName, $hashedPassword)"
      .update

  def get(userId: Int) =
    sql"SELECT user_id, user_name FROM users WHERE user_id = $userId"
      .query[Model.User]

  def getPw(userName: String) =
  sql"SELECT user_id, user_name, hashed_password FROM users WHERE user_name = $userName"
    .query[(Model.User, String)]
}
