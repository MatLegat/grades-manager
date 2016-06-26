package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import models.User


case class RegisterData(login: String, name: String, email: String, password: String)


class Application extends Controller {

  def loginGet = Action { implicit request =>
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.loginForm())
    }
  }

  def loginPost = Action.async { implicit request =>
    forms.LoginData.form.bindFromRequest.fold (
      formWithErrors => {
        Future { BadRequest(views.html.loginForm(formWithErrors("login").value)) }
      },
      loginData => {
        User.login(loginData.login, loginData.password).map(optUser =>
          optUser match {
            case None => BadRequest(
              views.html.loginForm(
                Option(loginData.login),
                Option("error.login.incorrect")
              )
            )
            case Some(user) => Redirect(routes.Application.index).withSession(
              "user" -> loginData.login
            )
          }
        )
      }
    )
  }

  def logout = Action {
    Redirect(routes.Application.loginGet).withNewSession.flashing(
      "status" -> "status.logout"
    )
  }

  def registerGet = Action { implicit request =>
    request.session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => Ok(views.html.registerForm(forms.RegisterData.form))
    }
  }

  def registerPost = Action.async { implicit request =>
    val form = forms.RegisterData.form
    form.bindFromRequest.fold (
      formWithErrors => {
        Future { BadRequest(views.html.registerForm(formWithErrors)) }
      },
      userData => {
        User.register(userData.login, userData.name, userData.email, userData.password).map(optUser =>
          optUser match {
            case None => Redirect(routes.Application.loginGet).withNewSession.flashing(
              "error" -> "error.unknow"
            )
            case Some(user) => Redirect(routes.Application.index).withSession(
              "user" -> user.login
            )
          }
        )
      }
    )
  }

  def index = Action.async {implicit request =>
    request.session.get("user") match {
      case Some(login) => User.getByLogin(login).map(optUser =>
        optUser match {
          case Some(user) => Ok(views.html.index(user))
          case None => Redirect(routes.Application.logout)
        }
      )
      case None => Future{ Redirect(routes.Application.loginGet) }
    }
  }

}
