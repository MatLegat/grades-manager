package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.User

@Singleton
class Application @Inject() extends Controller {

  def loginGet = Action { implicit request =>
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.loginForm())
    }
  }

  def loginPost = Action.async { implicit request =>
    val form = Form(tuple(
      "login" -> text,
      "password" -> text
    ))
    val (login, password) = form.bindFromRequest.get
    val futureOptUser = scala.concurrent.Future { User.login(login, password) }
    futureOptUser.map(optUser =>
      optUser match {
        case None => Ok(views.html.loginForm(Option("Usuário ou senha incorretos.")))
        case Some(user) => Redirect(routes.Application.index).withSession(
          "user" -> login
        )
      }
    )
  }

  def logout = Action {
    Redirect(routes.Application.loginGet).withNewSession.flashing(
      "status" -> "Logout realizado com sucesso."
    )
  }

  def registerGet = Action { implicit request =>
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.registerForm())
    }
  }

  def registerPost = Action.async { implicit request =>
    val form = Form(tuple(
      "login" -> text,
      "name" -> text,
      "email" -> text,
      "password" -> text,
      "passwordConfirm" -> text
    ))
    val (login, name, email, password, passwordConfirm) = form.bindFromRequest.get
    val futureOptUser = scala.concurrent.Future { User.register(login, name, email, password) }
    futureOptUser.map(optUser =>
      optUser match {
        case None => Ok(views.html.registerForm(Option("ERRO")))
        case Some(user) => Redirect(routes.Application.index).withSession(
          "user" -> login
        )
      }
    )
  }

  def index = Action {implicit request =>
    request.session.get("user") match {
      case Some(login) => User.getByLogin(login) match {
        case Some(user) => Ok(views.html.index(user))
        case None => Redirect(routes.Application.logout)
      }
      case None => Redirect(routes.Application.loginGet)
    }
  }

}
