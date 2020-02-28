# Spring messaging
## 目录结构
- converter
    - 转换器模块
- core
    - 核心模块
- handler
    - 处理器模块
- simp
- support
- tcp

## 核心类
### Message 
- 消息实体接口
```java
public interface Message<T> {public interface Message<T> {

    /**
     * Return the message payload.
     * 消息体
     */
    T getPayload();

    /**
     * Return message headers for the message (never {@code null} but may be empty).
     * 消息处理器
     */
    MessageHeaders getHeaders();

}
```
### MessageHandler 
- 消息处理接口
```java
@FunctionalInterface
public interface MessageHandler {

    /**
     * Handle the given message.
     * 处理消息
     * @param message the message to be handled
     * @throws MessagingException if the handler failed to process the message
     */
    void handleMessage(Message<?> message) throws MessagingException;

}

```