package com.gilbertojrequena.memsns.core.exception

internal class SubscriptionNotFoundException(arn: String) : NotFoundException("Subscription with arn: $arn not found")
