package com.goldtip.vivoledger.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.goldtip.vivoledger.data.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val exportFileTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.CHINA)

object LedgerExporter {
    fun exportToDocumentTree(
        context: Context,
        treeUri: Uri,
        format: ExportFormat,
        transactions: List<TransactionEntity>
    ): String {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
        val fileName = "vivo-ledger-${LocalDateTime.now().format(exportFileTimeFormatter)}.${format.extension}"
        val outputUri = DocumentsContract.createDocument(
            context.contentResolver,
            documentUri,
            format.mimeType,
            fileName
        ) ?: error("无法创建导出文件")

        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            if (format == ExportFormat.CSV) {
                outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            }
            outputStream.writer(Charsets.UTF_8).use { writer ->
                writer.write(
                    when (format) {
                        ExportFormat.CSV -> buildCsvExport(transactions)
                        ExportFormat.EXCEL_XML -> buildExcelXmlExport(transactions)
                        ExportFormat.JSON -> buildJsonExport(transactions)
                    }
                )
            }
        } ?: error("无法打开导出文件")

        return fileName
    }
}

private fun buildCsvExport(transactions: List<TransactionEntity>): String = buildString {
    appendLine("ID,日期,类型,类别,金额,说明")
    transactions.forEach { transaction ->
        appendLine(
            listOf(
                transaction.id.toString(),
                transaction.date.toString(),
                transaction.type.name,
                transaction.category,
                String.format(Locale.US, "%.2f", transaction.amount),
                transaction.note
            ).joinToString(",") { it.csvEscaped() }
        )
    }
}

private fun buildJsonExport(transactions: List<TransactionEntity>): String {
    val array = JSONArray()
    transactions.forEach { transaction ->
        array.put(
            JSONObject()
                .put("id", transaction.id)
                .put("date", transaction.date.toString())
                .put("type", transaction.type.name)
                .put("category", transaction.category)
                .put("amount", transaction.amount)
                .put("note", transaction.note)
        )
    }
    return array.toString(2)
}

private fun buildExcelXmlExport(transactions: List<TransactionEntity>): String = buildString {
    appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    appendLine("""<?mso-application progid="Excel.Sheet"?>""")
    appendLine("""<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">""")
    appendLine("""<Worksheet ss:Name="Ledger">""")
    appendLine("<Table>")
    appendExcelRow(listOf("ID", "日期", "类型", "类别", "金额", "说明"))
    transactions.forEach { transaction ->
        appendExcelRow(
            listOf(
                transaction.id.toString(),
                transaction.date.toString(),
                transaction.type.name,
                transaction.category,
                String.format(Locale.US, "%.2f", transaction.amount),
                transaction.note
            )
        )
    }
    appendLine("</Table>")
    appendLine("</Worksheet>")
    appendLine("</Workbook>")
}

private fun StringBuilder.appendExcelRow(values: List<String>) {
    appendLine("<Row>")
    values.forEach { value ->
        appendLine("""<Cell><Data ss:Type="String">${value.xmlEscaped()}</Data></Cell>""")
    }
    appendLine("</Row>")
}

private fun String.csvEscaped(): String = "\"${replace("\"", "\"\"")}\""

private fun String.xmlEscaped(): String = buildString(length) {
    forEach { char ->
        append(
            when (char) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&apos;"
                else -> char
            }
        )
    }
}
