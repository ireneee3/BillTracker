
package com.example.billtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Bill::class, Payment::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao

    abstract fun paymentDao(): PaymentDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                // Add the category column to the bills table.
                db.execSQL(
                    "ALTER TABLE bills ADD COLUMN category TEXT NOT NULL DEFAULT ''"
                )

                // Remove any orphaned payments before adding the foreign key.
                db.execSQL(
                    """
                    DELETE FROM payments
                    WHERE billId NOT IN (SELECT id FROM bills)
                    """.trimIndent()
                )

                // Create the replacement payments table with a foreign key.
                db.execSQL(
                    """
                    CREATE TABLE payments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        billId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        paymentDate TEXT NOT NULL,
                        FOREIGN KEY(billId)
                            REFERENCES bills(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Copy existing payment records.
                db.execSQL(
                    """
                    INSERT INTO payments_new (id, billId, amount, paymentDate)
                    SELECT id, billId, amount, paymentDate
                    FROM payments
                    """.trimIndent()
                )

                // Replace the old table.
                db.execSQL("DROP TABLE payments")
                db.execSQL("ALTER TABLE payments_new RENAME TO payments")

                // Create the index required by Room.
                db.execSQL(
                    "CREATE INDEX index_payments_billId ON payments (billId)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bill_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
