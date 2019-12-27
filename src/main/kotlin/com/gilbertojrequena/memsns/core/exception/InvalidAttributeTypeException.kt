package com.gilbertojrequena.memsns.core.exception

internal class InvalidAttributeTypeException(type: String) :
    SnsException("Invalid attribute with type: $type found during policy evaluation")