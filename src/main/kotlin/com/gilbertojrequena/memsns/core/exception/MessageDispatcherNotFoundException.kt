package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.Subscription

internal class MessageDispatcherNotFoundException(protocol: Subscription.Protocol) :
    SnsException("Message dispatcher for protocol ${protocol.value} not found")