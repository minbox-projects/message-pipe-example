package org.minbox.framework.exmaple.message.pipe.server;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author 恒宇少年
 */
//@Component
public class ListMessageListener extends KeyspaceEventMessageListener {
    public ListMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer container) {
        container.addMessageListener(this, new PatternTopic("__keyevent@*__:rpush"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("消息：" + message.toString() + ",pattern:" + new String(pattern));
    }

    @Override
    protected void doHandleMessage(Message message) {
        //...
    }
}
