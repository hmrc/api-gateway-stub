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

package uk.gov.hmrc.apigatewaystub.repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.apigatewaystub.models.Session

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionRepository @Inject()(mongo: ReactiveMongoComponent)
  extends ReactiveRepository[Session, BSONObjectID]("session", mongo.mongoConnector.db,
    Session.formatSession, ReactiveMongoFormats.objectIdFormats) {

    def save(session: Session): Future[Session] = {
      collection.findAndUpdate(selector = Json.obj("cookie" -> session.cookie), session, upsert = true).map(_ => session)
    }

    def fetch(cookie: String): Future[Option[Session]] = {
      collection.find(Json.obj("cookie" -> cookie)).one[Session]
    }

    def delete(cookie: String): Future[Unit] = {
      collection.remove(Json.obj("cookie" -> cookie)).map(_ => ())
    }
  }
