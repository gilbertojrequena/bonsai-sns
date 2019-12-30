package com.gilbertojrequena.bonsai_sns.core.exception

internal class InvalidPolicyNumericOperationException(operation: String) :
    SnsException("Invalid operation: $operation found during policy evaluation")