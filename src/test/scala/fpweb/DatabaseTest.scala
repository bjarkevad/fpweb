package fpweb

import doobie.scalatest.TaskChecker
import org.scalatest._
import doobie.imports._
import doobie.scalatest.imports._
import scalaz.concurrent.Task

class DatabaseTest extends FunSuite with Matchers with TaskChecker {
  val transactor = DriverManagerTransactor[Task]("org.sqlite.JDBC", "jdbc:sqlite:fpweb.db")

  test("User schema") {
    // sql"drop table users".update.run.transact(transactor).unsafePerformIO
    // Database.init(transactor)
    check(Database.userSchema.update)
    check(Database.test)
  }
}
