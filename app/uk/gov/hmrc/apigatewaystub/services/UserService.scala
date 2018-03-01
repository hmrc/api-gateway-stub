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

package uk.gov.hmrc.apigatewaystub.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.apigatewaystub.models.{Session, User}
import uk.gov.hmrc.apigatewaystub.repository.{SessionRepository, UserRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserService @Inject()(userRepository: UserRepository, sessionRepository: SessionRepository) {

  def createUser(username: String, password: String): Future[User] = {
    userRepository.save(User(username, password))
  }

  def login(username: String, password: String): Future[Session] = {
    userRepository.fetch(username).flatMap { maybeUser =>
      maybeUser.fold {
        Future.failed[Session](new IllegalArgumentException)
      }{ _ =>
        sessionRepository.save(Session.newSession)
      }
    }
  }

  def logout(cookie: String): Future[Unit] = {
    sessionRepository.delete(cookie)
  }
}
