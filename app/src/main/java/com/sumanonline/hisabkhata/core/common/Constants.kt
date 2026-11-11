package com.sumanonline.hisabkhata.core.common

object Constants {
    const val FIREBASE_API_KEY = "AIzaSyB3mvgy7spK_7mQaeby18WiY_GbS3A7yv0"
    const val BACKEND_BASE_URL = "https://backend.hisabkhata.sumanonline.com/"
    const val CDN_BASE_URL = "https://cdn.backend.hisabkhata.sumanonline.com/"
    const val FIREBASE_RTDB_BASE_URL = "https://hisabkhata-sumanonline-default-rtdb.asia-southeast1.firebasedatabase.app/"
    const val WEB_PORTAL_BASE_URL = "https://hisabkhata.sumanonline.com/"

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
