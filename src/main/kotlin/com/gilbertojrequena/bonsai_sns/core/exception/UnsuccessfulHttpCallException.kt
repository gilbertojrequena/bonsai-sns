package com.gilbertojrequena.bonsai_sns.core.exception

import com.gilbertojrequena.bonsai_sns.core.Subscription

internal class UnsuccessfulHttpCallException(url: String, body: String) :
    SnsException("Unable to execute successfully HTTPS/S post, endpoint: '$url', message: '$body'")