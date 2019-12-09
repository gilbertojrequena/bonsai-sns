package com.gilbertojrequena.memsns.core.exception

class TopicAlreadyExistException(name: String) : SnsException("Topic with name: $name already exist")
