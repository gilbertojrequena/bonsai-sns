# Bonsai SNS

Is a simple async implementation of AWS SNS intended for executing tests or simulate environments locally, it was inspired by [ElasticMQ](https://github.com/softwaremill/elasticmq)

## Features

Bonsai SNS supports a subset of AWS SNS api operations:
* CreateTopic
* DeleteTopic
* GetSubscriptionAttributes
* ListSubscription
* ListSubscriptionsByTopic
* ListTopics
* Publish
* SetSubscriptionAttributes
* Subscribe
  * HTTP/S
  * SQS
  * With: [MessageAttributes](https://docs.aws.amazon.com/sns/latest/dg/sns-message-attributes.html), [Message filtering](https://docs.aws.amazon.com/sns/latest/dg/sns-subscription-filter-policies.html#subscription-filter-policy-constraints), RawMessageDelivery, and  [RedrivePolicy](https://docs.aws.amazon.com/sns/latest/dg/sns-dead-letter-queues.html)
* Unsubscribe

The rest of the operations return dummy responses when invoked.

## Usage
### Embedded server

For starting an embedded server the `BonsaiSnsServerBuilder` can be used
```
BonsaiSnsServer server = new BonsaiSnsServer.Builder()
    .withAccountId("123")
    .withPort(9999)
    .withRegion("someRegion")
    .withSqsEndpoint("http://localhost:9324")
    .withSqsAccessKey("foo")
    .withSqsSecretKey("bar")
    .start();
// Do something
server.stop();
```

### Stand-alone server

Download the [jar](https://repo1.maven.org/maven2/io/github/gilbertojrequena/bonsai-sns/0.1.8/bonsai-sns-0.1.8.jar) and execute it 

`java -jar bonsai-sns-0.1.8.jar port=9494 region=region accountId=987654321 sqsEndpoint=http://localhost:9432 sqsAccessKey=foo sqsSecretKey=bar`

or 

`java -Dconfig.file=bonsai-sns.conf -jar bonsai-sns-0.1.8.jar`

### Environment configuration
bonsai-sns uses [Typesafe Config](https://github.com/lightbend/config) for loading its configuration, topics and subscriptions can be created on startup by providing an `application.conf` 
or `reference.conf` file in the classpath or by providing the JVM option `config.file` with the location of the 
configuration file 

```
bonsai-sns {
  port = 7979
  region = "region"
  accountId = "123456789"
  sqsEndpoint = "http://localhost:9324"
  sqsAccessKey = "foo"
  sqsSecretKey = "bar"
  topics {
    test-topic-0 {
      subscriptions = [
        {
          endpoint = "http://localhost:8080/action"
          protocol = "http"
          attributes {
            RawMessageDelivery = "true"
          }
        },
        {
          endpoint = "https://localhost:8081/target"
          protocol = "https"
        }
      ]
    }
    test-topic-1 {
      subscriptions = [
        {
          endpoint = "arn:aws:sqs:blah:blah:blah"
          protocol = "sqs"
          attributes {
            RawMessageDelivery = "true"
          }
        },
        {
          endpoint = "https://localhost:8081/target"
          protocol = "https"
        }
      ]
    }
  }
}
```

For SQS subscriptions, the SQS configuration needs to be provided using `sqsEndpoint`, `sqsAccessKey` and `sqsAccessKey`.

Environment configuration can also be done using the `BonsaiSnsServerBuilder` and 
`BonsaiSnsEnvironmentDefinition` builder

```
BonsaiSnsServer server = new BonsaiSnsServer.Builder()
    .withAccountId("123")
    .withPort(9999)
    .withRegion("someRegion")
    .withSqsEndpoint("http://localhost:9324")
    .withSqsAccessKey("foo")
    .withSqsSecretKey("bar")
    .withBonsaiSnsEnvironmentDefinition(
        BonsaiSnsEnvironment.Companion.definition()
            .withTopic(
                Topic.Companion.definition()
                    .withName("name")
                    .withSubscription(
                        Subscription.Companion.definition()
                            .withEndpoint("http:/localhost:8080/endpoint")
                            .withProtocol("http")
                            .withAttribute("a", "b")
                    )
            )
            .withTopic(
                Topic.Companion.definition()
                    .withName("name")
                    .withSubscription(
                        Subscription.Companion.definition()
                            .withEndpoint("arn:aws:sqs:blah:blah:blah")
                            .withProtocol("sqs")
                    )
                    .withSubscription(
                        Subscription.Companion.definition()
                            .withEndpoint("http:/localhost:8080/endpoint")
                            .withProtocol("http")
                    )
            )
        ).start();

// Do something
server.stop();
```

### How the configuration is resolved

### Using `BonsaiSnsServerBuilder`

When no configuration is provided using the `BonsaiSnsServerBuilder` the `application.conf` or `reference.conf`
file is used as fallback if there is no `.conf` file in the classpath, the defaults are used

### Running stand-alone server

When the jar is executed without params the `application.conf` or `reference.conf`
file is used as fallback, if there is no `.conf` file in the classpath, the defaults are used 

### Default configuration values
The default configuration values are `accountId=123456789`,`port=9797`, 
`region=region`, `sqsEndpoint=null`, `sqsAccessKey=""`, `sqsSecretKey=""`

### Using aws java SDK to interact with bonsai-sns

```
BonsaiSnsServer server = new BonsaiSnsServer.Builder()
    .withBonsaiSnsEnvironmentDefinition(
        BonsaiSnsEnvironment.Companion.definition()
            .withTopic(
                Topic.Companion.definition()
                    .withName("package-shipped")
                    .withSubscription(
                        Subscription.Companion.definition()
                            .withEndpoint("http://localhost:8080/target")
                            .withProtocol("http")
                )
            )
    ).start();

AmazonSNS snsClient = AmazonSNSAsyncClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey")))
    .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration("http://localhost:7979", "region")
    )
.build();

snsClient.listTopics().getTopics().forEach(topic ->
        snsClient.publish(new PublishRequest(topic.getTopicArn(), "Package 1 was shipped")));

server.stop();
```


## Bonsai-sns docker image

Docker image [gilbertojrequena/bonsai-sns](https://hub.docker.com/r/gilbertojrequena/bonsai-sns) is available

```
docker run -p 7979 gilbertojrequena/bonsai-sns
```

Custom configuration and `logback.xml` can be provided

```
docker run -p 7979 -v custom.conf:/opt/bonsai-sns/bonsai-sns.conf -v logback.xml:/opt/bonsai-sns/logback.xml
```

## Bonsai-sns in maven 

```
<dependency>
  <groupId>io.github.gilbertojrequena</groupId>
  <artifactId>bonsai-sns</artifactId>
  <version>0.1.8</version>
</dependency>
```

## Running tests

Go to the project directory and execute `gradlew test`

## Building the app

Go to the project directory and execute `gradlew build`

## Contributing
Pull requests and issues are welcome 

## Acknowledgments
bonsai-sns can be used for anything but for replacing Amazon SNS in production environments

