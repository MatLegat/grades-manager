package models

import play.api.Play.current
import anorm._
import play.api.db._
import anorm.SqlParser._
import scala.concurrent.Future
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.concurrent.Execution.Implicits._

case class User(login: String, name: String, email: String, password: String)

object User {

  val parse = {
    get[String]("User.login") ~
    get[String]("User.name") ~
    get[String]("User.email") ~
    get[String]("User.password") map {
      case login ~ name ~ email ~ password => User(login, name, email, password)
    }
  }

  def getByLogin(login: String): Future[Option[User]] = {
    Future {
      DB.withConnection { implicit connection =>
        SQL("""select * from "User" where login = {login}""").on(
          'login -> login.toLowerCase().trim()).as(User.parse.singleOpt)
      }
    }
  }

  def getByEmail(email: String): Future[Option[User]] = {
    Future {
      DB.withConnection { implicit connection =>
        SQL("""select * from "User" where email = {email}""").on(
          'email -> email.toLowerCase()).as(User.parse.singleOpt)
      }
    }
  }

  def login(login: String, password: String): Future[Option[User]] = {
    getByLogin(login).map(optUser =>
      optUser match {
        case Some(user) => BCrypt.checkpw(password, user.password) match {
          case true => Option(user)
          case false => None
        }
        case None => Thread.sleep(2500)
        None
      }
    )
  }

  def register(login: String, name: String, email: String, password: String): Future[Option[User]] = {
    Future {
      def hash = BCrypt.hashpw(password, BCrypt.gensalt(14));
      try {
        DB.withConnection { implicit connection =>
          SQL("""
            insert into "User" values (
              {login}, {name}, {email}, {password}
            )
          """).on(
            'login -> login.toLowerCase().trim(),
            'name -> name.trim(),
            'email -> email.toLowerCase(),
            'password -> hash
          ).executeUpdate()
        Option(User(login, name, email, hash))
        }
      } catch {
        case _ => None
      }
    }
  }
}
