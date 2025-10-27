package controllers

import javax.inject._
import play.api.mvc._
import com.razorpay.RazorpayClient
import org.json.JSONObject

@Singleton
class RazorpayTestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Replace with your actual Test Keys
  // val client = new RazorpayClient("rzp_test_RTeOMdWBZh4V1U", "TKFcAIgU6VG7ULhvR3YoDA7d")

  def createOrder(amount: Int) = Action {
    val orderRequest = new JSONObject()
    orderRequest.put("amount", amount * 100) // in paise
    orderRequest.put("currency", "INR")
    orderRequest.put("receipt", "order_rcptid_11")
    orderRequest.put("payment_capture", 1)

    val order = client.orders.create(orderRequest)
    Ok(order.toString)
  }
}
