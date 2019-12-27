package com.gilbertojrequena.memsns.core.exception

class InvalidPolicyNumericOperationException(operation: String) :
    SnsException("Invalid operation: $operation found during policy evaluation")