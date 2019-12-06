package com.gilbertojrequena.memsns.core.exception

class TopicAlreadyExist(name: String) : SnsException("Topic with name: $name already exist")
