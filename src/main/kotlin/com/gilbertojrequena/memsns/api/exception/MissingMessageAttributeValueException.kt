package com.gilbertojrequena.memsns.api.exception

open class MissingMessageAttributeValueException(name: String, dataType: String) :
    MessageAttributeValidationException("The message attribute '$name' must contain non-empty message attribute value for message attribute type '$dataType'")
