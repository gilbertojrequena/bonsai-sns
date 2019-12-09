package com.gilbertojrequena.memsns.core.exception

class SubscriptionNotFoundException(arn: String) : NotFoundException("Subscription with arn: $arn not found")
