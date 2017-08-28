package fpweb

import doobie.scalatest.TaskChecker
import org.scalatest._
import doobie.imports._
import doobie.scalatest.imports._
import scalaz.concurrent.Task

class UserRepositoryTest extends FunSuite with Matchers with TaskChecker {
  val transactor = DriverManagerTransactor[Task]("org.postgresql.Driver", "jdbc:postgresql:fpweb", "postgres", "")

  test("User schema") {
    // sql"drop table users".update.run.transact(transactor).unsafePerformSync
    // UserRepository.init(transactor).unsafePerformSync
    // check(Database.userSchema.update)
    check(UserRepository.insert("username", "P4ssW0rd"))
    check(UserRepository.get(1))
    check(UserRepository.getPw(""))
    // // check(Database.test)
  }
}
