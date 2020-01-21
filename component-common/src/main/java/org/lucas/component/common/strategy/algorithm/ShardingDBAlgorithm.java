package org.lucas.component.common.strategy.algorithm;

import org.lucas.component.common.hash.ShardingHashCode;
import org.springframework.util.StringUtils;

public class ShardingDBAlgorithm {

    private static ShardingHashCode dataBaseHashCoding;

    public static ShardingHashCode getDataBaseHashCoding() {
        return ShardingDBAlgorithm.dataBaseHashCoding;
    }

    public static void setDataBaseHashCoding(ShardingHashCode dataBaseHashCoding) {
        ShardingDBAlgorithm.dataBaseHashCoding = dataBaseHashCoding;
    }

    /**
     * 通过负载一致性算法 获取真实节点数
     *
     * @param identity
     * @return
     */
    public static Integer getRealNode(Object identity) {
        String s = String.valueOf(identity);
        return dataBaseHashCoding.hashFor(s);
    }

    /**
     * 生成2位 32进制定位符
     *
     * @param identity 生成定位符的标识 例如：memberId 或其他
     * @return
     */
    public static String getLocatorStr(String identity) {
        String locatorStr = Integer.toString(ShardingDBAlgorithm.getRealNode(identity), 32).toUpperCase();
        //不足两位 补0
        if (locatorStr.length() == 1) {
            locatorStr = "0" + locatorStr;
        }
        return locatorStr;
    }

    /**
     * 通过定位符字符串 转为真实节点数
     *
     * @param locatorStr
     * @return
     */
    public static Integer transferRealNodeByLocatorStr(String locatorStr) {
        if (StringUtils.isEmpty(locatorStr)) {
            throw new RuntimeException("通过 locatorStr 转为真实节点时不能为空!");
        }
        //要考虑 不足两位时，补位的左边 补位的0
        String localIndex = locatorStr.substring(locatorStr.length() - 2);
        String left = localIndex.substring(0, 1);
        String right = localIndex.substring(1);

        if ("0".equals(left)) {
            localIndex = right;
        }
        return Integer.parseInt(localIndex, 32);
    }
}
