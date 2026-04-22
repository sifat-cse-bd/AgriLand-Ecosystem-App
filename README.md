🌱 AgroVenture – Smart Agricultural Service Platform (Android App)
📌 Project Overview

AgroVenture is a fully offline-capable Android application designed to connect Admins, Landowners, Farmers, and Green AgroVenture Owners within a single agricultural ecosystem.

Unlike the previous web-based version, this app uses SQLite for local data storage, enabling fast performance, offline usability, and simplified deployment.

👥 Types of Users
👑 Admin
🏡 Landowner
🌾 Farmer
🚜 Green AgroVenture Owner
🔐 Common Features (Available to All Users)
🔑 Authentication
User Registration (stored locally in SQLite)
Login / Logout system
Session handling
👤 Account Management
View Profile
Edit Profile
Delete Account
Change Password
📊 Dashboard
Role-based dashboard
Personalized UI for each user type
🛠️ Features by User Type
👑 Admin Features
Manage all users (CRUD operations)
Verify user accounts
Monitor all services
Approve or reject services
🏡 Landowner Features
Hire farmers (seasonal/permanent)
Request agricultural services
Provide ratings and feedback
🌾 Farmer Features
View job opportunities
Apply for jobs
Rent equipment
Collaborate with service providers
🚜 Green AgroVenture Owner Features
Manage equipment rental listings
Handle irrigation projects
Provide cultivation services
Manage harvesting services
🧩 Technology Stack
📱 Android App
Language: Java / Kotlin
UI: XML (Material Design)
Architecture: MVVM / MVC
🗄️ Local Database
SQLite
SQLiteOpenHelper / Room Database (recommended)
🛠️ Tools
Android Studio
RecyclerView (for lists)
Glide/Picasso (optional for images)
