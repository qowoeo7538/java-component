package org.shaw.core.extension;

import org.shaw.core.Constants;
import org.shaw.util.CollectionUtils;
import org.shaw.util.NetUtils;
import org.shaw.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    private volatile transient String ip;

    private final int port;

    private final String path;

    /**  */
    private final Map<String, String> parameters;

    private volatile transient String string;

    protected ExtURL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public ExtURL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public ExtURL(String protocol, String host, int port, String[] pairs) { // varargs ... confilict with the following path argument, use array instead.
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public ExtURL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public ExtURL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public ExtURL(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public ExtURL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public ExtURL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public ExtURL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
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

    public static ExtURL valueOf(String url) {
        if (StringUtils.isEmpty(url.trim())) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        // 参数
        int i = url.indexOf("?");
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<>(16);
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        // 协议
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        // 路径
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        // 用户名和密码
        i = url.lastIndexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        // 端口
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        // 地址
        if (url.length() > 0) {
            host = url;
        }
        return new ExtURL(protocol, username, password, host, port, path, parameters);
    }

    public String getServiceInterface() {
        return getParameter(Constants.INTERFACE_KEY, path);
    }

    /**
     * 添加成对的参数
     */
    public ExtURL addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<>(16);
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }

    /**
     * 添加参数
     */
    public ExtURL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            // 通过key获取值
            String value = getParameters().get(entry.getKey());
            // 判断参数是否跟现有的参数匹配
            if (value == null) {
                if (entry.getValue() != null) {
                    hasAndEqual = false;
                    break;
                }
            } else {
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }

        // 如果参数都匹配, 则返回当前对象
        if (hasAndEqual) {
            return this;
        }
        // 等价一次拷贝
        Map<String, String> map = new HashMap<>(getParameters());
        map.putAll(parameters);
        return new ExtURL(protocol, username, password, host, port, path, map);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(final String key) {
        String value = parameters.get(key);
        if (StringUtils.isEmpty(value)) {
            value = parameters.get(DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(final String key, final String defaultValue) {
        final String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public ExtURL addParameter(final String key, final String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return this;
        }

        // 如果值没有改变,则立即返回
        if (value.equals(getParameters().get(key))) {
            return this;
        }

        // 如果值改变, 则重新生成对象返回
        Map<String, String> map = new HashMap<>(getParameters());
        map.put(key, value);
        return new ExtURL(protocol, username, password, host, port, path, map);
    }

    public ExtURL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    /**
     * 删除参数
     *
     * @param keys key
     * @return 删除对应参数的新对象
     */
    public ExtURL removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new ExtURL(protocol, username, password, host, port, path, map);
    }

    /**
     * 判断参数是否有值
     *
     * @param key key
     * @return if {@code true} 值存在
     */
    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    public String getServiceKey() {
        String inf = getServiceInterface();
        if (inf == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String group = getParameter(Constants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = getParameter(Constants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    /**
     * 构建 URL 参数
     *
     * @param buf        字符缓冲
     * @param concat     是否通过 "?" 连接参数
     * @param parameters 参数 key
     */
    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (!CollectionUtils.isEmpty(getParameters())) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Iterator<Map.Entry<String, String>> iterator = new TreeMap<>(getParameters()).entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                if (!StringUtils.isEmpty(entry.getKey())
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    /**
     * @see #buildString(boolean, boolean, boolean, boolean, String...)
     */
    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    /**
     * 构建 String 字符串
     *
     * @param appendUser      是否添加用户名和密码
     * @param appendParameter
     * @param useIP           是否使用 IP 地址
     * @param useService      是否服务调用
     * @param parameters
     * @return
     */
    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder buf = new StringBuilder();
        // 协议
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        // 用户名和密码
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        // host 地址
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        // 端口
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        // 路径
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public ExtURL setProtocol(String protocol) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    public String getUsername() {
        return username;
    }

    public ExtURL setUsername(String username) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    public String getPassword() {
        return password;
    }

    public ExtURL setPassword(String password) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    public String getHost() {
        return host;
    }

    public ExtURL setHost(String host) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public int getPort() {
        return port;
    }

    public ExtURL setPort(int port) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    public String getPath() {
        return path;
    }

    public ExtURL setPath(String path) {
        return new ExtURL(protocol, username, password, host, port, path, getParameters());
    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        return string = buildString(false, true);
    }

}
