
# 🏷 Stock Alert System (Microservices)🛒

A two-service system built in Scala:

- **Stock Alert Service** — REST API (Play Framework) that manages items, orders, and monitors stock levels. When stock falls below a minimum threshold, it triggers a notification.
- **Notification Service (Noti)** — gRPC server (Play Framework) that receives low-stock alerts and stores notifications in a MySQL database.

---

##  Architecture Overview


  Stock Service
    A[Stock Alert: REST Endpoints (Play)] --> D[(MySQL: stockdb via Slick)]
    A -->|gRPC| B(Noti Service)
  
  Noti Service
    B --> E[(MySQL: notidb via Slick)]
  
  Tests[Test via ScalaTest] --> A
  Tests --> B


* **REST endpoints** for items & orders.
* **Slick + Evolutions** for DB.
* **gRPC communication** with Notification Service.
* **ScalaTest** for unit/integration tests.

---

## Services & Endpoints

### Stock Alert Service (`stock-alert-service`)

| Method | Endpoint   | Description                     |
| ------ | ---------  | ------------------------------- |
| `POST` | `/items`   | Create a new item (stock entry) |
| `GET`  | `/items`   | List all items                  |
| `GET`  | `/items:id`| Get a item                      |
| `POST` | `/orders`  | Place an order, reducing stock  |
| `GET`  | `/orders`  | List all orders                 |

Place an order that drops `quantity < minStock` → triggers gRPC call to Notification Service.

---

### Notification Service (`noti`)

| Method | Endpoint           | Description                     |
| ------ | ------------------ | ------------------------------- |
| gRPC   | `Notify(lowStock)` | Receive low-stock event         |
| `GET`  | `/notifications`   | List all recorded notifications |

---

## Example Payloads

### Create Item

```json
POST /items
{
  "name": "Dress",
  "stock": 10,
  "minStock": 5
}
```

### Place Order

```json
POST /orders
{
  "item": 1,
  "qty": 7
}
```

### Notification Data (received by Noti service)

```json
{
  "itemId": 1,
  "message": "Item 'Widget' is low on stock!"
}
```

---

## Tech Stack

| Component     | Technology                 |
| ------------- | -------------------------- |
| Language      | Scala                      |
| Frameworks    | Play Framework, ScalaPB    |
| DB Access     | Slick + Evolutions (MySQL) |
| Communication | gRPC via ScalaPB stubs     |
| Testing       | ScalaTest (with Mockito)   |
| Containers    | Docker + Docker Compose    |

---

## Testing

* ScalaTest covers controllers, services, and gRPC interaction.
* Example: ordering below `minStock` should assert that Notification Service is invoked (mocked).
* Run tests:

```bash
sbt test
```

---

## Running with Docker Compose

```bash
docker-compose up --build
```

This brings up:

* `stock-alert-service` at **localhost:9002** (REST)
* `noti` service for notifications via gRPC and REST at **localhost:9001**

Endpoints:

* Stock:

  * `/items`, `/orders`
* Notifications:

  * `/notifications`

---

## Project Structure

```
├── stock-alert
│   ├── app
│   │   ├── controllers
│   │   ├── models
│   │   ├── repositories
│   │   ├── services
│   │   └── modules
│   ├── docker-compose.yml
│   ├── Dockerfile (for each service)
│   ├── conf
│   ├── test
│   └── build.sbt
├── noti
│   ├── src\main\scala\shared\notification
│   │   ├── repositories
│   │   ├── services
│   │   NotificationServer 
│   ├── docker-compose.yml
│   ├── Dockerfile (for each service)
│   ├── conf
│   ├── test
│   └── build.sbt
└── README.md
```

---

## Contribution

1. Fork the repo
2. Create your feature branch
3. Run tests locally
4. Commit and push :-)
