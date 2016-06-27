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

  private def newTimeout = (System.currentTimeMillis + 1000 * 60 * 15).toString

  def getLoggedUser(session: Session): Future[Either[Option[String], User]] = {
    session.get("expires") match {
      case Some(timestamp) => {
        if (timestamp.toLong > System.currentTimeMillis)
          User.getByLogin(session.get("user").getOrElse("")).map(
            _ match {
              case Some(user) => Right(user)
              case None => Left(Option("error.session.removed"))
            }
          )
        else
          Future(Left(Option("error.session.timeout")))
      }
      case None => Future(Left(None))
    }
  }

  def doWithSession(action: (User) => Result, session: Session) = {
    def updatedSession = session + ("expires" -> newTimeout)
    getLoggedUser(session).map(
      _ match {
        case Right(user) =>
          action(user).withSession(updatedSession)
        case Left(optError) => {
          def action = Redirect(routes.Application.loginGet).withNewSession
          optError match {
            case Some(error) => action.flashing("error" -> error)
            case None => action
          }
        }
      }
    )
  }

  def doWithoutSession(result: Result, session: Session) = {
    session.get("user") match {
      case Some(user) => Redirect(routes.Application.index)
      case None => result
    }
  }

  def loginGet = Action { implicit request =>
    doWithoutSession(Ok(views.html.loginForm()), request.session)
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
              "user" -> loginData.login,
              "expires" -> newTimeout
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
    doWithoutSession(
      Ok(views.html.registerForm(forms.RegisterData.form)),
      request.session
    )
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
              "user" -> user.login,
              "expires" -> newTimeout
            )
          }
        )
      }
    )
  }

  def index = Action.async {implicit request =>
    doWithSession(user => Ok(views.html.index(user)), request.session)
  }

}
