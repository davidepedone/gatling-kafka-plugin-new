package org.galaxio.gatling.kafka.request.builder

import io.gatling.core.session._
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serde
import org.galaxio.gatling.kafka.actions.KafkaRequestReplyActionBuilder

import scala.reflect.ClassTag

case class KafkaRequestBuilderBase(requestName: Expression[String]) {

  def send[K, V](
      key: Expression[K],
      payload: Expression[V],
      headers: Expression[String],
  )(implicit
      sender: Sender[K, V],
  ): RequestBuilder[K, V] = {
    if (key == null)
      sender.send(requestName, None, payload, headers)
    else
      sender.send(requestName, Some(key), payload, headers)
  }

  def send[K, V](
      key: Expression[K],
      payload: Expression[V],
      headers: Headers,
  )(implicit
      sender: Sender[K, V],
  ): RequestBuilder[K, V] = {
    if (key == null)
      sender.send(requestName, None, payload, headers)
    else
      sender.send(requestName, Some(key), payload, headers)
  }

  def send[V](payload: Expression[V])(implicit sender: Sender[Nothing, V]): RequestBuilder[_, V] =
    sender.send(requestName, None, payload)

  def requestReply: ReqRepBase.type = ReqRepBase

  object ReqRepBase {
    case class RROutTopicStep(inputTopic: Expression[String], outputTopic: Expression[String]) {
      def send[K: Serde: ClassTag, V: Serde: ClassTag](
          key: Expression[K],
          payload: Expression[V],
          headers: Expression[String],
      ): KafkaRequestReplyActionBuilder[K, V] = {
        KafkaRequestReplyActionBuilder[K, V](
          new KafkaRequestReplyAttributes[K, V](
            requestName,
            inputTopic,
            outputTopic,
            key,
            payload,
            Left(headers),
            implicitly[Serde[K]].serializer(),
            implicitly[Serde[V]].serializer(),
            List.empty,
          ),
        )
      }

      def send[K: Serde: ClassTag, V: Serde: ClassTag](
          key: Expression[K],
          payload: Expression[V],
          headers: Headers,
      ): KafkaRequestReplyActionBuilder[K, V] = {
        KafkaRequestReplyActionBuilder[K, V](
          new KafkaRequestReplyAttributes[K, V](
            requestName,
            inputTopic,
            outputTopic,
            key,
            payload,
            Right(headers),
            implicitly[Serde[K]].serializer(),
            implicitly[Serde[V]].serializer(),
            List.empty,
          ),
        )
      }
    }

    case class RRInTopicStep(inputTopic: Expression[String]) {
      def replyTopic(outputTopic: Expression[String]): RROutTopicStep = RROutTopicStep(inputTopic, outputTopic)
    }
    def requestTopic(rt: Expression[String]): RRInTopicStep = RRInTopicStep(rt)

  }

}
