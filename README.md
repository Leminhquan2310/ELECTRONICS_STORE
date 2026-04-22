# 🛒 Electronics Store Web Application

A full-stack e-commerce web application for an electronics store, supporting product browsing, shopping cart, order management, and secure online payment.

---

## 🚀 Overview

This project is a web-based electronics store system that allows users to browse products, manage carts, place orders, and perform online payments. It also includes an admin panel for managing products, categories, and orders.

Built with **Spring MVC architecture**, the system ensures clear separation of concerns, maintainability, and scalability.

---

## ✨ Features

- 🛍️ **Product Management**
  - Browse products by category
  - View product details (image, price, description)

- 🛒 **Shopping Cart**
  - Add / remove products
  - Update quantity
  - Calculate total price

- 📦 **Order Processing**
  - Place orders
  - Track order status

- 💳 **Payment (VNPay API)**
  - Secure online payment integration
  - Transaction verification and callback handling

- 🔐 **Spring Security (RBAC)**
  - Login / Register system
  - Role-based access control (Admin / User)

- 🗂️ **Category System (Hierarchical Design)**
  - Multi-level category structure for scalability

- ⚡ **Caffeine Cache**
  - Improve performance and reduce response time

- ☁️ **Cloudinary**
  - Upload and manage product images efficiently

---

## 🛠️ Tech Stack

- **Backend:** Spring MVC, Spring Security, Spring Data JPA  
- **Frontend:** Thymeleaf  
- **Database:** MySQL  
- **Payment:** VNPay API  
- **Cloud Storage:** Cloudinary  
- **Caching:** Caffeine Cache  

---

## 📂 Project Structure
```
src/main/java/com/yourproject
├── controller
├── service
├── repository
├── entity
├── config
├── dto
└── exception
```


---

## ⚙️ Installation & Setup

### 1. Clone repository

```bash
git clone https://github.com/Leminhquan2310/ELECTRONICS_STORE.git
cd ELECTRONICS_STORE
```
### 2. Setup MySQL database
```
CREATE DATABASE electronics_store;
```

### 3. Configure application

Update file:
```
src/main/resources/application.properties
```

Example:
```
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/electronics_store
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

vnpay.tmnCode=your_tmn_code
vnpay.hashSecret=your_hash_secret
vnpay.returnUrl=http://localhost:8080/payment-return

cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

### 4. Build project
```
mvn clean install
```
### 5. Run application
```
mvn spring-boot:run
```
Application runs at:
```
http://localhost:8080
```
🔌 Usage

User

- Browse products
- Add to cart
- Checkout and pay

Admin

- Manage products
- Manage categories
- Manage orders

📈 Future Improvements

- REST API + React frontend
- Redis caching
- Docker & CI/CD
- Recommendation system

👨‍💻 Author

- GitHub: https://github.com/Leminhquan2310

📄 License

This project is for educational and portfolio purposes.
