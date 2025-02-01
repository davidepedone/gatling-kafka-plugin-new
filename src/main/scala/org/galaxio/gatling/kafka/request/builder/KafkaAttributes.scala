package org.galaxio.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import org.apache.kafka.common.header.Headers

case class KafkaAttributes[K, V](
    requestName: Expression[String],
    key: Option[Expression[K]],
    payload: Expression[V],
    headers: Either[Expression[String], Headers],
)
