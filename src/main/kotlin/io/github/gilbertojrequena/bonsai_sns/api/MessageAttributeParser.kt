package io.github.gilbertojrequena.bonsai_sns.api

import com.gilbertojrequena.bonsai_sns.api.exception.MessageAttributeValidationException
import com.gilbertojrequena.bonsai_sns.api.exception.MissingMessageAttributeDataTypeException
import com.gilbertojrequena.bonsai_sns.api.exception.MissingMessageAttributeValueException
import com.gilbertojrequena.bonsai_sns.core.MessageAttribute
import io.ktor.http.Parameters

internal class MessageAttributeParser {

    companion object {
        fun parse(parameters: Parameters): Map<String, MessageAttribute> {
            val validationExceptions = mutableListOf<MessageAttributeValidationException>()
            val attributes = mutableMapOf<String, MessageAttribute>()
            generateSequence(1) {
                it + 1
            }.takeWhile {
                parameters["MessageAttributes.entry.$it.Name"] != null
            }.forEach { position ->
                try {
                    val name = parameters["MessageAttributes.entry.$position.Name"]
                        ?: throw MessageAttributeValidationException("Invalid attribute name '${parameters["MessageAttributes.entry.$position.Name"]}'")
                    if (!name.toLowerCase().matches(Regex("^(?!\\.)(?!.*\\.\\.)(?!amazon\\.|aws\\.)[a-z0-9-_.]{1,256}(?<!\\.)$"))) {
                        throw MessageAttributeValidationException("Invalid attribute name '$name'.")
                    }
                    val dataType = parameters["MessageAttributes.entry.$position.Value.DataType"].let {
                        if (it.isNullOrEmpty()) throw MissingMessageAttributeDataTypeException(name) else it
                    }
                    if (dataType.length > 256) {
                        throw MessageAttributeValidationException("Length of message attribute type must be less than 256 bytes.")
                    }
                    if (!dataType.matches(Regex("^(String|Binary|Number){1}[a-zA-z0-9.-_]*$"))) {
                        throw MessageAttributeValidationException("The message attribute '$name' has an invalid message attribute type, the set of supported type prefixes is Binary, Number, and String.")
                    }
                    val value = parameters["MessageAttributes.entry.$position.Value.StringValue"].let {
                        if (it.isNullOrEmpty()) throw MissingMessageAttributeValueException(name, dataType) else it
                    }
                    if (dataType == "Number") {
                        try {
                            value.toBigDecimal()
                        } catch (e: NumberFormatException) {
                            throw MessageAttributeValidationException("Could not cast message attribute '$name' value to number.")
                        }
                    }
                    attributes[name] = MessageAttribute(dataType, value)
                } catch (e: MessageAttributeValidationException) {
                    validationExceptions.add(e)
                }
            }
            if (validationExceptions.isNotEmpty()) {
                if (validationExceptions.size > 1) {
                    throw MessageAttributeValidationException(
                        "${validationExceptions.size} validation error detected: ${validationExceptions.map { it.message }.joinToString(
                            "; "
                        )}"
                    )
                }
                throw validationExceptions.first()
            }
            return attributes
        }
    }
}