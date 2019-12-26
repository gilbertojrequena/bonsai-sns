package com.gilbertojrequena.memsns.core.exception

class EndpointProtocolMismatchException(endpoint: String, protocol: String) :
    SnsException("Endpoint: $endpoint does't match the specified protocol: $protocol ")
