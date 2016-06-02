package forms

import play.api.data._
import play.api.data.Forms._
import models.User

case class RegisterData(login: String, name: String, email: String, password: String, passwordConfirm: String)

object RegisterData {

  def form = Form(
    mapping(
      "login" -> nonEmptyText.verifying(
        "error.login.taken", login =>
        User.getByLogin(login) == None
      ),
      "name" -> nonEmptyText,
      "email" -> email.verifying(
        "error.email.stored", email =>
        User.getByEmail(email) == None
      ),
      "password"-> nonEmptyText,
      "passwordConfirm" -> text
    )(RegisterData.apply)(RegisterData.unapply).verifying(
      "error.password.nomatch", input =>
       input.password == input.passwordConfirm
    )
  )

}
