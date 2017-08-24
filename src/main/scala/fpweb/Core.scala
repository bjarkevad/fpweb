package fpweb

import scalaz.{ Monad, Free, Kleisli, ~> }
import scalaz.syntax.monad._
import scalaz.std.function._
import scalaz.effect.IO
import scalaz.concurrent.Task
import scalaz.std.option._
import scalaz.std.list._

import doobie.imports._

import Model._

object Core {
 sealed trait ServiceOp[A]
  object ServiceOp {
    case class GetUser(userId: Int) extends ServiceOp[User]
    case class AddUser(user: User) extends ServiceOp[Int]
    case class DeleteUser(userId: Int) extends ServiceOp[Boolean]
    case object GetAllUsers extends ServiceOp [List[User]]
  }

  type Service[A] = Free[ServiceOp, A]

  def getUser(userId: Int) = Free.liftF(ServiceOp.GetUser(userId))
  def addUser(user: User) = Free.liftF(ServiceOp.AddUser(user))
  def deleteUser(userId: Int) = Free.liftF(ServiceOp.DeleteUser(userId))
  val getAllUsers = Free.liftF(ServiceOp.GetAllUsers)

  def addAndGetUser(user: User) = for {
    id <- addUser(user)
    user <- getUser(id)
  } yield user


  // TODO: Name this better?
  type KT[A] = Kleisli[Task, Transactor[Task], A]

  val xa = DriverManagerTransactor[Task]("org.sqlite.JDBC", "jdbc:sqlite:fpweb.db")

  def mockInterpreter: ServiceOp ~> KT =
    new (ServiceOp ~> KT) {
      def apply[A](fa: ServiceOp[A]) =
        fa match {
          case ServiceOp.GetUser(userId)    =>
            Kleisli { xa  =>
              User(userId, "Mock user").point[Task]
            }
          case ServiceOp.AddUser(user)      =>
            1.point[KT]

          case ServiceOp.DeleteUser(userId) =>
            true.point[KT]

          case ServiceOp.GetAllUsers        =>
            List.empty.point[KT]

        }
    }

  def runService[A](program: Service[A]): KT[A] =
    program.foldMap(mockInterpreter)

  val myProgram = for {
    users <- getAllUsers
  } yield users

  def run = runService(myProgram)
}
