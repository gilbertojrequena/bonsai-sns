package com.gilbertojrequena.bonsai_sns.core.exception

internal class InvalidQueueArnException(arn: String) : SnsException("Queue arn: $arn is invalid")
