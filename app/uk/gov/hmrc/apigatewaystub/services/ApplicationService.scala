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

import uk.gov.hmrc.apigatewaystub.models._
import uk.gov.hmrc.apigatewaystub.repository.ApplicationRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApplicationService @Inject()(applicationRepository: ApplicationRepository) {

  def fetchApplication(applicationName: String): Future[Option[Application]] = {
    applicationRepository.fetch(applicationName)
  }

  def createApplication(applicationName: String, tier: String): Future[Application] = {
    applicationRepository.save(Application(applicationName, tier))
  }

  def updateApplication(applicationName: String, tier: String): Future[Application] = {
    applicationRepository.save(Application(applicationName, tier))
  }

  def deleteApplication(applicationName: String): Future[Unit] = {
    applicationRepository.remove(applicationName)
  }

  def generateApplicationKey(applicationName: String, keyType: String): Future[ApplicationKey] = {
    applicationRepository.fetch(applicationName).flatMap { maybeApplication =>
      maybeApplication.fold {
        Future.failed[ApplicationKey](new IllegalArgumentException)
      }{ application =>
        val key = ApplicationKey.generate
        applicationRepository.save(keyType match {
          case "PRODUCTION" => application.copy(productionKey = Some(key))
          case _ => application.copy(sandboxKey = Some(key))
        }).map(_ => key)
      }
    }
  }
}
