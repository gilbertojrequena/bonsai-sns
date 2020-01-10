package io.github.gilbertojrequena.bonsai_sns.core.exception

import com.gilbertojrequena.bonsai_sns.core.Subscription

internal class SubscriptionAlreadyExist(
    subscription: Subscription
) : SnsException("Subscription for ${subscription.topicArn}, with protocol: ${subscription.protocol} and endpoint: ${subscription.endpoint} already exist")
