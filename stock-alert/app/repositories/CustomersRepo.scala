package repositories

import models.Customers
import tables.CustomersTable
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

// Repository for managing Customer-related database operations
@Singleton
class CustomersRepo @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  private val customers = TableQuery[CustomersTable]

  // Adds a new customer after checking for existing email or name
  // Returns Right(id) if added successfully, Left(errorMessage) if already exists
  def addCustomer(c: Customers): Future[Either[String, Long]] = {
    db.run(customers.filter(_.email === c.email).result.headOption).flatMap {
      case Some(_) => Future.successful(Left(s"Customer with email ${c.email} already exists"))
      case None =>
        db.run(customers.filter(_.name === c.name).result.headOption).flatMap {
          case Some(_) => Future.successful(Left(s"Customer with name ${c.name} already exists"))
          case None =>
            db.run((customers returning customers.map(_.id)) += c).map(Right(_))
        }
    }
  }

  // Fetches a customer by their ID
  def getById(id: Long): Future[Option[Customers]] =
    db.run(customers.filter(_.id === id).result.headOption)

  // Fetches all customers in the database
  def getAll(): Future[Seq[Customers]] =
    db.run(customers.result)

  // Updates the phone number of a customer by ID
  // Returns the number of rows affected
  def updatePhone(id: Long, phone: String): Future[Int] =
    db.run(customers.filter(_.id === id).map(_.phone).update(phone))

  // Deletes a customer by ID
  // Returns the number of rows affected
  def deleteCustomer(id: Long): Future[Int] =
    db.run(customers.filter(_.id === id).delete)

  // Adds a new notification to a customer
  // If notifications already exist, appends with a comma separator
  // Returns the number of rows affected
  def updateNotifications(id: Long, notification: String): Future[Int] = {
    db.run(customers.filter(_.id === id).result.headOption).flatMap {
      case Some(cust) =>
        val updatedCust = cust.copy(
          notifications =
            if (cust.notifications.isEmpty) notification
            else cust.notifications + " ," + notification
        )
        db.run(customers.filter(_.id === id).update(updatedCust))
      case None => Future.successful(0)
    }
  }

  // Validates login credentials for a customer
  // Returns true if a matching customer is found, false otherwise
  def login(name: String, password: String): Future[Boolean] =
    db.run(customers.filter(c => c.name === name && c.password === password).result.headOption)
      .map(_.isDefined)
}
