package com.gilbertojrequena.memsns.core.exception

internal class InvalidFilterPolicyException(message: String) : SnsException("FilterPolicy: $message")