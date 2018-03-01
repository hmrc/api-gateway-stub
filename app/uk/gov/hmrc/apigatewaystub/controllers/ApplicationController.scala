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

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.apigatewaystub.models.Application._
import uk.gov.hmrc.apigatewaystub.services.{ApplicationService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApplicationController @Inject()(applicationService: ApplicationService, subscriptionService: SubscriptionService) extends BaseController {

  def fetchApplication(applicationName: String) = Action.async {
    for {
      maybeApplication <- applicationService.fetchApplication(applicationName)
    } yield maybeApplication match {
      case Some(application) => Ok(Json.obj(
        "error" -> false,
        "application" -> Json.obj("tier" -> application.tier)
      )).as(JSON)
      case _ => NotFound("")
    }
  }

  def createApplication() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val applicationName = requestParams("application").head
      val tier = requestParams("tier").head
      applicationService.createApplication(applicationName, tier).map(_ => successResponse)
    }
  }

  def updateApplication() = Action.async {implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val applicationName = requestParams("application").head
      val tier = requestParams("tier").head
      applicationService.updateApplication(applicationName, tier).map(_ => successResponse)
    }
  }

  def deleteApplication() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val applicationName = requestParams("application").head
      applicationService.deleteApplication(applicationName).map(_ => successResponse)
    }
  }

  def addSubscriptionOrGenerateApplicationKey() = Action.async { implicit request =>
    def generateApplicationKey(requestParams: Map[String, Seq[String]]) = {
      val applicationName = requestParams("application").head
      val keyType = requestParams("keytype").head
      applicationService.generateApplicationKey(applicationName, keyType).map { key =>
        Ok(Json.obj(
          "error" -> false,
          "data" -> Json.obj(
            "key" -> Json.toJson(key)
          )
        )).as(JSON)
      }
    }

    def addSubscription(requestParams: Map[String, Seq[String]]) = {
      val applicationName = requestParams("applicationName").head
      val apiName = requestParams("name").head
      val apiVersion = requestParams("version").head
      val tier = requestParams("tier").head
      subscriptionService.addSubscription(applicationName, apiName, apiVersion, tier).map(_ => successResponse)
    }

    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val action = requestParams("action").head
      
      action match {
        case "addAPISubscription" => addSubscription(requestParams)
        case _ => generateApplicationKey(requestParams)
      }
    }
  }
  
  def removeSubscription() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val applicationName = requestParams("applicationName").head
      val apiName = requestParams("name").head
      val apiVersion = requestParams("version").head
      subscriptionService.removeSubscription(applicationName, apiName, apiVersion).map(_ => successResponse)
    }
  }

  def fetchSubscriptions() = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(badParametersResponse) { requestParams =>
      val applicationName = requestParams("applicationName").head
      subscriptionService.fetchSubscriptions(applicationName).map { subscriptions =>
        Ok(Json.obj(
          "error" -> false,
          "apis" -> subscriptions.map { subscription =>
            Json.obj(
              "apiName" -> subscription.name,
              "apiVersion" -> subscription.version
            )
          }
        )).as(JSON)
      }
    }
  }
  
  private def successResponse = {
    Ok("""{"error":false}"""").as(JSON)
  }

  private def badParametersResponse = {
    Future.successful(BadRequest("Missing parameters"))
  }
}
