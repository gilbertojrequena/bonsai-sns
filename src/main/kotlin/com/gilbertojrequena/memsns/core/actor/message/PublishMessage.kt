package com.gilbertojrequena.memsns.core.actor.message

import com.gilbertojrequena.memsns.core.PublishRequest

internal sealed class PublishMessage(val publishRequest: PublishRequest) {
    class Publish(publishRequest: PublishRequest) :
        PublishMessage(publishRequest)
}