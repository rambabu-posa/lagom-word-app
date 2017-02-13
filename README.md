

Post Word to translate:
```
curl -H "Content-Type: text/plain" -X POST -d 'one' http://localhost:9000/api/word 
```

Get Word saga state:
```
curl http://localhost:9000/api/word/state/2250815c-cec6-43d8-ae75-b724b802e0da
```

Consume Word Persistent events:
```
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic word-events --from-beginning
```
