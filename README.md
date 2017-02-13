

Post Word to translate:
```
curl -H "Content-Type: text/plain" -X POST -d 'one' http://localhost:9000/api/word 
```

Get Word saga state:
```
curl http://localhost:9000/api/word/state/2250815c-cec6-43d8-ae75-b724b802e0da
```

Consume Word Persistent events (kafka_2.11-0.9.0.1):
```
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic word-events --from-beginning
```

Sending words to service via Kafka topic (kafka_2.11-0.9.0.1): 
 
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic incoming-words
```