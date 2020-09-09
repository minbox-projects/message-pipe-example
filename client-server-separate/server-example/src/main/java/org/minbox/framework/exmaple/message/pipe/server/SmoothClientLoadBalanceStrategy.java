package org.minbox.framework.exmaple.message.pipe.server;

import org.minbox.framework.message.pipe.core.exception.MessagePipeException;
import org.minbox.framework.message.pipe.core.information.ClientInformation;
import org.minbox.framework.message.pipe.server.lb.ClientLoadBalanceStrategy;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 平滑负载均衡策略
 *
 * @author 恒宇少年
 */
public class SmoothClientLoadBalanceStrategy implements ClientLoadBalanceStrategy {
    @Override
    public ClientInformation lookup(List<ClientInformation> clients) throws MessagePipeException {
        if (!ObjectUtils.isEmpty(clients)) {
            // 实现平滑过渡负载均衡策略逻辑
        }
        // 不存在符合的客户端时，可以返回null
        return null;
    }
}
