package io.ticofab.legacyprocessor1

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

object LegacyProcessor1App extends App {

  println("Legacy Processor 1 starting.")
  implicit val actorSystem = ActorSystem("legacyProcessor1")
  val config = actorSystem.settings.config.getConfig("our-kafka-consumer")

  // settings to consume from a kafka topic
  val consumerSettings  = ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
  val kafkaSubscription = Subscriptions.assignmentWithOffset(new TopicPartition("FirstTopic", 0), 0)

  // the rsocket sink that will propagate items downstream
  val rSocketSink = Sink.fromGraph(new RSocketSink(7000))

  // connects to a running kafka topics and consumes from there
  Consumer
    .plainSource(consumerSettings, kafkaSubscription)
    .map(msg => {
      println(s"read message from kafka: ${msg.value} at offset ${msg.offset}")
      msg.value.getBytes
    })
    .to(rSocketSink)
    .run()
}