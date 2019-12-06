package com.gilbertojrequena.memsns.core.exception

class SubscriptionNotFound(arn: String) : SnsException("Subscription with arn: $arn not found")
