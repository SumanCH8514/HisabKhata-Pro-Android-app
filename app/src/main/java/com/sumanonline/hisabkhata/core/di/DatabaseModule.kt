package com.sumanonline.hisabkhata.core.di

import android.content.Context
import androidx.room.Room
import com.sumanonline.hisabkhata.core.database.HisabKhataDatabase
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.OutboxDao
import com.sumanonline.hisabkhata.core.database.dao.PendingPaymentDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HisabKhataDatabase {
        return Room.databaseBuilder(
            context,
            HisabKhataDatabase::class.java,
            "hisabkhata_local_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCustomerDao(db: HisabKhataDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideTransactionDao(db: HisabKhataDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideOutboxDao(db: HisabKhataDatabase): OutboxDao = db.outboxDao()

    @Provides
    fun providePendingPaymentDao(db: HisabKhataDatabase): PendingPaymentDao = db.pendingPaymentDao()
}
