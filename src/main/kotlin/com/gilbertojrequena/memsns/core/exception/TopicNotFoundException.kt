package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.TopicArn

class TopicNotFoundException(topicArn: TopicArn) : SnsException("Topic $topicArn not found")
