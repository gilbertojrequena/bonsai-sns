package com.gilbertojrequena.memsns.core.exception

internal class MessageDispatchException(endpoint: String, msg: String) :
    SnsException("Unable to deliver message, endpoint: '$endpoint', message: '$msg'")