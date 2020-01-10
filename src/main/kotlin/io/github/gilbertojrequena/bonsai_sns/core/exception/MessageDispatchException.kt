package io.github.gilbertojrequena.bonsai_sns.core.exception

internal class MessageDispatchException(endpoint: String, msg: String) :
    SnsException("Unable to deliver message, endpoint: '$endpoint', message: '$msg'")