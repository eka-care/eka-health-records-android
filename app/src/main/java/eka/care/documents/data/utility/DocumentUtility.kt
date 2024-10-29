package eka.care.documents.data.utility

import eka.care.documents.R
import eka.care.documents.data.model.DocTypeModel

class DocumentUtility {
    companion object{
        const val PARAM_RECORD_PARAMS_MODEL = "records_params"
        const val FILES_DB_UPDATED_AT = "files_db_updated_at"

        val docTypes = listOf(
            DocTypeModel(
                documentType = "Lab Report",
                filter = "Lab Reports",
                id = "lr",
                idNew = 1,
                icon = R.drawable.ic_lab_report
            ), DocTypeModel(
                documentType = "Prescription",
                filter = "Prescriptions",
                id = "ps",
                idNew = 2,
                icon = R.drawable.ic_prescription_new
            ), DocTypeModel(
                documentType = "Insurance",
                filter = "Insurances",
                id = "in",
                idNew = 5,
                icon = R.drawable.ic_insurance
            ), DocTypeModel(
                documentType = "Scan",
                filter = "Scans",
                id = "sc",
                idNew = 7,
                icon = R.drawable.ic_prescription_new
            ), DocTypeModel(
                documentType = "Discharge Summary",
                filter = "Discharge Summaries",
                id = "ds",
                idNew = 3,
                icon = R.drawable.ic_prescription_new
            ), DocTypeModel(
                documentType = "Vaccine Certificate",
                filter = "Vaccines Certificates",
                id = "vc",
                idNew = 4,
                icon = R.drawable.ic_lab_report
            ), DocTypeModel(
                documentType = "Invoice",
                filter = "Invoices",
                id = "iv",
                idNew = 6,
                icon = R.drawable.ic_prescription_new
            ), DocTypeModel(
                documentType = "Other",
                filter = "Others",
                id = "ot",
                idNew = 8,
                icon = R.drawable.ic_prescription_new
            )
        )
    }
}