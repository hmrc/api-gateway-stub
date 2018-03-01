/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apigatewaystub.controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc._
import uk.gov.hmrc.apigatewaystub.services.UserService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(userService: UserService) extends BaseController {

  def login() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { params =>
      val username = params("username").head
      val password = params("password").head
      userService.login(username, password).map { session =>
        successResponse.withCookies(Cookie("session", session.cookie))
      }
    }
  }

  def logout() = Action.async { implicit request =>
    request.cookies.get("session").fold(badParametersResponse) { session =>
      userService.logout(session.value).map(_ => successResponse)
    }
  }

  def createUser() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { params =>
      val username = params("username").head
      val password = params("password").head
      userService.createUser(username, password).map(_ => successResponse)
    }
  }

  private def successResponse = {
    Ok("""{"error":false}"""").as(JSON)
  }

  private def badParametersResponse = {
    Future.successful(BadRequest("Missing parameters"))
  }
}
