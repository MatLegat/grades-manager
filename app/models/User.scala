package models

import play.api.Play.current
import anorm._
import play.api.db._
import anorm.SqlParser._
import org.mindrot.jbcrypt.BCrypt

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

  def getByLogin(login: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""select * from "User" where login = {login}""").on(
        'login -> login).as(User.parse.singleOpt)
    }
  }

  def login(login: String, password: String): Option[User] = {
    getByLogin(login) match {
      case Some(user) => BCrypt.checkpw(password, user.password) match {
        case true => Option(user)
        case false => None
      }
      case None => Thread.sleep(2500)
      None
    }
  }

  def register(login: String, name: String, email: String, password: String): Option[User] = {
    // TODO form validation
    def hash = BCrypt.hashpw(password, BCrypt.gensalt(15));
    DB.withConnection { implicit connection =>
      SQL("""
        insert into "User" values (
          {login}, {name}, {email}, {password}
        )
      """).on(
        'login -> login, 'name -> name, 'email -> email, 'password -> hash
      ).executeUpdate()
    Option(User(login, name, email, hash))
    }
  }
}
