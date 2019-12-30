package com.gilbertojrequena.bonsai_sns.api.exception

internal open class MissingMessageAttributeValueException(name: String, dataType: String) :
    MessageAttributeValidationException("The message attribute '$name' must contain non-empty message attribute value for message attribute type '$dataType'")
