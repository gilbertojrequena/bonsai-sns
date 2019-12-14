package com.gilbertojrequena.memsns.api.exception

class ActionNotFoundException(action: String) : RuntimeException("Action $action not found")