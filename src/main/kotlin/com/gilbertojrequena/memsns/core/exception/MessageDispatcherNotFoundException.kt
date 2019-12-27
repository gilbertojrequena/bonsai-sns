package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.Subscription

internal class MessageDispatcherNotFoundException(protocol: Subscription.Protocol) :
    RuntimeException("Message dispatcher for protocol ${protocol.value} not found")