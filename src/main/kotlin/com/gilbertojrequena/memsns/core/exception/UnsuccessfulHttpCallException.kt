package com.gilbertojrequena.memsns.core.exception

import com.gilbertojrequena.memsns.core.Subscription

internal class UnsuccessfulHttpCallException(url: String, body: String) :
    SnsException("Unable to execute successfully HTTPS/S post, endpoint: '$url', message: '$body'")