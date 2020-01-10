package io.github.gilbertojrequena.bonsai_sns.core.exception

internal class SubscriptionNotFoundException(arn: String) : NotFoundException("Subscription with arn: $arn not found")
