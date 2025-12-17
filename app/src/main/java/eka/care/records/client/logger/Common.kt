package eka.care.records.client.logger

import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.data.utility.LoggerConstant.Companion.BUSINESS_ID
import eka.care.records.data.utility.LoggerConstant.Companion.CASE_ID
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
import eka.care.records.data.utility.LoggerConstant.Companion.OWNER_ID

internal fun logRecordSyncEvent(
    dId: String? = null,
    caseId: String? = null,
    bId: String,
    oId: String,
    msg: String
) {
    Records.logEvent(
        EventLog(
            params = mutableMapOf<String, Any?>().also { param ->
                param.put(DOCUMENT_ID, dId)
                param.put(BUSINESS_ID, bId)
                param.put(CASE_ID, caseId)
                param.put(OWNER_ID, oId)
            },
            message = msg
        )
    )
}