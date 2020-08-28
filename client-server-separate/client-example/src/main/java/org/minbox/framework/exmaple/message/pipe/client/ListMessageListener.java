package org.minbox.framework.exmaple.message.pipe.client;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @author 恒宇少年
 */
@Component
public class ListMessageListener extends KeyspaceEventMessageListener {
    public ListMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer container) {
        //container.addMessageListener(this, new PatternTopic("__keyevent@*__:rpush"));
        //container.addMessageListener(this, new PatternTopic("__keyevent@*:lrem"));
        container.addMessageListener(this, new PatternTopic("__keyevent@*:lpop"));
        //container.addMessageListener(this, new PatternTopic("__keyevent@*:ltrim"));
        //container.addMessageListener(this, new PatternTopic("__keyevent@*:lset"));
        //container.addMessageListener(this, new PatternTopic("__keyevent@*:linsert"));
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
