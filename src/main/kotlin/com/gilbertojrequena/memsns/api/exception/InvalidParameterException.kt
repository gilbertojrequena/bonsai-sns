package com.gilbertojrequena.memsns.api.exception

internal open class InvalidParameterException(val parameter: String, val reason: String) :
    RuntimeException("Invalid parameter $parameter: $reason")
