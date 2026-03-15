package com.oriole.wisepen.document.api.constant;

/**
 * Document 服务 Kafka Topic 常量
 */
public interface MqTopicConstants {

    /** 文档解析任务队列：由 FileUploadedConsumer 生产，DocumentParseConsumer 消费 */
    String TOPIC_DOCUMENT_PARSE = "wisepen-document-parse-topic";

    /** 文档处理就绪事件：由 DocumentParseConsumer 生产，DocumentReadyConsumer 消费 */
    String TOPIC_DOCUMENT_READY = "wisepen-document-ready-topic";
}
