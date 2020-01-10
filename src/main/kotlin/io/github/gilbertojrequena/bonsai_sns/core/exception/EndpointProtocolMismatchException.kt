package io.github.gilbertojrequena.bonsai_sns.core.exception

internal class EndpointProtocolMismatchException(endpoint: String, protocol: String) :
    SnsException("Endpoint: $endpoint does't match the specified protocol: $protocol ")
