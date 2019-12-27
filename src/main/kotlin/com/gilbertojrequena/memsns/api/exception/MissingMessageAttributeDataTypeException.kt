package com.gilbertojrequena.memsns.api.exception

open class MissingMessageAttributeDataTypeException(attributeName: String) :
    MessageAttributeValidationException("The message attribute '$attributeName' must contain non-empty message attribute type")
