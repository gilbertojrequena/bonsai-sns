package com.gilbertojrequena.memsns.core.exception

internal class InvalidPolicyNumericOperationException(operation: String) :
    SnsException("Invalid operation: $operation found during policy evaluation")