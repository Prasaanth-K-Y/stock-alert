package controllers


import play.api.mvc._
import org.json.JSONObject
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import com.razorpay.RazorpayClient
import play.api.libs.json._
import utils.{JwtActionBuilder, Attrs}
import play.filters.csrf.{CSRF, CSRFAddToken}
import io.github.cdimascio.dotenv.Dotenv

@Singleton
class RazorpayTestController @Inject() (
  cc: ControllerComponents,
  jwtAction: JwtActionBuilder,

)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  

  //For local run
  // private val dotenv = Dotenv.load() 

  // Dontenv
  private val dotenv = Dotenv.configure().directory("/app").load()

  //Client Setup with KeyId , KeySecret
  private val client = new RazorpayClient(
    dotenv.get("RAZORPAY_KEY_ID"),
    dotenv.get("RAZORPAY_KEY_SECRET")
  )

  def createOrder(amount: Int): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    //Authoization for Customer
    if (user.role != "Customer") {
      Future.successful(Forbidden("Only Customers can create Razorpay orders"))
    } else {
      Future {
        val orderRequest = new JSONObject()
        orderRequest.put("amount", amount * 100) 
        orderRequest.put("currency", "INR")
        orderRequest.put("receipt", s"order_rcptid_${user.id.getOrElse(0L)}")
        orderRequest.put("payment_capture", 1)

        val order = client.orders.create(orderRequest)
        Ok(Json.parse(order.toString))
      }.recover {
        case ex: Exception =>
          InternalServerError(Json.obj("error" -> ex.getMessage))
      }
    }
  }
}
