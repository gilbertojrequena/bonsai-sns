package com.gilbertojrequena.bonsai_sns.api.exception

internal open class MissingMessageAttributeDataTypeException(attributeName: String) :
    MessageAttributeValidationException("The message attribute '$attributeName' must contain non-empty message attribute type")
