package com.gilbertojrequena.memsns.api.exception

internal class ActionNotFoundException(action: String) : RuntimeException("Action $action not found")