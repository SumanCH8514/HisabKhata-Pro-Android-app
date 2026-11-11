package com.sumanonline.hisabkhata.core.common

import com.sumanonline.hisabkhata.BuildConfig

object Constants {
    val FIREBASE_API_KEY: String
        get() = BuildConfig.FIREBASE_API_KEY

    const val BACKEND_BASE_URL = "https://backend.hisabkhata.sumanonline.com/"
    const val CDN_BASE_URL = "https://cdn.backend.hisabkhata.sumanonline.com/"
    const val FIREBASE_RTDB_BASE_URL = "https://hisabkhata-sumanonline-default-rtdb.asia-southeast1.firebasedatabase.app/"
    const val WEB_PORTAL_BASE_URL = "https://hisabkhata.sumanonline.com/"
    const val GOOGLE_WEB_CLIENT_ID = "788974402623-b8pq007v69mq9mr5p0solo6ls64roifd.apps.googleusercontent.com"
    const val APP_VERSION = "1.1.0"
    const val APP_BUILD_NUMBER = "2"

    const val TX_TYPE_GAVE = "GAVE"
    const val TX_TYPE_GOT = "GOT"

    const val PARTY_TYPE_CUSTOMER = "Customer"
    const val PARTY_TYPE_SUPPLIER = "Supplier"

    const val SYNC_STATUS_PENDING = "PENDING_SYNC"
    const val SYNC_STATUS_SYNCING = "SYNCING"
    const val SYNC_STATUS_SYNCED = "SYNCED"
    const val SYNC_STATUS_FAILED = "FAILED"
    const val SYNC_STATUS_NEEDS_REVIEW = "NEEDS_REVIEW"

    const val OP_CREATE_CUSTOMER = "CREATE_CUSTOMER"
    const val OP_UPDATE_CUSTOMER = "UPDATE_CUSTOMER"
    const val OP_DELETE_CUSTOMER = "DELETE_CUSTOMER"
    const val OP_CREATE_TRANSACTION = "CREATE_TRANSACTION"
    const val OP_UPDATE_TRANSACTION = "UPDATE_TRANSACTION"
    const val OP_DELETE_TRANSACTION = "DELETE_TRANSACTION"

    const val SYNC_WORK_NAME = "hisabkhata_sync_worker"
}
