package forms

import play.api.data._
import play.api.data.Forms._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import models.User

case class RegisterData(login: String, name: String, email: String, password: String, passwordConfirm: String)

object RegisterData {
  private val timeout = 150 milli;
  def form = Form(
    mapping(
      "login" -> nonEmptyText.verifying(
        "error.login.taken", login =>
        Await.result(User.getByLogin(login).map(_ == None), timeout)
      ),
      "name" -> nonEmptyText,
      "email" -> email.verifying(
        "error.email.stored", email =>
        Await.result(User.getByEmail(email).map(_ == None), timeout)
        ),
      "password"-> nonEmptyText,
      "passwordConfirm" -> text
    )(RegisterData.apply)(RegisterData.unapply).verifying(
      "error.password.nomatch", input =>
       input.password == input.passwordConfirm
    )
  )

}
