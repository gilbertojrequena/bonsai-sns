package io.github.gilbertojrequena.bonsai_sns.core.exception

internal class UnsuccessfulHttpCallException(url: String, body: String) :
    SnsException("Unable to execute successfully HTTPS/S post, endpoint: '$url', message: '$body'")