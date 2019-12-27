package com.gilbertojrequena.memsns.api.exception

internal open class MissingMessageAttributeDataTypeException(attributeName: String) :
    MessageAttributeValidationException("The message attribute '$attributeName' must contain non-empty message attribute type")
