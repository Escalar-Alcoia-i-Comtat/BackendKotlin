package server.endpoints.contact

import ServerDatabase
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Attachments
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import database.entity.Path
import database.entity.Sector
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import io.ktor.util.encodeBase64
import java.io.File
import server.endpoints.EndpointBase
import server.request.saveFile
import server.response.respondSuccess
import system.EnvironmentVariables

object ReportIssueEndpoint : EndpointBase("/report") {
    private val fromEmailAddress = EnvironmentVariables.Services.SendGrid.FromEmail.value!!
    private val toEmailAddress = EnvironmentVariables.Services.SendGrid.ToEmail.value!!

    override suspend fun RoutingContext.endpoint() {
        val sendGridApiKey = EnvironmentVariables.Services.SendGrid.ApiKey.value
        if (sendGridApiKey == null) {
            return respondSuccess(HttpStatusCode.ServiceUnavailable)
        }

        var name: String? = null
        var email: String? = null
        var message: String? = null
        var sectorId: Int? = null
        var pathId: Int? = null
        val attachments = mutableMapOf<String, File>()

        receiveMultipart(
            forEachFormItem = { item ->
                when (item.name) {
                    "name" -> name = item.value
                    "email" -> email = item.value
                    "message" -> message = item.value
                    "sectorId" -> sectorId = item.value.toIntOrNull()
                    "pathId" -> pathId = item.value.toIntOrNull()
                }
            },
            forEachFileItem = { partData ->
                val tempFile = File.createTempFile("report_issue_", null).apply {
                    deleteOnExit()
                }
                partData.saveFile(tempFile)
                attachments[partData.name ?: tempFile.name] = tempFile
            },
        )

        if (message.isNullOrEmpty()) {
            return respondSuccess(HttpStatusCode.NoContent)
        }

        val emailMessage = StringBuilder().apply {
            if (name != null) appendLine("Name: $name")
            if (email != null) appendLine("Email: $email")
            sectorId?.let { id ->
                val sector = ServerDatabase { Sector.findById(id) }
                val zone = ServerDatabase { sector?.zone }
                append("Sector: ${sector?.displayName ?: "N/A"} ($id)")
                append(" <- ")
                appendLine("${zone?.displayName ?: "N/A"} (#${zone?.id ?: "N/A"})")
            }
            pathId?.let { id ->
                val path = ServerDatabase { Path.findById(id) }
                val sector = ServerDatabase { Sector.findById(id) }
                val zone = ServerDatabase { sector?.zone }
                append("Path: ${path?.displayName ?: "N/A"} ($id)")
                append(" <- ")
                append("${sector?.displayName ?: "N/A"} (#${sector?.id ?: "N/A"})")
                append(" <- ")
                appendLine("${zone?.displayName ?: "N/A"} (#${zone?.id ?: "N/A"})")
            }
            appendLine("Message: $message")
        }

        val sg = SendGrid(sendGridApiKey)
        val request = Request().apply {
            method = Method.POST
            endpoint = "mail/send"

            val fromEmail = Email(fromEmailAddress)
            val toEmail = Email(toEmailAddress)
            body = Mail(
                fromEmail,
                "New report from ${name ?: "N/A"}",
                toEmail,
                Content("text/plain", emailMessage.toString()),
            ).apply {
                val replyTo = email?.let { Email(it) }
                name?.let { replyTo?.name = it }
                replyTo?.let { setReplyTo(it) }

                attachments.forEach { (name, file) ->
                    addAttachments(
                        Attachments().apply {
                            content = file.readBytes().encodeBase64()
                            type = "application/octet-stream"
                            filename = name
                            disposition = "attachment"
                        }
                    )
                }
            }.build()
        }
        val response = sg.api(request)

        if (response.statusCode in 200..299) {
            respondSuccess()
        } else {
            respondSuccess(HttpStatusCode.InternalServerError)
        }
    }
}
