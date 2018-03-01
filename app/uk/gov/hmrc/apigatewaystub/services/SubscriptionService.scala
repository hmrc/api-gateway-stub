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
class SubscriptionService @Inject()(applicationRepository: ApplicationRepository) {

  def addSubscription(applicationName: String, apiName: String, apiVersion: String, tier: String): Future[Unit] = {
    applicationRepository.fetch(applicationName).flatMap { maybeApplication =>
      maybeApplication.fold {
        Future.failed[Unit](new IllegalArgumentException)
      } { application =>
        applicationRepository.save(
          application.copy(subscriptions = application.subscriptions :+ ApplicationSubscription(apiName, apiVersion, tier))
        ).map(_ => ())
      }
    }
  }

  def removeSubscription(applicationName: String, apiName: String, apiVersion: String): Future[Unit] = {
    applicationRepository.fetch(applicationName).flatMap { maybeApplication =>
      maybeApplication.fold {
        Future.failed[Unit](new IllegalArgumentException)
      } { application =>
        applicationRepository.save(
          application.copy(subscriptions = application.subscriptions.filter(subscription =>
            subscription.name == apiName && subscription.version == apiVersion)
          )
        ).map(_ => ())
      }
    }
  }

  def fetchSubscriptions(applicationName: String): Future[Seq[ApplicationSubscription]] = {
    applicationRepository.fetch(applicationName).map(_.fold(Seq.empty[ApplicationSubscription])(_.subscriptions))
  }
}
