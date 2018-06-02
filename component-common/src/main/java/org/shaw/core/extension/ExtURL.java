package org.shaw.core.extension;

import org.shaw.util.CollectionUtils;
import org.shaw.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.shaw.core.Constants.DEFAULT_KEY_PREFIX;

/**
 * 该对象同 String 类型设计一样, 都是不可变对象. 当操作发生属性改变时,会返回新的对象.
 */
public final class ExtURL implements Serializable {

    private static final long serialVersionUID = 4352343305068952942L;

    private final String protocol;

    private final String username;

    private final String password;

    private final String host;

    private final int port;

    private final String path;

    /**  */
    private final Map<String, String> parameters;

    protected ExtURL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public ExtURL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public ExtURL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public ExtURL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if (StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        // trim the beginning "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        if (StringUtils.isEmpty(value)) {
            value = parameters.get(DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public ExtURL addParameter(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return this;
        }

        // 如果值重复, 则立即返回
        if (value.equals(getParameters().get(key))) {
            return this;
        }

        // 如果值改变, 则重新生成对象返回
        Map<String, String> map = new HashMap<>(getParameters());
        map.put(key, value);
        return new ExtURL(protocol, username, password, host, port, path, map);
    }
}
