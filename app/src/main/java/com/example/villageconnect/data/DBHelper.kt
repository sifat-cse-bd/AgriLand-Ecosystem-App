package com.example.villageconnect.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {

        // Database Info
        const val DATABASE_NAME = "village_connect.db"
        const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_FARMER_PROFILES = "farmer_profiles"
        const val TABLE_MERCHANT_ASSETS = "merchant_assets"
        const val TABLE_SERVICE_BOOKINGS = "service_bookings"
        const val TABLE_INVENTORY = "inventory"
        const val TABLE_INVENTORY_ORDERS = "inventory_orders"
        const val TABLE_HIRE_REQUESTS = "hire_requests"

        // Common Columns
        const val COL_ID = "id"
        const val COL_STATUS = "status"

        // Users Columns
        const val COL_FULL_NAME = "full_name"
        const val COL_NID = "nid"
        const val COL_PHONE = "phone"
        const val COL_DOB = "dob"
        const val COL_PASSWORD = "password"
        const val COL_ROLE = "role"
        const val COL_DISTRICT = "district"
        const val COL_UPAZILA = "upazila"
        const val COL_VILLAGE_NAME = "village_name"

        // Farmer Profile Columns
        const val COL_USER_ID = "user_id"
        const val COL_SKILLS = "skills"
        const val COL_EXPERIENCE = "experience"
        const val COL_DAILY_WAGE = "daily_wage"
        const val COL_BIO = "bio"

        // Merchant Asset Columns
        const val COL_MERCHANT_ID = "merchant_id"
        const val COL_ASSET_TYPE = "asset_type"
        const val COL_TOTAL_CAPACITY = "total_capacity"

        // Service Booking Columns
        const val COL_LANDOWNER_ID = "landowner_id"
        const val COL_ASSET_ID = "asset_id"
        const val COL_SERVICE_TYPE = "service_type"
        const val COL_QUEUE_NO = "queue_no"
        const val COL_BOOKING_DATE = "booking_date"

        // Inventory Columns
        const val COL_ITEM_NAME = "item_name"
        const val COL_CATEGORY = "category"
        const val COL_PRICE = "price"
        const val COL_STOCK_QTY = "stock_qty"

        // Inventory Order Columns
        const val COL_INVENTORY_ID = "inventory_id"
        const val COL_QUANTITY = "quantity"
        const val COL_TOTAL_PRICE = "total_price"
        const val COL_ORDER_DATE = "order_date"

        // Hire Request Columns
        const val COL_FARMER_ID = "farmer_id"
        const val COL_WORK_DATE = "work_date"

        // Role Values
        const val ROLE_LANDOWNER = "landowner"
        const val ROLE_FARMER = "farmer"
        const val ROLE_MERCHANT = "merchant"

        // Status Values
        const val STATUS_PENDING = "Pending"
        const val STATUS_ACCEPTED = "Accepted"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_AVAILABLE = "Available"
        const val STATUS_ON_WORK = "On-Work"

        // Create Users Table
        const val CREATE_USERS_TABLE = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                nid TEXT UNIQUE NOT NULL,
                phone TEXT UNIQUE NOT NULL,
                dob TEXT,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                district TEXT DEFAULT NULL,
                upazila TEXT DEFAULT NULL,
                village_name TEXT DEFAULT NULL
            )
        """

        // Create Farmer Profiles Table
        const val CREATE_FARMER_PROFILES_TABLE = """
            CREATE TABLE farmer_profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                skills TEXT,
                experience TEXT,
                daily_wage REAL,
                bio TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """

        // Create Merchant Assets Table
        const val CREATE_MERCHANT_ASSETS_TABLE = """
            CREATE TABLE merchant_assets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                merchant_id INTEGER,
                asset_type TEXT,
                status TEXT DEFAULT 'Available',
                total_capacity REAL,
                FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """

        // Create Service Bookings Table
        const val CREATE_SERVICE_BOOKINGS_TABLE = """
            CREATE TABLE service_bookings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                landowner_id INTEGER,
                merchant_id INTEGER,
                asset_id INTEGER,
                service_type TEXT,
                queue_no INTEGER,
                status TEXT DEFAULT 'Pending',
                booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (landowner_id) REFERENCES users(id),
                FOREIGN KEY (merchant_id) REFERENCES users(id),
                FOREIGN KEY (asset_id) REFERENCES merchant_assets(id)
            )
        """

        // Create Inventory Table
        const val CREATE_INVENTORY_TABLE = """
            CREATE TABLE inventory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                merchant_id INTEGER,
                item_name TEXT NOT NULL,
                category TEXT,
                price REAL,
                stock_qty INTEGER,
                FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """

        // Create Inventory Orders Table
        const val CREATE_INVENTORY_ORDERS_TABLE = """
            CREATE TABLE inventory_orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                landowner_id INTEGER,
                merchant_id INTEGER,
                inventory_id INTEGER,
                quantity INTEGER,
                total_price REAL,
                status TEXT DEFAULT 'Pending',
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (landowner_id) REFERENCES users(id),
                FOREIGN KEY (merchant_id) REFERENCES users(id),
                FOREIGN KEY (inventory_id) REFERENCES inventory(id)
            )
        """

        // Create Hire Requests Table
        const val CREATE_HIRE_REQUESTS_TABLE = """
            CREATE TABLE hire_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                landowner_id INTEGER,
                farmer_id INTEGER,
                work_date TEXT,
                status TEXT DEFAULT 'Pending',
                FOREIGN KEY (landowner_id) REFERENCES users(id),
                FOREIGN KEY (farmer_id) REFERENCES users(id)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON")

        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_FARMER_PROFILES_TABLE)
        db.execSQL(CREATE_MERCHANT_ASSETS_TABLE)
        db.execSQL(CREATE_SERVICE_BOOKINGS_TABLE)
        db.execSQL(CREATE_INVENTORY_TABLE)
        db.execSQL(CREATE_INVENTORY_ORDERS_TABLE)
        db.execSQL(CREATE_HIRE_REQUESTS_TABLE)

        createIndexes(db)
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_role_village ON users(role, village_name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_farmer_profiles_user ON farmer_profiles(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_assets_merchant ON merchant_assets(merchant_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_service_landowner ON service_bookings(landowner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_service_merchant ON service_bookings(merchant_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_service_asset ON service_bookings(asset_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_inventory_merchant ON inventory(merchant_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_inventory_category ON inventory(category)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_order_landowner ON inventory_orders(landowner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_order_merchant ON inventory_orders(merchant_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hire_landowner ON hire_requests(landowner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hire_farmer ON hire_requests(farmer_id)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys = ON")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS hire_requests")
        db.execSQL("DROP TABLE IF EXISTS inventory_orders")
        db.execSQL("DROP TABLE IF EXISTS inventory")
        db.execSQL("DROP TABLE IF EXISTS service_bookings")
        db.execSQL("DROP TABLE IF EXISTS merchant_assets")
        db.execSQL("DROP TABLE IF EXISTS farmer_profiles")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

}