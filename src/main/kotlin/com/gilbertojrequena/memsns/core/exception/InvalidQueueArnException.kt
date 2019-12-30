package com.gilbertojrequena.memsns.core.exception

internal class InvalidQueueArnException(arn: String) : SnsException("Queue arn: $arn is invalid")
