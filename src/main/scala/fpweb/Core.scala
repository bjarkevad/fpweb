package fpweb

import scalaz.{Free, ~>}
import scalaz.syntax.monad._
import scalaz.std.function._
import scalaz.effect.IO

object Core {
  // type Actionable[A] = Coyoneda[Action, A]
  sealed trait ServiceOp[A]
  object ServiceOp {
    case class SpecialPrint(x: String) extends ServiceOp[Unit]
  }

  type Service[A] = Free[ServiceOp, A]

  def specialPrint(str: String) = Free.liftF(ServiceOp.SpecialPrint(str))

  type IOInterpreter[A] = IO[A]
  val interpreter: ServiceOp ~> IOInterpreter =
    new (ServiceOp ~> IOInterpreter) {
      def apply[A](fa: ServiceOp[A]) =
        fa match {
          case ServiceOp.SpecialPrint(str) => IO(println(str))
        }
    }

  def runService[A](program: Service[A]): IO[A] =
    program.foldMap(interpreter)

  val myProgram = for {
    _ <- specialPrint("Hello, Free!")
  } yield Unit

  def run = runService(myProgram)
}
