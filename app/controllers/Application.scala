package controllers

import java.sql.Timestamp

import play.api.mvc._
import play.api.db.slick._
import models.Tables._
import profile.simple._

import play.api.data._
import play.api.data.Forms._

import org.mindrot.jbcrypt.BCrypt.{hashpw, checkpw, gensalt}

object Application extends Controller {
  val salt = """$2a$10$zXdoVN2Xci3bRB8UwnEL7u"""

  case class RegisterForm(name: String, mail: String, password: String)

  val registerForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "mail" -> email,
      "password" -> nonEmptyText
    )(RegisterForm.apply)(RegisterForm.unapply)
  )

  def index = Action {
    Ok(views.html.index(registerForm))
  }

  /**
   * デバッグ表示、テーブルをすべて出力する
   */
  def debug = DBAction.transaction { implicit rs =>
    val members = MemberTable.sortBy(_.timestampCreated).list

    Ok(views.html.debug(members))
  }

  /**
   * 登録実行
   */
  def create = DBAction.transaction { implicit rs =>
    registerForm.bindFromRequest.fold(
      error => Redirect(routes.Application.index),
      form => {
        // ユーザを登録
        val timestamp = new Timestamp(System.currentTimeMillis())
        val user = MemberTableRow(form.name, None, hashpw(form.password, salt), form.mail, timestamp, Some(timestamp))
        MemberTable.insert(user)

        Redirect(routes.Application.debug)
      }
    )
  }

}