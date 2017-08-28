package fpweb

import scalaz.{ Monad, Free, Kleisli, ~> }
import scalaz.syntax.monad._
import scalaz.std.function._
import scalaz.effect.IO
import scalaz.concurrent.Task
import scalaz.std.option._
import scalaz.std.list._

import doobie.imports._
import org.mindrot.jbcrypt.BCrypt

import Model._

object Core {
 sealed trait ServiceOp[A]
  object ServiceOp {
    case class GetUser(userId: Int) extends ServiceOp[User]
    case class AddUser(user: NewUser) extends ServiceOp[Int]
    case class DeleteUser(userId: Int) extends ServiceOp[Boolean]
    case object GetAllUsers extends ServiceOp [List[User]]
  }

  type Service[A] = Free[ServiceOp, A]

  def getUser(userId: Int) = Free.liftF(ServiceOp.GetUser(userId))
  def addUser(user: NewUser) = Free.liftF(ServiceOp.AddUser(user))
  def deleteUser(userId: Int) = Free.liftF(ServiceOp.DeleteUser(userId))
  val getAllUsers = Free.liftF(ServiceOp.GetAllUsers)

  def addAndGetUser(user: NewUser) = for {
    id <- addUser(user)
    user <- getUser(id)
  } yield user

  // TODO: Name this better?
  type KT[A] = Kleisli[Task, Transactor[Task], A]

  def interpreter: ServiceOp ~> KT =
    new (ServiceOp ~> KT) {
      def apply[A](fa: ServiceOp[A]) =
        fa match {
          case ServiceOp.GetUser(userId)    =>
            Kleisli { xa  =>
              UserRepository.get(userId)
                .unique
                .transact(xa)
            }

          case ServiceOp.AddUser(user)      =>
            Kleisli { xa =>
              val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt)
              UserRepository.insert(user.userName, hashedPassword)
                .withUniqueGeneratedKeys[Int]("user_id")
                .transact(xa)
            }

          case ServiceOp.DeleteUser(userId) =>
            true.point[KT]

          case ServiceOp.GetAllUsers        =>
            List.empty.point[KT]
        }
    }

  def runService[A](program: Service[A]): Kleisli[Task, Transactor[Task], A] = {
    program.foldMap(interpreter)
  }

  val myProgram = for {
    users <- getAllUsers
  } yield users

  def run = runService(myProgram)
}
