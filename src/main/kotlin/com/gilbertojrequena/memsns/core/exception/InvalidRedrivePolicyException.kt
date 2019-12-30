package com.gilbertojrequena.memsns.core.exception

internal class InvalidRedrivePolicyException(message: String) : SnsException("RedrivePolicy: $message")