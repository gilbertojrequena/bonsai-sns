package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.Subscription

internal class SubscriptionAlreadyExist(
    subscription: Subscription
) : SnsException("Subscription for ${subscription.topicArn}, with protocol: ${subscription.protocol} and endpoint: ${subscription.endpoint} already exist")
