package org.minbox.framework.exmaple.message.pipe.server;

import com.google.common.hash.Hashing;
import org.minbox.framework.message.pipe.core.transport.RequestIdGenerator;

import java.util.UUID;

/**
 * 自定义请求ID生成方式
 *
 * @author 恒宇少年
 */
public class CustomRequestIdGenerator implements RequestIdGenerator {
    @Override
    public String generate() {
        long hash = this.nextShortId();
        return Long.toUnsignedString(hash, 32);
    }

    private long nextShortId() {
        UUID uuid = UUID.randomUUID();
        long h = uuid.getMostSignificantBits();
        long l = uuid.getLeastSignificantBits();

        byte[] bytes = new byte[16];
        bytes[0] = (byte) ((h >>> 56) & 0xFF);
        bytes[1] = (byte) ((h >>> 48) & 0xFF);
        bytes[2] = (byte) ((h >>> 40) & 0xFF);
        bytes[3] = (byte) ((h >>> 32) & 0xFF);
        bytes[4] = (byte) ((h >>> 24) & 0xFF);
        bytes[5] = (byte) ((h >>> 16) & 0xFF);
        bytes[6] = (byte) ((h >>> 8) & 0xFF);
        bytes[7] = (byte) (h & 0xFF);

        bytes[8] = (byte) ((l >>> 56) & 0xFF);
        bytes[9] = (byte) ((l >>> 48) & 0xFF);
        bytes[10] = (byte) ((l >>> 40) & 0xFF);
        bytes[11] = (byte) ((l >>> 32) & 0xFF);
        bytes[12] = (byte) ((l >>> 24) & 0xFF);
        bytes[13] = (byte) ((l >>> 16) & 0xFF);
        bytes[14] = (byte) ((l >>> 8) & 0xFF);
        bytes[15] = (byte) (l & 0xFF);

        return Hashing.murmur3_128().hashBytes(bytes).asLong();
    }
}
