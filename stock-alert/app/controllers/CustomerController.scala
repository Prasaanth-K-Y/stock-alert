package controllers

import play.api.mvc._
import javax.inject.Inject
import services.CustomerService
import models.Customers
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

// Controller for handling HTTP requests related to Customers
class CustomerController @Inject()(
    cc: ControllerComponents,
    service: CustomerService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Adds a new customer from JSON request body
  // Returns 201 Created on success, 409 Conflict if customer exists, 400 BadRequest if validation fails
  def addCustomer(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Customers].fold(
      err => Future.successful(BadRequest(JsError.toJson(err))),
      cust => service.registerCustomer(cust).map {
        case Left(msg) => Conflict(Json.obj("error" -> msg))
        case Right(id) => Created(Json.obj("id" -> id, "message" -> s"Customer ${cust.name} created"))
      }
    )
  }

  // Fetches a single customer by ID
  // Returns 200 OK with customer data or 404 NotFound if customer does not exist
  def getCustomer(id: Long): Action[AnyContent] = Action.async {
    service.fetchCustomer(id).map {
      case Some(cust) => Ok(Json.toJson(cust))
      case None       => NotFound(Json.obj("error" -> "Customer not found"))
    }
  }

  // Fetches all customers
  // Returns 200 OK with a JSON array of customers
  def getAll(): Action[AnyContent] = Action.async {
    service.fetchAll().map(custs => Ok(Json.toJson(custs)))
  }

  // Updates the phone number of a customer
  // Returns 200 OK if update succeeded, 404 NotFound if customer does not exist
  def updatePhone(id: Long, phone: String): Action[AnyContent] = Action.async {
    service.updatePhone(id, phone).map { updated =>
      if (updated > 0) Ok(Json.obj("message" -> s"Phone updated for $id"))
      else NotFound(Json.obj("error" -> "Customer not found"))
    }
  }

  // Deletes a customer by ID
  // Returns 200 OK on success, 404 NotFound if customer does not exist
  def deleteCustomer(id: Long): Action[AnyContent] = Action.async {
    service.removeCustomer(id).map { deleted =>
      if (deleted > 0) Ok(Json.obj("message" -> s"Customer $id deleted"))
      else NotFound(Json.obj("error" -> "Customer not found"))
    }
  }

  // Handles customer login
  // Validates JSON body for name and password, returns 200 OK if successful, 401 Unauthorized if invalid, 400 BadRequest if missing fields
  def login(): Action[JsValue] = Action.async(parse.json) { req =>
    (req.body \ "name").asOpt[String].zip((req.body \ "password").asOpt[String]) match {
      case Some((name, password)) =>
        service.login(name, password).map {
          case true  => Ok(Json.obj("message" -> "Login successful"))
          case false => Unauthorized(Json.obj("error" -> "Invalid credentials"))
        }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing name or password")))
    }
  }
}
