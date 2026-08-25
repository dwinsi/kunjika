package com.keyfortress.app.service

import android.app.assist.AssistStructure
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.Presentations
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.keyfortress.app.R
import com.keyfortress.app.data.local.AppDatabase
import com.keyfortress.app.data.repository.PasswordRepository
import kotlinx.coroutines.runBlocking

class KeyFortressAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val contexts = request.fillContexts
        val structure = contexts[contexts.size - 1].structure

        val parsedStructure = parseStructure(structure)
        
        // If we can't find any fields to fill, just return
        if (parsedStructure.usernameId == null && parsedStructure.passwordId == null) {
            callback.onSuccess(null)
            return
        }

        // Search for matching credentials in the vault
        val searchKey = parsedStructure.domain ?: ""
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PasswordRepository(database.passwordDao(), database.blockDao())
        
        val matches = runBlocking {
            if (searchKey.isNotEmpty()) {
                repository.getPasswordsByDomain(searchKey)
            } else {
                emptyList()
            }
        }

        if (matches.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        val responseBuilder = FillResponse.Builder()

        // Create a dataset for each match
        matches.forEach { item ->
            val presentation = RemoteViews(packageName, R.layout.autofill_dataset_item).apply {
                setTextViewText(R.id.title, item.title)
                setTextViewText(R.id.username, item.username)
            }

            val datasetBuilder = Dataset.Builder()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val presentations = Presentations.Builder()
                    .setMenuPresentation(presentation)
                    .build()

                parsedStructure.usernameId?.let { id ->
                    val field = Field.Builder()
                        .setValue(AutofillValue.forText(item.username))
                        .setPresentations(presentations)
                        .build()
                    datasetBuilder.setField(id, field)
                }

                parsedStructure.passwordId?.let { id ->
                    val field = Field.Builder()
                        .setValue(AutofillValue.forText(item.plaintextPassword))
                        .setPresentations(presentations)
                        .build()
                    datasetBuilder.setField(id, field)
                }
            } else {
                @Suppress("DEPRECATION")
                parsedStructure.usernameId?.let { id ->
                    datasetBuilder.setValue(id, AutofillValue.forText(item.username), presentation)
                }

                @Suppress("DEPRECATION")
                parsedStructure.passwordId?.let { id ->
                    datasetBuilder.setValue(id, AutofillValue.forText(item.plaintextPassword), presentation)
                }
            }

            responseBuilder.addDataset(datasetBuilder.build())
        }

        callback.onSuccess(responseBuilder.build())
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // Optional: Implement logic to capture and save new passwords detected on other screens
        callback.onSuccess()
    }

    private fun parseStructure(structure: AssistStructure): ParsedStructure {
        val parsed = ParsedStructure()
        val nodes = structure.windowNodeCount
        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i).rootViewNode
            traverseNode(node, parsed)
        }
        return parsed
    }

    private fun traverseNode(node: AssistStructure.ViewNode?, parsed: ParsedStructure) {
        if (node == null) return

        // 1. Identify Domain/Package
        if (parsed.domain == null) {
            node.webDomain?.let { parsed.domain = it }
        }

        // 2. Identify Fields using Autofill Hints
        val hints = node.autofillHints
        if (hints != null) {
            for (hint in hints) {
                when {
                    hint.contains("username", ignoreCase = true) || 
                    hint.contains("email", ignoreCase = true) -> {
                        parsed.usernameId = node.autofillId
                    }
                    hint.contains("password", ignoreCase = true) -> {
                        parsed.passwordId = node.autofillId
                    }
                }
            }
        }

        // 3. Fallback Heuristics based on ID and Class
        if (parsed.usernameId == null && (node.className?.contains("EditText") == true)) {
            val idEntry = node.idEntry
            if (idEntry != null && (idEntry.contains("username") || idEntry.contains("email") || idEntry.contains("login"))) {
                parsed.usernameId = node.autofillId
            }
        }

        if (parsed.passwordId == null && (node.className?.contains("EditText") == true)) {
            val idEntry = node.idEntry
            if (idEntry != null && (idEntry.contains("password") || idEntry.contains("pwd"))) {
                parsed.passwordId = node.autofillId
            }
        }

        // Recursively traverse children
        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), parsed)
        }
    }

    class ParsedStructure {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null
        var domain: String? = null
    }
}
