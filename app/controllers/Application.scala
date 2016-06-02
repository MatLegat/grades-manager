package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import forms.RegisterData
import forms.LoginData
import models.User


case class RegisterData(login: String, name: String, email: String, password: String)


@Singleton
class Application @Inject() extends Controller {

  def loginGet = Action { implicit request =>
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.loginForm())
    }
  }

  def loginPost = Action.async { implicit request =>
    val loginData = LoginData.form.bindFromRequest.get
    val futureOptUser = scala.concurrent.Future { User.login(loginData.login, loginData.password) }
    futureOptUser.map(optUser =>
      optUser match {
        case None => BadRequest(views.html.loginForm(Option("Usuário ou senha incorretos."), Option(loginData.login)))
        case Some(user) => Redirect(routes.Application.index).withSession(
          "user" -> loginData.login
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
    val form = forms.RegisterData.form
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.registerForm(form))
    }
  }

  def registerPost = Action.async { implicit request =>
    val form = forms.RegisterData.form
    form.bindFromRequest.fold (
      formWithErrors => {
        scala.concurrent.Future { BadRequest(views.html.registerForm(formWithErrors)) }
      },
      userData => {
        val futureOptUser = scala.concurrent.Future {
          User.register(userData.login, userData.name, userData.email, userData.password)
        }
        futureOptUser.map(optUser =>
          optUser match {
            case None => Redirect(routes.Application.index)
            case Some(user) => Redirect(routes.Application.index).withSession(
              "user" -> user.login
            )
          }
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
