package com.gilbertojrequena.bonsai_sns.core.exception

internal class InvalidRedrivePolicyException(message: String) : SnsException("RedrivePolicy: $message")