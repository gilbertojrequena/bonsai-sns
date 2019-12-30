package com.gilbertojrequena.bonsai_sns.core.exception

internal class InvalidFilterPolicyException(message: String) : SnsException("FilterPolicy: $message")