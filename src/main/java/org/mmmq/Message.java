package org.mmmq;

class Message {

    String topic;
    String payload;

    Message(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }
}
