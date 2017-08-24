package fpweb

import doobie.imports._
import scalaz._
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.std.list.listInstance
import scalaz.Traverse
import scalaz.concurrent.Task
import scalaz.syntax.functor._

object Database {
  def init = Kleisli { xa: Transactor[Task] =>
    for {
      _ <- userSchema.update.run.transact(xa)
    } yield Unit
  }

  // def init = List(userSchema)
  //   .traverseU { fragment =>
  //     Kleisli { xa: Transactor[Task] =>
  //       fragment.update.run.transact(xa)
  //     }
  //   }

  val userSchema =
  sql"""
CREATE TABLE IF NOT EXISTS users (
  userId INTEGER PRIMARY KEY ASC,
  userName TEXT NOT NULL)
    """

  val test = sql"select * from users".query[Model.User]
}
