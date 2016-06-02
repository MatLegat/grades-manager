package forms

import play.api.data._
import play.api.data.Forms._

case class LoginData(login: String, password: String)

object LoginData {

  def form = Form(
    mapping(
      "login" -> text,
      "password"-> text
    )(LoginData.apply)(LoginData.unapply)
  )

}
