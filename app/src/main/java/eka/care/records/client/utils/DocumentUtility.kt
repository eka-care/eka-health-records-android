package eka.care.records.client.utils

import eka.care.documents.R
import eka.care.records.client.model.DocumentTypeModel

class DocumentUtility {
    companion object{
        val docTypes = listOf(
            DocumentTypeModel(
                documentType = "Lab Report",
                filter = "Lab Reports",
                id = "lr",
                idNew = 1,
                icon = R.drawable.ic_lab_report
            ), DocumentTypeModel(
                documentType = "Prescription",
                filter = "Prescriptions",
                id = "ps",
                idNew = 2,
                icon = R.drawable.ic_prescription_new
            ), DocumentTypeModel(
                documentType = "Insurance",
                filter = "Insurances",
                id = "in",
                idNew = 5,
                icon = R.drawable.ic_insurance
            ), DocumentTypeModel(
                documentType = "Scan",
                filter = "Scans",
                id = "sc",
                idNew = 7,
                icon = R.drawable.ic_prescription_new
            ), DocumentTypeModel(
                documentType = "Discharge Summary",
                filter = "Discharge Summaries",
                id = "ds",
                idNew = 3,
                icon = R.drawable.ic_prescription_new
            ), DocumentTypeModel(
                documentType = "Vaccine Certificate",
                filter = "Vaccines Certificates",
                id = "vc",
                idNew = 4,
                icon = R.drawable.ic_lab_report
            ), DocumentTypeModel(
                documentType = "Invoice",
                filter = "Invoices",
                id = "iv",
                idNew = 6,
                icon = R.drawable.ic_prescription_new
            ), DocumentTypeModel(
                documentType = "Other",
                filter = "Others",
                id = "ot",
                idNew = 8,
                icon = R.drawable.ic_prescription_new
            )
        )
    }
}