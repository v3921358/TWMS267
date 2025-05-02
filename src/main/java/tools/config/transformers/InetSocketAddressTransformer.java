package tools.config.transformers;

import tools.config.TransformationException;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class InetSocketAddressTransformer
        implements PropertyTransformer<InetSocketAddress> {
    /* 23 */ public static final InetSocketAddressTransformer SHARED_INSTANCE = new InetSocketAddressTransformer();

    public InetSocketAddress transform(String value, Field field) throws TransformationException {
        /* 35 */
        String[] parts = value.split(":");

        /* 37 */
        if (parts.length != 2) {
            /* 38 */
            throw new TransformationException("Can't transform property, must be in format \"address:port\"");
        }

        try {
            /* 42 */
            if ("*".equals(parts[0])) {
                /* 43 */
                return new InetSocketAddress(Integer.parseInt(parts[1]));
            }
            /* 45 */
            InetAddress address = InetAddress.getByName(parts[0]);
            /* 46 */
            int port = Integer.parseInt(parts[1]);
            /* 47 */
            return new InetSocketAddress(address, port);
            /* 48 */
        } catch (Exception e) {
            /* 49 */
            throw new TransformationException(e);
        }
    }
}
