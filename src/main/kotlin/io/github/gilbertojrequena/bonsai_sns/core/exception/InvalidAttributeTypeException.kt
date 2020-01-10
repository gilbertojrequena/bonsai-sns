package io.github.gilbertojrequena.bonsai_sns.core.exception

internal class InvalidAttributeTypeException(type: String) :
    SnsException("Invalid attribute with type: $type found during policy evaluation")