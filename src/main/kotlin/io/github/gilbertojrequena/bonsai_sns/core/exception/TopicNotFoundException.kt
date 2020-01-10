package io.github.gilbertojrequena.bonsai_sns.core.exception

import com.gilbertojrequena.bonsai_sns.core.TopicArn

internal class TopicNotFoundException(topicArn: TopicArn) : NotFoundException("Topic $topicArn not found")
