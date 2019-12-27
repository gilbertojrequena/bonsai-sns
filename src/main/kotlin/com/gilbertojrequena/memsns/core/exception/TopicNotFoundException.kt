package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.TopicArn

internal class TopicNotFoundException(topicArn: TopicArn) : NotFoundException("Topic $topicArn not found")
