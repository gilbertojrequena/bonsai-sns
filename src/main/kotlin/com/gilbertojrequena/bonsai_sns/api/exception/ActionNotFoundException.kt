package com.gilbertojrequena.bonsai_sns.api.exception

internal class ActionNotFoundException(action: String) : RuntimeException("Action $action not found")