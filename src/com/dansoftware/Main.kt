package com.dansoftware

import javafx.application.Platform
import javafx.stage.FileChooser
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter

fun main() {
    print("Separator:")
    val sep: String = readLine()?.takeIf { it.isNotBlank() } ?: "\t"
    print("Table name:")
    val tableName = readLine() ?: throw IllegalArgumentException("Table name must be specified")
    println("Select txt file")

    Platform.startup {
        val txtFile = FileChooser().showOpenDialog(null)
        val table = fromTxt(txtFile, tableName, sep)

        val destSqlFile = FileChooser().showSaveDialog(null)
        toSqlInsert(destSqlFile, table)
        Platform.runLater { Platform.exit() }
    }
}

fun fromTxt(file: File, tableName: String, sep: String): Table {
    val lines = BufferedReader(FileReader(file))
        .readLines()

    val columns = lines[0].split(sep)
    val rows = lines.asSequence()
        .drop(1)
        .map {
            Row(it.split(sep).map(::formatTableValue))
        }
        .toList()
    return Table(tableName, columns, rows)
}

fun formatTableValue(value: String): String {
    return value.toIntOrNull()?.toString() ?: "'$value'"
}

fun toSqlInsert(file: File, table: Table) {
    PrintWriter(FileWriter(file)).use {
        it.println("INSERT INTO ${table.name} (${table.columns.joinToString(separator = ", ")}) VALUES")
        it.println(
            table.items
                .joinToString(separator = ",\n") { row ->
                    "(${row.items.joinToString(separator = ", ")})"
                } + ";"
        )
    }
}