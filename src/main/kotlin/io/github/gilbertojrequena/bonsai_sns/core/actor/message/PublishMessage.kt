package io.github.gilbertojrequena.bonsai_sns.core.actor.message

import com.gilbertojrequena.bonsai_sns.core.PublishRequest

internal sealed class PublishMessage(val publishRequest: PublishRequest) {
    class Publish(publishRequest: PublishRequest) :
        PublishMessage(publishRequest)
}