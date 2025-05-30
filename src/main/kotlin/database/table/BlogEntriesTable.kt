package database.table

object BlogEntriesTable : BaseTable() {
    val summary = text("summary")
    val content = text("content")
}
