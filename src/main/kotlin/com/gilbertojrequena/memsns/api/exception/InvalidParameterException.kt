package com.gilbertojrequena.memsns.api.exception

open class InvalidParameterException(val parameter: String, val reason: String) :
    RuntimeException("Invalid parameter $parameter: $reason")
