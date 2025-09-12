package services

import javax.inject.{Inject, Singleton}
import repositories.CustomersRepo
import models.Customers
import scala.concurrent.{ExecutionContext, Future}

// Service layer for handling all customer-related business logic, delegating database operations to CustomersRepo
@Singleton
class CustomerService @Inject()(customersRepo: CustomersRepo)(implicit ec: ExecutionContext) {

  // Registers a new customer and returns either an error message or the newly created customer ID
  def registerCustomer(cust: Customers): Future[Either[String, Long]] =
    customersRepo.addCustomer(cust)

  // Fetches a single customer by their ID, returns Some(customer) if found, None otherwise
  def fetchCustomer(id: Long): Future[Option[Customers]] =
    customersRepo.getById(id)

  // Retrieves all customers from the database as a sequence
  def fetchAll(): Future[Seq[Customers]] =
    customersRepo.getAll()

  // Validates customer login credentials, returns true if successful, false otherwise
  def login(name: String, password: String): Future[Boolean] =
    customersRepo.login(name, password)

  // Updates the phone number of a customer by ID, returns the number of rows updated (0 if not found)
  def updatePhone(id: Long, newPhone: String): Future[Int] =
    customersRepo.updatePhone(id, newPhone)

  // Deletes a customer by ID, returns the number of rows deleted (0 if not found)
  def removeCustomer(id: Long): Future[Int] =
    customersRepo.deleteCustomer(id)
}
