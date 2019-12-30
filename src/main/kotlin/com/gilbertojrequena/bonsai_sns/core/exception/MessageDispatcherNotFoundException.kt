package com.gilbertojrequena.bonsai_sns.core.exception

import com.gilbertojrequena.bonsai_sns.core.Subscription

internal class MessageDispatcherNotFoundException(protocol: Subscription.Protocol) :
    SnsException("Message dispatcher for protocol ${protocol.value} not found")