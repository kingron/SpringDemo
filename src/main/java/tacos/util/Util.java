package tacos.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 工具类
 * <p>
 * 本工具类，仅仅使用Java本身自带类库，不允许引入任何第三方类库以保证低耦合性 <br/><br/>
 * 版权所有，(C) Kingron<kingron@163.com>
 *
 * @author Kingron
 * @version 1.0
 * @since 2022-10-30
 */
public class Util {

    /**
     * 不允许实例化
     */
    private Util() {
    }

    /**
     * 专门用于HEX表示的字母表，数字用对应字符表示，10~15用字母a~f表示
     */
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 用于格式化时间的常量 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如15点23分40秒输出为 152340
     */
    public static final String FORMAT_SHORT_TIME = "HHmmss";

    /**
     * 用于格式化时间的常量 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如15点23分40秒输出为 15:23:40
     */
    public static final String FORMAT_TIME = "HH:mm:ss";

    /**
     * 输出带毫秒数的时间字符串 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如 15:23:40.479
     */
    public static final String FORMAT_FULL_TIME = "HH:mm:ss.SSS";

    /**
     * 输出类似 20221023 的日期字符串 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}
     */
    public static final String FORMAT_SHORT_DATE = "yyyyMMdd";

    /**
     * 输出短划线分隔的日期字符串 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如 2022-10-23
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd";

    /**
     * 输出不带短划线的完整日期时间，不带毫秒 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如 20221023110150
     */
    public static final String FORMAT_SHORT_DATETIME = "yyyyMMddHHmmss";

    /**
     * 输出短划线格式的时间，不带毫秒，常用此格式 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如: 2022-10-23 12:34:56
     */
    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 输出短划线格式带毫秒的完整时间字符串 <br/>
     * 参阅 {@link SimpleDateFormat}, {@link #format(Date, String)}<br/>
     * 例如： 2022-10-23 16:45:12.678
     */
    public static final String FORMAT_FULL_DATETIME = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 每秒的毫秒数，可用于addTime计算使用，例如5秒: 5*MS_SECOND <br/>
     * 用法:
     * <pre>{@code
     * addTime(now(), 30 * MS_SECOND);
     * }</pre>
     * 参阅 {@link #addTime(Date, long)}
     */
    public static final long MS_SECOND = 1000;

    /**
     * 每分钟的毫秒数，可用于addTime计算使用，例如5分钟: 5*MS_MINUTE <br/>
     * 用法:
     * <pre>{@code
     * addTime(now(), 30 * MS_MINUTE);
     * }</pre>
     * 参阅 {@link #addTime(Date, long)}
     */
    public static final long MS_MINUTE = 60 * MS_SECOND;

    /**
     * 每小时的毫秒数，可用于addTime计算使用，例如5小时: 5*MS_HOUR
     * 用法：
     * <pre>{@code
     * addTime(now(), 30 * MS_MINUTE);
     * }</pre>
     * 参阅 {@link #addTime(Date, long)}
     */
    public static final long MS_HOUR = MS_MINUTE * 60;

    /**
     * 每天毫秒数，可用于addTime使用，例如5天: 5*MS_DAY
     * 用法，计算6天前:<pre>{@code
     * addTime(now(), -6 * MS_DAY);
     * }</pre>
     * 参阅 {@link #addTime(Date, long)}
     */
    public static final long MS_DAY = MS_HOUR * 24;

    /**
     * 每周毫秒数，可用于addTime使用，例如5周: 5*MS_WEEK
     * 用法：<pre>{@code
     * addTime(now(), 2 * MS_WEEK);
     * }</pre>
     * 参阅 {@link #addTime(Date, long)}
     */
    public static final long MS_WEEK = MS_DAY * 7;

    private static final Properties properties;

    static {
        properties = loadResourceProperties("application.properties");
    }

    /**
     * 加载资源目录下的配置文件 *.properties ，调用时动态获取新的
     *
     * @param filename 文件名，可带相对路径
     * @return 返回 Properties 对象
     */
    public static Properties loadResourceProperties(String filename) {
        Properties ret = new Properties();
        try {
            InputStream stream = Util.class.getClassLoader().getResourceAsStream(filename);
            if (stream != null) {
                ret.load(stream);
                stream.close();
            }
            return ret;
        } catch (Exception e) {
            return ret;
        }
    }

    /**
     * 加载属性文件
     *
     * @param file 属性文件名，为磁盘文件
     * @return 属性列表
     */
    public static Properties loadProperties(String file) {
        Properties ret = new Properties();
        try {
            InputStream stream = new FileInputStream(file);
            ret.load(stream);
            stream.close();
            return ret;
        } catch (Exception e) {
            return ret;
        }
    }

    /**
     * 对字符串求MD5输出
     *
     * @param value 源字符串
     * @return 返回小写字母hex格式md5数据，失败返回 "" 空字符串
     */
    public static String md5(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(value.getBytes());

            StringBuilder sb = new StringBuilder(64);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 对字符串求MD5输出
     *
     * @param value 源字符串
     * @return 返回小写字母hex格式md5数据，失败返回 "" 空字符串
     */
    public static String sha1(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-1").digest(value.getBytes());

            StringBuilder sb = new StringBuilder(80);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 判断字符串是否为空
     *
     * @param s 字符串
     * @return 空、null，"null" 返回 true, 否则返回 false
     */
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s) || "null".equals(s);
    }

    /**
     * 对象转字符串输出，空对象和异常报错返回 "" 空字符串
     *
     * @param obj 待转换的字符串
     * @return 返回字符串，如果失败或异常，返回 ""
     */
    public static String toStr(Object obj) {
        try {
            if (obj == null) return "";

            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 判断列表中是否有对应数据，例如：
     * <ul>
     *   <li>inList(["abc", "def", "123"], null) ==> false</li>
     *   <li>inList(["abc", "def", "123", null], null) ==> true</li>
     *   <li>inList(["abc", "def", "123"], "xyz") ==> false</li>
     *   <li>inList(["abc", "def", "123"], "123") ==> true</li>
     * </ul>
     *
     * @param list 泛型对象实例列表
     * @param t    泛型对象实例
     * @param <T>  泛型对象类，类需要实现 equals 方法
     * @return 在列表中返回 true，否则返回 false
     */
    public static <T> boolean inList(T[] list, T t) {
        return search(list, t) != -1;
    }

    /**
     * 判断列表中是否有对应数据，例如：
     * <ul>
     *   <li>inList(["abc", "def", "123"], null) ==> false</li>
     *   <li>inList(["abc", "def", "123", null], null) ==> true</li>
     *   <li>inList(["abc", "def", "123"], "xyz") ==> false</li>
     *   <li>inList(["abc", "def", "123"], "123") ==> true</li>
     * </ul>
     *
     * @param list 泛型对象实例列表
     * @param t    泛型对象实例
     * @param <T>  泛型对象类，类需要实现 equals 方法
     * @return 在列表中返回 true，否则返回 false
     */
    public static <T> boolean inList(Collection<T> list, T t) {
        return search(list, t) != -1;
    }

    /**
     * 在数组中查找对应的元素，并返回索引，例如：
     * <ul>
     *   <li>inList(["abc", "def", "123"], null) ==> -1</li>
     *   <li>inList(["abc", "def", "123", null], null) ==> 3</li>
     *   <li>inList(["abc", "def", "123"], "xyz") ==> -1</li>
     *   <li>inList(["abc", "def", "123"], "123") ==> 2</li>
     * </ul>
     *
     * @param list 泛型对象实例列表
     * @param t    泛型对象实例
     * @param <T>  泛型对象类，类需要实现 equals 方法
     * @return 在列表中返回数组下标，否则返回 -1, zero-base
     */
    public static <T> int search(T[] list, T t) {
        for (int i = 0; i < list.length; i++) {
            if (t != null && t.equals(list[i])
                    || (t == null && list[i] == null)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 在数组中查找对应的元素，并返回索引，例如：
     * <ul>
     *   <li>inList(["abc", "def", "123"], null) ==> -1</li>
     *   <li>inList(["abc", "def", "123", null], null) ==> 3</li>
     *   <li>inList(["abc", "def", "123"], "xyz") ==> -1</li>
     *   <li>inList(["abc", "def", "123"], "123") ==> 2</li>
     * </ul>
     *
     * @param list 泛型对象实例列表
     * @param t    泛型对象实例
     * @param <T>  泛型对象类，类需要实现 equals 方法
     * @return 在列表中返回数组下标，否则返回 -1, zero-base
     */
    public static <T> int search(Collection<T> list, T t) {
        int index = 0;
        for (T item : list) {
            if (t != null && t.equals(item)
                    || (t == null && item == null)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * 在数组中查找对应的元素，并返回索引，例如：
     * <ul>
     *   <li>search(null, "abc", "def", "123") ==> -1</li>
     *   <li>search(null, "abc", "def", "123", null) ==> 3</li>
     *   <li>search(5L, 1L, 2L, 3L) ==> -1</li>
     *   <li>search(2L, 1L, 2L, 3L) ==> 1</li>
     * </ul>
     *
     * @param list 泛型对象实例列表
     * @param t    泛型对象实例
     * @param <T>  泛型对象类，类需要实现 equals 方法
     * @return 在列表中返回下标，否则返回 -1，zero-base
     */
    public static <T> int search(T t, T... list) {
        for (int i = 0; i < list.length; i++) {
            if (t != null && t.equals(list[i])
                    || (t == null && list[i] == null)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 判断列表中是否有对应数据，例如:
     * <ul>
     *   <li>inList([1,2,3], 1) ==> true</li>
     *   <li>inList([1,2,3], 4) ==> false</li>
     * </ul>
     *
     * @param list  整数列表
     * @param value 要查找的数据
     * @return 在列表中返回 true，否则返回 false
     */
    public static boolean inList(int[] list, int value) {
        return search(list, value) != -1;
    }

    /**
     * 判断列表中的数据是不是全部是value值，通过 equals 进行判断
     * <p>
     * 示例:
     * <pre>{@code
     *  all([null, null, null], null) => true
     *  all([1, 2, 3, 4], 1) => false
     *  all(["abc", "abc", "abc", "abc"], "abc") => true
     * }</pre>
     *
     * @param list  对象集合
     * @param value 目标值
     * @param <T>   泛型
     * @return 如果所有值均 equals Value，返回 true，否则返回失败
     */
    public static <T> boolean all(T[] list, T value) {
        for (T t : list) {
            if (t != null && !t.equals(value)) {
                return false;
            }

            if (value != null && !value.equals(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断列表中的数据是不是全部是value值，通过 equals 进行判断
     * <p>
     * 示例:
     * <pre>{@code
     *  all([null, null, null], null) => true
     *  all([1, 2, 3, 4], 1) => false
     *  all(["abc", "abc", "abc", "abc"], "abc") => true
     * }</pre>
     *
     * @param list  对象集合
     * @param value 目标值
     * @param <T>   泛型
     * @return 如果所有值均 equals Value，返回 true，否则返回失败
     */
    public static <T> boolean all(Collection<T> list, T value) {
        for (T t : list) {
            if (t != null && !t.equals(value)) {
                return false;
            }

            if (value != null && !value.equals(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断对象是否在一个列表中
     *
     * @param t      目标对象
     * @param values 值列表
     * @param <T>    泛型类型
     * @return 在列表中返回 true，否则返回 false
     */
    public static <T> boolean among(T t, T... values) {
        return search(t, values) != -1;
    }

    /**
     * 在数组中查找并返回下标，例如:
     * <ul>
     *   <li>inList([1,2,3], 1) ==> 0</li>
     *   <li>inList([1,2,3], 4) ==> -1</li>
     * </ul>
     *
     * @param list  整数列表
     * @param value 要查找的数据
     * @return 找到返回下标，否则返回 -1
     */
    public static int search(int[] list, int value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == value) return i;
        }

        return -1;
    }

    /**
     * 判断列表中是否有对应数据，例如:
     * <ul>
     *   <li>inList([1,2,3], 1) ==> true</li>
     *   <li>inList([1,2,3], 4) ==> false</li>
     * </ul>
     *
     * @param list  整数列表
     * @param value 要查找的数据
     * @return 在列表中返回 true，否则返回 false
     */
    public static boolean inList(long[] list, long value) {
        return search(list, value) != -1;
    }

    /**
     * 下载文件
     *
     * @param url      文件URL地址
     * @param filename 保存的文件名
     * @return 成功返回 true，失败返回 false
     */
    public static boolean downloadFile(String url, String filename) {
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();
            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[4096];
            int length;

            FileOutputStream fos = new FileOutputStream(filename);
            while ((length = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从字符串中左边开始获取len个字符
     * <p>
     * 可处理超过字符串长度的情况不会异常，最多取到字符串末尾，例如:
     * <ul>
     *   <li>left("abcd1234", 4) ==> "abcd"</li>
     *   <li>left("abcd1234", 10) ==> "abcd1234"</li>
     *   <li>left("abcd1234", 0) ==> ""</li>
     * </ul>
     *
     * @param s   源字符串
     * @param len 获取的长度
     * @return 返回截取后的字符串
     */
    public static String left(String s, int len) {
        return s.substring(0, Math.min(len, s.length()));
    }

    /**
     * 从字符串中取分隔符左边的字符，例如：
     * <p>
     * left("aaaa=1234", "=") == > "aaaa"<br/>
     * left("aaaa1234", "=") == > "aaaa1234"
     *
     * @param s         源字符串
     * @param separator 分隔字符串
     * @return 如果有分隔符返回分隔符左边的，否则返回原始字符串
     */
    public static String left(String s, String separator) {
        int idx = s.indexOf(separator);
        return s.substring(0, idx == -1 ? s.length() : idx);
    }

    /**
     * 从字符串右边开始，获取len个字符，长度超标不报异常
     *
     * @param s   源字符串
     * @param len 要取的长度，可超过字符串长度，最多取到开头
     * @return 返回截取后的字符串
     */
    public static String right(String s, int len) {
        return s.substring(Math.max(0, s.length() - len));
    }

    /**
     * 从字符串中取分隔符右边的字符
     * <p>
     * 例如 right("aaaa=1234", "=") == > "1234"
     *
     * @param s         源字符串
     * @param separator 分隔字符串
     * @return 如果有分隔符返回分隔符右边的，否则返回原始字符串
     */
    public static String right(String s, String separator) {
        int idx = s.indexOf(separator);
        return s.substring(idx == -1 ? 0 : idx + separator.length());
    }

    /**
     * 把任意字符串试图解释为时间对象，若异常出错返回1970年1月1日零时的时间
     *
     * @param s 任意字符串，可以是 DateFormat 格式也可以是自由是英语格式等
     * @return 返回 date 对象，失败返回 null
     * @see Date#parse(String)
     * @see java.text.DateFormat
     * @see SimpleDateFormat
     */
    @SuppressWarnings("deprecation")
    public static Date strToDate(String s) {
        String[] formats = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss+ZZZ", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss+ZZZ",
                FORMAT_FULL_DATETIME, FORMAT_DATETIME, FORMAT_DATE,
                "yyyyMMddHHmmssSSS", FORMAT_SHORT_DATETIME, FORMAT_SHORT_DATE,
                FORMAT_FULL_TIME, FORMAT_TIME,
                "dd/MMMM/yyyy hh:mm:s z", "MMMM dd yyyy",
                "dd, MMMM yyyy", "MMM dd, yyyy", "MMM dd yyyy"
        };
        try {
            for (String format : formats) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    return simpleDateFormat.parse(s);
                } catch (Exception e) {
                    // nothing
                }
            }
            throw new Exception(s);
        } catch (Exception e) {
            try {
                long l = Date.parse(s);
                return new Date(l);
            } catch (Exception exception) {
                return null;
            }
        }
    }

    /**
     * 验证是否是 E-MAIL 地址格式字符串
     *
     * @param email 待验证字符串
     * @return 符合邮件地址，返回 true，否则返回 false
     */
    public static boolean isValidEmail(String email) {
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是数字字符串，支持前导+-正负数符号
     *
     * @param str 待验证字符串
     * @return 是数字字符串返回true，否则返回false
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[+-]?[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 判断字符串是否是整数
     *
     * @param s 字符串
     * @return 整数返回 true, 否则返回 false
     */
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 判断是否是 long
     *
     * @param s 字符串
     * @return 整数返回 true, 否则返回 false
     */
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是浮点数
     *
     * @param s 字符串
     * @return 是浮点数返回 true, 否则返回 false
     */
    public static boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    校验规则：
        如果为15位，只能是15位数字；前两位满足省/直辖市的行政区划代码。
        如果为18位，允许为18位数字，如出现字母只能在最后一位，且仅能为“X”；
        18位中包含年月的字段满足日期的构成规则；前两位满足省/直辖市的行政区划代码；
        最后一位校验位满足身份证的校验规则（身份证校验规则见附录）。
        附录：身份证校验规则
            公民身份证号码校验公式为RESULT = ∑( A[i] * W[i] ) mod 11。
            其中,i表示号码字符从右至左包括校验码在内的位置序号;A[i]表示第I位置上的数字的数值;W[i]表示第i位置上的加权因子,其值如下:

            i 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2
            W[i] 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2

            RESULT 0 1 2 3 4 5 6 7 8 9 10
            校验码A[1] 1 0 X 9 8 7 6 5 4 3 2
     */
    public static boolean isValidID(String idCard) {
        String[] valCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2"};
        int[] wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

        // 号码的长度 15位或18位
        if (idCard.length() != 15 && idCard.length() != 18) {
            return false;
        }

        // 除最后一位外都为数字
        String fullID;
        if (idCard.length() == 18) {
            fullID = idCard.substring(0, 17);
        } else {
            fullID = idCard.substring(0, 6) + "19" + idCard.substring(6, 15);
        }

        // 必须全为数字
        for (int i = 0; i < fullID.length(); i++) {
            char n = fullID.charAt(i);
            if (n < '0' || n > '9') {
                return false;
            }
        }

        // 出生年月是否有效
        String strYear = fullID.substring(6, 10); // 年份
        String strMonth = fullID.substring(10, 12); // 月份
        String strDay = fullID.substring(12, 14); // 月份
        String birthday = strYear + "-" + strMonth + "-" + strDay;
        if (!isValidDate(birthday)) {
            return false;
        }

        try {
            Calendar gc = Calendar.getInstance();
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            // 年龄应该在 0~150 岁之间
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (s.parse(birthday).getTime() > gc.getTime().getTime())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // 地区码是否有效
        int[] areaCode = new int[]{
                11, 12, 13, 14, 15,             // 北京, 天津, 河北, 山西, 内蒙古
                21, 22, 23,                     // 辽宁, 吉林, 黑龙江
                31, 32, 33, 34, 35, 36, 37,     // 上海, 江苏, 浙江, 安徽, 福建, 江西, 山东
                41, 42, 43, 44, 45, 46,         // 河南, 湖北, 湖南, 广东, 广西, 海南,
                50, 51, 52, 53, 54,             // 重庆, 四川, 贵州, 云南, 西藏,
                61, 62, 63, 64, 65,             // 陕西, 甘肃, 青海, 宁夏, 新疆,
                71, 81, 82, 91                  // 台湾. 香港, 澳门, 国外
        };
        int code = strToInt(fullID.substring(0, 2), 0);
        if (!inList(areaCode, code)) {
            return false;
        }

        if (idCard.length() == 15) return true;

        // 18位，要计算判断最后一位的CRC值
        int sum = 0;
        for (int i = 0; i < idCard.length() - 1; i++) {
            sum += Integer.parseInt(String.valueOf(idCard.charAt(i))) * wi[i];
        }
        String strVerifyCode = valCodeArr[sum % 11];
        return strVerifyCode.equals(idCard.substring(idCard.length() - 1).toLowerCase());
    }

    /**
     * 验证手机号码是否符合格式，仅支持国内手机号码验证
     *
     * @param mobileNumber 待验证手机号码字符串
     * @return 符合手机号码格式返回 true，否则返回 false
     */
    public static boolean isValidPhoneNumber(String mobileNumber) {
        try {
            Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 返回当前日期时间
     *
     * @return 当前日期时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 返回当前的日期时间字符串，格式为yyyy-MM-dd HH:mm:ss格式
     *
     * @return 当前时间字符串
     */
    public static String currentTime() {
        return format(now(), FORMAT_DATETIME);
    }

    /**
     * 返回当前时间文件名，不含路径，例如: 20221106_123456.jpg
     *
     * @param extension 文件名后缀，例如 .jpg
     * @return 返回当前时间的文件名
     */
    public static String getFilename(String extension) {
        return format(now(), "yyyyMMdd_HHmmss") + extension;
    }

    /**
     * 按格式返回当前时间字符串
     *
     * @param format 输出日期格式，格式参考 {@link SimpleDateFormat} ，可使用FORMAT_XXXX常量:
     *               {@link #FORMAT_DATETIME}, {@link #FORMAT_DATE}, {@link #FORMAT_FULL_DATETIME}, {@link #FORMAT_TIME}……
     * @return 格式化的字符串
     */
    public static String now(String format) {
        return format(new Date(), format);
    }

    /**
     * 返回当天日期，不带时间
     *
     * @return 返回当前日期 {@link Date}
     */
    public static Date today() {
        Calendar calendar = Calendar.getInstance();
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 返回当前时间的第二天凌晨时间，不带时间
     *
     * @return 返回日期 {@link Date}
     */
    public static Date tomorrow() {
        Calendar calendar = Calendar.getInstance();
        setTime(calendar, 0, 0, 0, 0);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 返回当前时间的前一天凌晨时间，不带时间
     *
     * @return 返回日期 {@link Date}
     */
    public static Date yesterday() {
        Calendar calendar = Calendar.getInstance();
        setTime(calendar, 0, 0, 0, 0);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * Unix 毫秒时间戳转换为 Date
     *
     * @param millSecond 来自 {@link System#currentTimeMillis()} }
     * @return 返回 {@link Date} 对象
     */
    public static Date unixToDate(long millSecond) {
        Date date = new Date();
        date.setTime(millSecond);
        return date;
    }

    /**
     * 指定时间转换为Unix毫秒时间戳
     *
     * @param date 给定的日期对象
     * @return 返回 long 毫秒时间戳
     */
    public static long dateToUnix(Date date) {
        return date.getTime();
    }

    /**
     * 返回指定时间所在年的开始时间，含凌晨时刻
     * <p>
     * 例如： 2022-10-30 12:34:56 返回 2022-10-30
     *
     * @param date 指定的日期对象，带时间
     * @return 返回 date 对象，时间部分为0
     */
    public static Date beginYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 返回指定时间的日期，不带小时
     * <p>
     * 例如： 2022-10-30 12:34:56 返回 2022-10-30
     *
     * @param date 指定的日期对象，带时间
     * @return 返回 date 对象，时间部分为0
     */
    public static Date beginDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 返回指定时间的一天的结束时间，即该天午夜时间
     *
     * @param date 给定的日期
     * @return 返回 date 当天的午夜时间
     */
    public static Date endDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTime(calendar, 23, 59, 59, 999);
        return calendar.getTime();
    }

    /**
     * 获取给定季度的开始和结束时间字符串 <br/>
     * 开始时间在返回数组第一个元素，结束时间在返回数组第二个元素
     *
     * @param year   年份
     * @param season 季度，1~4
     * @return 返回两个元素的字符串数据，第一个为开始范围，第二个为季度的结束时间
     */
    public static String[] getSeasonRange(String year, int season) {
        assert season >= 1 && season <= 4;

        String[] range = new String[2];
        if (season == 1) {
            range[0] = year.concat("-01-01 00:00:00");
            range[1] = year.concat("-03-31 23:59:59");
        } else if (season == 2) {
            range[0] = year.concat("-04-01 00:00:00");
            range[1] = year.concat("-06-30 23:59:59");
        } else if (season == 3) {
            range[0] = year.concat("-07-01 00:00:00");
            range[1] = year.concat("-09-30 23:59:59");
        } else {
            range[0] = year.concat("-10-01 00:00:00");
            range[1] = year.concat("-12-31 23:59:59");
        }

        return range;
    }

    /**
     * 获取给定季度的开始和结束时间字符串 <br/>
     * 开始时间在返回数组第一个元素，结束时间在返回数组第二个元素
     *
     * @param date 待返回所在季节返回的日期
     * @return 字符串数组，第一个为所在季节开始时间，第二个为所在季节末尾时间，格式为 yyyy-MM-dd hh:mm:ss
     */
    public static String[] getSeasonRange(Date date) {
        int season = getSeason(date);
        assert season >= 1 && season <= 4;
        return getSeasonRange(String.valueOf(getYear(date)), season);
    }

    /**
     * 返回日期对象的年份
     *
     * @param date 待返回年份的 date
     * @return 年份
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 返回日期对象的月份
     *
     * @param date 待返回月份的 date
     * @return 月份，1~12
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 返回日期对象的日期
     *
     * @param date 待返回月份的 date
     * @return 月份，1~12
     */
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 返回给定时间的所在季度的开始
     *
     * @param date 给定的时间
     * @return date 所在季度的开始时刻，含凌晨时刻
     */
    public static Date beginSeason(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTime(calendar, 0, 0, 0, 0);
        int month = calendar.get(Calendar.MONTH) + 1;

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        if (between(month, 1, 3)) {
            calendar.set(Calendar.MONTH, 0);
        } else if (between(month, 4, 6)) {
            calendar.set(Calendar.MONTH, 3);
        } else if (between(month, 7, 9)) {
            calendar.set(Calendar.MONTH, 6);
        } else if (between(month, 10, 12)) {
            calendar.set(Calendar.MONTH, 9);
        }

        return calendar.getTime();
    }

    /**
     * 给日历设置时间部分
     *
     * @param calendar   待设置的日历对象
     * @param hour       小时，0~23
     * @param minute     分钟，0~59
     * @param second     秒数，0~59
     * @param millSecond 毫秒数，0~999
     * @return 返回原calendar对象
     */
    public static Calendar setTime(Calendar calendar, int hour, int minute, int second, int millSecond) {
        calendar.set(Calendar.MILLISECOND, millSecond);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar;
    }

    /**
     * 给日历设置日期部分
     *
     * @param calendar 待设置的日历对象
     * @param year     年份
     * @param month    月份，1~12
     * @param day      天，1~31
     * @return 返回原calendar对象
     */
    @SuppressWarnings("MagicConstant")
    public static Calendar setDate(Calendar calendar, int year, int month, int day) {
        calendar.set(year, month - 1, day);
        return calendar;
    }

    /**
     * 返回给定时间的所在周的开始
     *
     * @param date 给定的时间
     * @return date 所在周的开始时刻，含凌晨时刻
     */
    public static Date beginWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 返回给定时间的所在季度的末尾时刻，包含午夜时刻
     *
     * @param date 给定的时间
     * @return date 所在季度末尾时刻
     */
    public static Date endSeason(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;
        if (between(month, 1, 3)) {
            calendar.set(Calendar.MONTH, 2);
        } else if (between(month, 4, 6)) {
            calendar.set(Calendar.MONTH, 5);
        } else if (between(month, 7, 9)) {
            calendar.set(Calendar.MONTH, 8);
        } else if (between(month, 10, 12)) {
            calendar.set(Calendar.MONTH, 11);
        }
        return endMonth(calendar.getTime());
    }

    /**
     * 返回给定时间的所在年度的末尾时刻，包含午夜时刻
     *
     * @param date 给定的时间
     * @return date 所在年度末尾时刻
     */
    public static Date endYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setTime(calendar, 23, 59, 59, 999);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        return calendar.getTime();
    }

    /**
     * 返回给定时间的所在周的末尾时刻，包含午夜时刻
     *
     * @param date 给定的时间
     * @return date 所在周末尾时刻
     */
    public static Date endWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        setTime(calendar, 23, 59, 59, 999);
        return calendar.getTime();
    }

    /**
     * 返回两个time2 - time1 时间的毫秒差
     *
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @return 时间差可能出现复数，如果 time2 > time1，返回正数，否则返回负数
     */
    public static long diffTime(Date time1, Date time2) {
        return time2.getTime() - time1.getTime();
    }

    /**
     * 返回两个时间差的秒数
     *
     * @param t1 第一个时间
     * @param t2 第二个时间
     * @return 时间差可能出现复数，如果 time2 > time1，返回正数，否则返回负数
     */
    public static long diffSecond(Date t1, Date t2) {
        return diffTime(t1, t2) / MS_SECOND;
    }

    /**
     * 返回两个时间差的分钟数
     *
     * @param t1 第一个时间
     * @param t2 第二个时间
     * @return 时间差可能出现复数，如果 time2 > time1，返回正数，否则返回负数
     */
    public static long diffMinute(Date t1, Date t2) {
        return diffTime(t1, t2) / MS_MINUTE;
    }

    /**
     * 返回两个时间差的小时数
     *
     * @param t1 第一个时间
     * @param t2 第二个时间
     * @return 时间差可能出现复数，如果 time2 > time1，返回正数，否则返回负数
     */
    public static long diffHour(Date t1, Date t2) {
        return diffTime(t1, t2) / MS_HOUR;
    }

    /**
     * 返回两个时间差的天数
     *
     * @param t1 第一个时间
     * @param t2 第二个时间
     * @return 时间差可能出现复数，如果 time2 > time1，返回正数，否则返回负数
     */
    public static long diffDay(Date t1, Date t2) {
        return diffTime(t1, t2) / MS_DAY;
    }

    /**
     * 日期相加指定的毫秒数
     * <p>
     * 示例代码，时间 + 5 秒
     * <pre>{@code
     * addTime(now (), 5 * MS_SECOND)
     * }</pre>
     *
     * @param time     基准时间
     * @param mSeconds 待相加的毫秒数，可以输入负数，负数表示基准时间之前的时间，
     *                 参阅: {@link #MS_SECOND}, {@link #MS_DAY}, {@link #MS_HOUR}, {@link #MS_MINUTE}, {@link #MS_SECOND}……
     * @return 返回日期对象
     */
    public static Date addTime(Date time, long mSeconds) {
        return new Date(time.getTime() + mSeconds);
    }

    /**
     * 日期相加指定的秒数
     *
     * @param time    基准时间
     * @param seconds 待相加的秒数，可以输入负数，负数表示基准时间之前的时间，参阅: {@link #MS_SECOND}
     * @return 返回时间相加后的日期
     */
    public static Date addSecond(Date time, long seconds) {
        return new Date(time.getTime() + seconds * MS_SECOND);
    }

    /**
     * 日期相加指定的分钟
     *
     * @param time    基准时间
     * @param minutes 待相加的分钟，可以输入负数，负数表示基准时间之前的时间，参阅 {@link #MS_MINUTE}
     * @return 返回时间相加后的日期
     */
    public static Date addMinute(Date time, long minutes) {
        return new Date(time.getTime() + minutes * MS_MINUTE);
    }

    /**
     * 日期相加指定的小时数
     * <p>
     * 例如，时间 + 5 小时:<pre>{@code
     * addHour(now(), 5 * MS_HOUR);
     * }</pre>
     *
     * @param time  基准时间
     * @param hours 待相加的小时数，可以输入负数，负数表示基准时间之前的时间，参阅 {@link #MS_HOUR}
     * @return 返回时间相加后的日期
     * @see #addMinute(Date, long)
     * @see #addDay(Date, long)
     * @see #addTime(Date, long)
     * @see #addSecond(Date, long)
     */
    public static Date addHour(Date time, long hours) {
        return new Date(time.getTime() + hours * MS_HOUR);
    }

    /**
     * 日期相加指定的天数
     *
     * @param time 基准时间
     * @param days 待相加的天数，可以输入负数，负数表示基准时间之前的时间
     * @return 返回时间相加后的日期
     * @see #MS_DAY
     * @see #addTime(Date, long)
     */
    public static Date addDay(Date time, long days) {
        return new Date(time.getTime() + days * MS_DAY);
    }

    /**
     * 日期相加指定的月份
     *
     * @param date   给定的时间
     * @param months 要相加的月份数
     * @return 返回 date 增加指定月份数后的时间，除年月日数据变化外，时间部分不变
     */
    public static Date addMonth(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 获取指定时间所在的季度
     *
     * @param date 给定的时间
     * @return 返回季度（0~4），0=出错，第一季度=1，第二季度=2，依此类推
     */
    public static int getSeason(Date date) {
        if (date == null) return 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int m = calendar.get(Calendar.MONTH) + 1;

        if (m <= 3) {
            return 1;
        } else if (m <= 6) {
            return 2;
        } else if (m <= 9) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * 给定日期对象判断是否是闰年
     *
     * @param date 给定的日期
     * @return 闰年返回 true，平年返回 false
     */
    public static boolean isLeapYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        return isLeapYear(year);
    }

    /**
     * 判断是否是闰年
     * <p>
     * 闰年： 能被400整除 或者 能被4整除且不是100的倍数
     *
     * @param year 年份，例如 2022
     * @return 闰年返回 true，平年返回 false
     */
    public static boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    /**
     * 返回给定日期所在月份第一天的时间
     *
     * @param date 给定的时间
     * @return 返回 date 所在月份第一天的日期，返回的日期不含时间部分
     */
    public static Date beginMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 返回给定日期所在月份第一天的时间
     *
     * @param date 给定的时间
     * @return 返回 date 所在月份最后一天的日期，返回时间包括最后一天的午夜时分
     */
    public static Date endMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, max);
        setTime(calendar, 23, 59, 59, 999);
        return calendar.getTime();
    }

    /**
     * 比较两个Date类型日期是否相等，忽略时间
     *
     * @return boolean
     */
    public static boolean sameDay(Date date1, Date date2) {
        if (date1 == date2) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        }
        // 不能直接使用 date1.getTime() / MS_DAY == date2.getTime() / MS_DAY
        // 因为有时区差，例如同样是今天，但是东八区 8:00 前会计算到前一天去，实际上当地时间都是同一天
        String str1 = format(date1, "yyyy-MM-dd");
        String str2 = format(date2, "yyyy-MM-dd");
        return equal(str1, str2);
    }

    /**
     * 比较两个Date对象的时间是否相同，不考虑日期
     *
     * @return boolean 如果时间相同返回true，否则返回false
     */
    public static boolean sameTime(Date date1, Date date2) {
        if (date1 == date2) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() % MS_DAY == date2.getTime() % MS_DAY;
    }

    /**
     * 比较两个Date类型是否相等
     *
     * @return boolean
     */
    public static boolean sameDate(Date date1, Date date2) {
        if (date1 == date2) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        }

        return date1.getTime() == date2.getTime();
    }

    /**
     * 返回给定的时间是否在范围内
     *
     * @param time  给定的时间
     * @param begin 开始时间
     * @param end   结束时间，应该大于开始时间
     * @return 在给定范围内，返回 true, 否则返回 false
     */
    public static boolean between(Date time, Date begin, Date end) {
        assert begin.getTime() <= end.getTime();

        return (!time.after(end)) && (!time.before(begin));
    }

    /**
     * 返回给定的时间是否在范围内，例如 <br/>
     * between("08:00:00", "07:00:00", "18:00:00")
     *
     * @param time  给定的时间
     * @param begin 开始时间
     * @param end   结束时间，应该大于开始时间
     * @return 在给定范围内，返回 true, 否则返回 false
     */
    public static boolean between(String time, String begin, String end) throws ParseException {
        String format = "HH:mm:ss";
        Date t = new SimpleDateFormat(format).parse(time);
        Date b = new SimpleDateFormat(format).parse(begin);
        Date e = new SimpleDateFormat(format).parse(end);
        return between(t, b, e);
    }

    /**
     * 判断整数是否在开始和结束范围内，包含开始和结束值
     *
     * @param value 待判定的值
     * @param begin 开始值
     * @param end   结束值，应该大于开始值
     * @return 在给定范围内，返回 true, 否则返回 false
     */
    public static boolean between(long value, long begin, long end) {
        return value >= begin && value <= end;
    }

    /**
     * 判断浮点数是否在开始和结束范围内，包含开始和结束
     *
     * @param value 待判定的值
     * @param begin 开始值
     * @param end   结束值，应该大于开始值
     * @return 在给定范围内，返回 true, 否则返回 false
     */
    public static boolean between(double value, double begin, double end) {
        return value >= begin && value <= end;
    }

    /**
     * 把类似 2022-10-23 13:06:12.565 格式字符串转为Date对象，字符串的时间为本地时间
     *
     * @return 格式化后的Date类型
     */
    public static Date date(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATETIME);
        return simpleDateFormat.parse(date);
    }

    /**
     * String类型格式化指定格式的Date类型，为本地时间
     *
     * @return 格式化后的Date类型
     */
    public static Date date(String date, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(date);
    }

    /**
     * String类型格式化指定格式的Date类型，时间数据为UTC数据
     *
     * @return 格式化后的Date类型
     */
    public static Date dateUTC(String date, String format) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone("GMT+0");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.parse(date);
    }

    /**
     * 对浮点数 x 向上取整返回整数
     *
     * @param x 待处理的浮点数
     * @return 返回整数
     */
    public static long roundUp(double x) {
        return (long) Math.ceil(x);
    }

    /**
     * 对浮点数 x 向下取整返回整数
     *
     * @param x 待处理的浮点数
     * @return 返回整数
     */
    public static long roundDown(double x) {
        return (long) Math.floor(x);
    }

    /**
     * Date类型格式化成String类型， {@link SimpleDateFormat},
     * 可使用 FORMAT_XXX常量，例如{@link #FORMAT_TIME}, {@link #FORMAT_DATETIME}……
     *
     * @return String类型日期
     */
    public static String format(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 从年月日时分秒得到Date对象，年月日时分秒为本地时间数据
     *
     * @param year    本地年份
     * @param month   本地月份，1月为1，2月为2，以此类推
     * @param day     本地天，0~31
     * @param hour    本地小时，0~23
     * @param minute  本地分钟，0~59
     * @param second  本地秒数，0~59
     * @param mSecond 本地时间毫秒数，0~999
     * @return Date对象
     */
    @SuppressWarnings("MagicConstant")
    public static Date date(int year, int month, int day, int hour, int minute, int second, int mSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, mSecond);
        return calendar.getTime();
    }

    /**
     * 根据年月日返回Date对象
     *
     * @param year  本地时间年份
     * @param month 本地时间月份，范围：1~12
     * @param day   本地时间日子，范围：1~31
     * @return 返回 date 对象，不带时间部分，即时间部分全部为0
     */
    @SuppressWarnings("MagicConstant")
    public static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        setTime(calendar, 0, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 从年月日时分秒得到Date对象，年月日时分秒为UTC时间数据
     *
     * @param year   本地年份
     * @param month  本地月份，1月为1，2月为2，以此类推
     * @param day    本地天
     * @param hour   本地小时
     * @param minute 本地分钟
     * @param second 本地秒数
     * @return Date对象
     */
    @SuppressWarnings("MagicConstant")
    public static Date dateUTC(int year, int month, int day, int hour, int minute, int second, int mSecond) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, mSecond);
        return calendar.getTime();
    }


    /**
     * 判断是否是合法日期，字符串格式: yyyy-MM-dd，能严格匹配大小月份，闰年等各种情况
     *
     * @param str 待验证字符串
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidDate(String str) {
        boolean flag = false;
        String regxStr = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$";
        Pattern pattern1 = Pattern.compile(regxStr);
        Matcher isNo = pattern1.matcher(str);
        if (isNo.matches()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 检验日期时间数据是否合法，可以校验大小月份，闰年，对应的数值范围是否合法
     *
     * @param year    年份，四位数
     * @param month   月份，1~12范围
     * @param day     天
     * @param hour    小时
     * @param minute  分钟
     * @param second  秒
     * @param mSecond 毫秒
     */
    private static boolean isValidDate(int year, int month, int day, int hour, int minute, int second, int mSecond) {
        if (!between(hour, 0, 23)) return false;
        if (!between(minute, 0, 59)) return false;
        if (!between(second, 0, 59)) return false;
        if (!between(mSecond, 0, 999)) return false;

        if (!between(month, 1, 12)) return false;
        if (month == 2) {
            if (isLeapYear(year)) {
                return (between(month, 1, 29));
            } else {
                return (between(month, 1, 28));
            }
        } else if (inList(new int[]{1, 3, 5, 7, 8, 10, 12}, month)) {
            return (between(day, 1, 31));
        } else {
            return between(month, 1, 30);
        }
    }

    /**
     * 对字符串中的正则表达式特殊字符进行转义，以便可以用于正则表达式中
     *
     * @param str 待转移的字符串
     * @return 转义后的字符串
     */
    public static String escapeRegexp(String str) {
        // 如果输入字符串中有 \ ，需要替换为 \\，避免正则表达式处理错误
        str = str.replace("\\", "\\\\");
        for (char ch : ".?![]{}()<>*+-=^$|".toCharArray()) {
            str = str.replace(String.valueOf(ch), "\\" + ch);
        }
        return str;
    }

    /**
     * 判断字符串是否是合法的日期格式
     * <p>
     * 支持占位符，占位符含义 <ul>
     * <li>%y: 代表年</li>
     * <li>%m: 代表月</li>
     * <li>%d: 代表日</li>
     * <li>%h: 代表小时</li>
     * <li>%n: 代表分钟</li>
     * <li>%s: 代表秒</li>
     * </ul>
     * <pre>{@code
     * isValidDate("2022-10-30 12:34:56", null);
     * isValidDate("2022年10月30日 12:34:56", "$y年%m%日 %h:%n:%s");
     * isValidDate("2022/10/30 12:34:56", "%y/%m%d %h:%n:%s");
     * isValidDate("10/30 12:34:56", "%m%d %h:%n:%s")
     * isValidDate("10-30, 2022 12:34:56", "%m-%d, %y %h:%n:%s");
     * }</pre>
     *
     * @param value  字符串
     * @param format 格式，可以为 null，即默认 "%y-%m-%d %h:%n:%s" 的格式
     * @return 有效日期返回 true，否则返回 false
     */
    public static boolean isValidDate(String value, String format) {
        if (format == null) format = "%y-%m-%d %h:%n:%s";

        format = escapeRegexp(format);
        String s = format.replace("%y", "(\\d+)")
                .replace("%m", "(\\d+)")
                .replace("%d", "(\\d+)")
                .replace("%h", "(\\d+)")
                .replace("%n", "(\\d+)")
                .replace("%s", "(\\d+)");
        Pattern pattern = Pattern.compile(s);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) return false;
        int[] values = new int[matcher.groupCount()];
        for (int i = 0; i < matcher.groupCount(); i++) {
            values[i] = strToInt(matcher.group(i + 1), -1);
        }

        // 求占位符在格式中出现的顺序，对应的就是正则表达式结果中的顺序
        int yearOrder = -1;
        int monthOrder = -1;
        int dayOrder = -1;
        int hourOrder = -1;
        int minuteOrder = -1;
        int secondOrder = -1;

        int index = 0;
        for (int i = 1; i < format.length(); i++) {
            if (format.charAt(i) == 'y' && format.charAt(i - 1) == '%') {
                yearOrder = index++;
            } else if (format.charAt(i) == 'm' && format.charAt(i - 1) == '%') {
                monthOrder = index++;
            } else if (format.charAt(i) == 'd' && format.charAt(i - 1) == '%') {
                dayOrder = index++;
            } else if (format.charAt(i) == 'h' && format.charAt(i - 1) == '%') {
                hourOrder = index++;
            } else if (format.charAt(i) == 'n' && format.charAt(i - 1) == '%') {
                minuteOrder = index++;
            } else if (format.charAt(i) == 's' && format.charAt(i - 1) == '%') {
                secondOrder = index++;
            }
        }

        int year = yearOrder == -1 ? 1900 : values[yearOrder];
        int month = monthOrder == -1 ? 1 : values[monthOrder];
        int day = dayOrder == -1 ? 1 : values[dayOrder];
        int hour = hourOrder == -1 ? 0 : values[hourOrder];
        int minute = minuteOrder == -1 ? 0 : values[minuteOrder];
        int second = secondOrder == -1 ? 0 : values[secondOrder];
        return isValidDate(year, month, day, hour, minute, second, 0);
    }

    /**
     * 返回当前带毫秒的时间字符串，不带日期
     *
     * @return 返回当前时间，带毫秒时间戳
     */
    public static String time() {
        return format(now(), FORMAT_FULL_TIME);
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 相等返回 true，否则返回 false
     */
    public static boolean equal(String s1, String s2) {
        if (s1 != null) return s1.equals(s2);

        return s2 == null;
    }

    /**
     * 比较两个字符串是否相等，忽略大小写
     *
     * @param s1            字符串1
     * @param s2            字符串2
     * @param caseSensitive 是否大小写敏感， true 区分大小写，false不区分大小写
     * @return 相等返回 true，不等返回 false
     */
    public static boolean equal(String s1, String s2, boolean caseSensitive) {
        if (s1 != null) {
            if (caseSensitive) {
                return s1.equals(s2);
            } else {
                return s1.equalsIgnoreCase(s2);
            }
        }

        return s2 == null;
    }

    /**
     * 比较两个浮点数是否相等，精度为浮点数最小精度1E-6
     *
     * @param f1 浮点数1
     * @param f2 浮点数2
     * @return 默认精度内相等返回 true，否则返回 false
     */
    public static boolean equal(float f1, float f2) {
        return Math.abs(f1 - f2) < 1E-6;
    }

    /**
     * 比较两个double数是否相等，精度为double最小精度1E-15
     *
     * @param f1 浮点数1
     * @param f2 浮点数2
     * @return 默认精度内相等返回 true，否则返回 false
     */
    public static boolean equal(double f1, double f2) {
        return Math.abs(f1 - f2) < 1E-15;
    }

    /**
     * 控制台输出日志信息，带 [E] 和时间戳信息
     *
     * @param message 日志文本
     */
    public static void logError(String message) {
        System.out.println("[E] " + time() + " " + message);
    }

    /**
     * 控制台输出Info信息，带 [I] 和时间戳信息
     *
     * @param message 日志文本
     */
    public static void logInfo(String message) {
        System.out.println("[I] " + time() + " " + message);
    }

    /**
     * 控制台输出日志信息，带 [D] 和时间戳信息
     *
     * @param message 日志文本
     */
    public static void logDebug(String message) {
        System.out.println("[D] " + time() + " " + message);
    }

    /**
     * 控制台输出日志信息，带 [W] 和时间戳信息
     *
     * @param message 日志文本
     */
    public static void logWarn(String message) {
        System.out.println("[W] " + time() + " " + message);
    }

    /**
     * 读取整个文件到内存对象
     *
     * @param filename 待读取的文件名
     * @return 返回字符串列表
     * @throws IOException 读取异常
     */
    public static List<String> loadFromFile(String filename) throws IOException {
        return Files.readAllLines(Paths.get(filename));
    }

    /**
     * 把List对象保存到文件，一般把字符串保存到文件
     *
     * @param list     待保存对象列表
     * @param filename 保存的文件名
     * @return 成功返回 true，失败返回 false
     */
    public static boolean saveToFile(List<?> list, String filename) {
        try {
            FileWriter fileWriter = new FileWriter(filename);
            boolean first = true;
            for (Object item : list) {
                if (first) {
                    fileWriter.write(toStr(item));
                } else {
                    fileWriter.write(System.lineSeparator() + toStr(item));
                }
                first = false;
            }
            fileWriter.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 保存对象到文件，Object必须实现了 Serializable 接口，保存的文件内容包含对象相关信息，而不是纯粹的字符串
     *
     * @param object   Serializable对象
     * @param filename 文件名
     * @return 成功返回 true，失败返回 false
     * @see #loadObject(String)
     */
    public static boolean saveObject(Object object, String filename) {
        try {
            FileOutputStream stream = new FileOutputStream(filename);
            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
            outputStream.writeObject(object);
            outputStream.close();
            stream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 保存多个对象到文件，Object必须实现了 Serializable 接口，保存的文件内容包含对象相关信息，而不是纯粹的字符串
     *
     * @param objects  Serializable对象
     * @param filename 文件名
     * @return 成功返回 true，失败返回 false
     * @see #loadObject(String)
     */
    public static boolean saveObjects(String filename, Object... objects) {
        try {
            FileOutputStream stream = new FileOutputStream(filename);
            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
            for (Object object : objects) {
                outputStream.writeObject(object);
            }
            outputStream.close();
            stream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从文件中读取多个对象
     *
     * @param filename 文件名
     * @return 成功返回 true，失败返回 false
     * @see #loadObject(String)
     */
    public static List<Object> loadObjects(String filename) {
        List<Object> ret = new ArrayList<>();
        Object obj;
        try {
            FileInputStream stream = new FileInputStream(filename);
            ObjectInputStream inputStream = new ObjectInputStream(stream);

            while (true) try {
                obj = inputStream.readObject();
                ret.add(obj);
            } catch (EOFException e) {
                break;
            }
            inputStream.close();
            stream.close();
            return ret;
        } catch (Exception e) {
            return ret;
        }
    }

    /**
     * 从文件中读取序列化对象，配套 {@link #saveObject(Object, String)} 使用
     *
     * @param filename 文件名
     * @return 返回读取的对象，如果失败返回 null
     */
    public static <T> T loadObject(String filename) {
        try {
            FileInputStream stream = new FileInputStream(filename);
            ObjectInputStream inputStream = new ObjectInputStream(stream);
            T object = (T) inputStream.readObject();
            inputStream.close();
            stream.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从文件读取全部内容并返回字符串
     *
     * @param fileName 待读取文件名
     * @return 成功返回文件内容，失败返回 null
     */
    public static String stringFromFile(String fileName) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileInputStream fis = new FileInputStream(fileName);
            byte[] buf = new byte[fis.available()];
            readBuffer(fis, buf, buf.length);
            fis.close();
            return new String(buf);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 读取数据到缓冲区，不断读够直到达到预期长度为止
     *
     * @param stream 输入流
     * @param buf    缓冲区
     * @param len    期望读取的长度
     * @throws IOException 异常
     */
    public static void readBuffer(InputStream stream, byte[] buf, int len) throws IOException {
        int count = 0;
        while (count < len) {
            int size = stream.read(buf, count, len - count);
            if (size == -1) return;
            count += size;
        }
    }

    /**
     * 保存字符串到文件，若存在会覆盖，不存在会创建
     *
     * @param fileName 保存的文件名
     * @param content  文件内容
     * @return 成功返回 true，失败返回 false
     */
    public static boolean stringToFile(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 连接对象数组为字符串输出
     *
     * @param list      列表对象，可以为字符串或对象
     * @param separator 分隔符
     * @param <T>       泛型对象
     * @return 返回拼接后的字符串
     */
    public static <T> String join(T[] list, String separator) {
        if (list == null || list.length == 0) return "";

        StringBuilder sb = new StringBuilder(1000);
        if (list.length > 1) {
            sb.append(toStr(list[0]));
        }
        for (int i = 1; i < list.length; i++) {
            sb.append(separator);
            sb.append(toStr(list[i]));
        }
        return sb.toString();
    }

    /**
     * 连接对象不定参数为字符串输出
     *
     * @param params    列表对象，可以为字符串或对象
     * @param separator 分隔符
     * @param <T>       泛型对象
     * @return 返回拼接后的字符串
     */
    public static <T> String join(String separator, T... params) {
        StringBuilder sb = new StringBuilder(1000);
        if (params.length > 1) {
            sb.append(toStr(params[0]));
        }
        for (int i = 1; i < params.length; i++) {
            sb.append(separator);
            sb.append(toStr(params[i]));
        }
        return sb.toString();
    }


    /**
     * 连接List对象为字符串输出
     *
     * @param list      列表对象，可以为字符串或对象
     * @param separator 分隔符
     * @param <T>       泛型对象
     * @return 返回拼接后的字符串
     */
    public static <T> String join(Collection<T> list, String separator) {
        if (list == null || list.size() == 0) return "";

        boolean first = true;
        StringBuilder sb = new StringBuilder(1000);
        for (T item : list) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(toStr(item));
            first = false;
        }
        return sb.toString();
    }

    public static String join(long[] list, String separator) {
        if (list == null || list.length == 0) return "";

        StringBuilder sb = new StringBuilder(1000);
        if (list.length > 1) {
            sb.append(toStr(list[0]));
        }
        for (int i = 1; i < list.length; i++) {
            sb.append(separator);
            sb.append(toStr(list[i]));
        }
        return sb.toString();
    }

    /**
     * @param list      数组
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(int[] list, String separator) {
        if (list == null || list.length == 0) return "";

        StringBuilder sb = new StringBuilder(1000);
        if (list.length > 1) {
            sb.append(toStr(list[0]));
        }
        for (int i = 1; i < list.length; i++) {
            sb.append(separator);
            sb.append(toStr(list[i]));
        }
        return sb.toString();
    }

    /**
     * 扫描目录下的文件，递归扫描所有子目录，速度比较快
     *
     * @param dir 待扫描的目录
     * @return 返回文件名列表
     */
    public static Set<String> listFile(String dir) {
        Set<String> fileSet = new HashSet<>();
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
                for (Path path : stream) {
                    if (!Files.isDirectory(path)) {
                        fileSet.add(dir + File.separator + path.getFileName().toString());
                    } else {
                        fileSet.addAll(listFile(path.toString()));
                    }
                }
            }
        } catch (Exception e) {
        }
        return fileSet;
    }

    /**
     * 返回临时文件名，若无法创建临时文件，返回 null
     *
     * @param prefix 文件名前缀，最小3个字符，可以为空，默认 tmp 前缀
     * @param suffix 文件名后缀，可以为空
     * @return 完整的临时文件名，失败返回 null
     */
    public static String getTempFilename(String prefix, String suffix) {
        try {
            prefix = prefix == null ? "tmp" : prefix;

            File file = File.createTempFile(prefix, suffix);
            String ret = file.getAbsolutePath();
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 返回范围内的随机数
     *
     * @param min 最小值
     * @param max 最大范围
     * @return 随机数
     */
    public static int rand(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * 返回随机整数
     *
     * @return 返回随机数
     */
    public static int rand() {
        return new Random().nextInt();
    }

    /**
     * 返回随机数
     *
     * @param max 最大范围，生成的随机数不会超过该值
     * @return 随机数
     */
    public static int rand(int max) {
        return new Random().nextInt(max);
    }

    /**
     * 十六进制String转byte[]，Hex字符串必须前导0格式，即不足的必须补0对齐
     *
     * @param s 源十六进制字符串
     * @return 返回转换后的二进制数组数据
     */
    public static byte[] hex2bin(String s) {
        if (s == null) return null;
        String str = s.replace(" ", "");
        if (str.length() == 0) return new byte[0];

        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * 整个byte[]转十六进制String
     *
     * @param byteArray 二进制数据
     * @param space     是否保持空格， true hex之间保持空格，false 不保持
     * @return 返回十六进制字符串
     */
    public static String bin2hex(byte[] byteArray, boolean space) {
        if (byteArray == null) return null;
        int size = space ? 3 : 2;
        char[] hexChars = new char[byteArray.length * size];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * size] = HEX_DIGITS[v >>> 4];
            hexChars[j * size + 1] = HEX_DIGITS[v & 0x0F];
            if (space) hexChars[j * size + 2] = ' ';
        }
        return new String(hexChars);
    }

    /**
     * 把 byte[] 指定长度数据转换为十六进制字符串
     *
     * @param byteArray 二进制数据
     * @param len       需要转换的长度
     * @param space     是否保持空格， true hex之间保持空格，false 不保持
     * @return 返回转换后的十六进制字符串
     */
    public static String bin2hex(byte[] byteArray, int len, boolean space) {
        if (byteArray == null) return null;
        int size = space ? 3 : 2;
        char[] hexChars = new char[len * size];
        for (int j = 0; j < len; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * size] = HEX_DIGITS[v >>> 4];
            hexChars[j * size + 1] = HEX_DIGITS[v & 0x0F];
            if (space) hexChars[j * size + 2] = ' ';
        }
        return new String(hexChars);
    }

    /**
     * 采用累加和取反的校验方式计算CRC
     * 所有字节进行算术累加，抛弃高位，只保留最后单字节，将单字节取反；
     *
     * @param data 需要计算的数据
     * @return 结果
     */
    public static byte crc8(byte[] data) {
        int r = 0;
        for (byte datum : data) r += datum;
        byte b = (byte) (r & 0x00FF);
        return (byte) ~b;
    }

    /**
     * 异常的调用堆栈输出为字符串
     *
     * @param e 异常对象
     * @return 异常调用栈详细信息
     */
    public static String dumpExceptionStack(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 把线程调用栈转录成字符串
     *
     * @param thread 需要转录的线程
     * @return 返回线程调用栈信息
     */
    public static String dumpThreadStack(Thread thread) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : thread.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 把字节转换为可读化的字符串显示
     *
     * @param bytes 字节数
     * @param si    是否科学计数，true，使用1000计算，false，按1024计算
     * @return 可读化的字节显示
     */
    public static String readableSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "i" : "");
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * 字符串转整数，带默认值
     *
     * @param str          要转换的字符串
     * @param defaultValue 默认值
     * @return 成功返回原始对应的数值，异常转换失败返回默认值
     */
    public static int strToInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception nfe) {
            return defaultValue;
        }
    }

    public static String getUptime(long seconds) {
        long day = seconds / 86400;
        long hour = (seconds / 3600) % 24;
        long minute = (seconds / 60) % 60;
        long second = seconds % 60;
        String ret = "";
        if (day > 0) ret += " " + day + " 天";
        if (hour > 0) ret += " " + hour + " 小时";
        if (minute > 0) ret += " " + minute + " 分钟";
        if (second > 0) ret += " " + second + " 秒";
        return ret.trim();
    }

    /**
     * 求两个日期的年份差，周年数差，忽略时间部分
     *
     * @param start 开始日期
     * @param end   结束日期，应该大于 start 日期
     * @return 返回年份差
     */
    public static int diffYear(Date start, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);
        int startDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(end);
        int endYear = calendar.get(Calendar.YEAR);
        int endMonth = calendar.get(Calendar.MONTH);
        int endDay = calendar.get(Calendar.DAY_OF_MONTH);

        int ret = endYear - startYear;
        if (endMonth < startMonth) {
            ret--;
        } else if (endMonth == startMonth && endDay < startDay) {
            ret--;
        }
        return ret;
    }

    /**
     * 根据生日获取当前年龄周岁
     *
     * @param birthDay 生日日期
     * @return 返回年龄整数值，为周岁数据
     */
    public static int getAge(Date birthDay) {
        return diffYear(birthDay, new Date()) + 1;
    }

    /**
     * 生成随机字符串
     *
     * @param len         生成字符串的长度
     * @param lowerCase   是否包括小写字符， true = 包含， false = 不包含
     * @param upperCase   是否包括大写字符， true = 包含， false = 不包含
     * @param number      是否包含数字，true = 包含， false = 不包含
     * @param specialChar 是否包含特殊字符，true = 包含， false = 不包含
     * @return 返回生成的随机字符串
     */
    public static String getRandString(int len, boolean lowerCase, boolean upperCase, boolean number,
                                       boolean specialChar) {
        String seed = "";
        if (lowerCase) {
            seed += "abcdefghijklmnopqrstuvwxyz";
        }

        if (upperCase) {
            seed += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        }

        // 重复一份数字，增加数字权重，大概与字符串相同
        if (number) {
            seed += "01234567890123456789";
        }

        if (specialChar) {
            seed += "`~!@#$%^&*()_+\\][{}|:\"';/.,<>?";
        }

        int max = seed.length();
        if (max == 0) return "";

        char[] buffer = new char[len];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = seed.charAt(rand(0, max));
        }
        return new String(buffer);
    }

    /**
     * 获取时间的时间轴表示
     *
     * @param date 给定的日期
     * @return 时间轴字符串
     */
    public static String getTimeLine(Date date) {
        long diff = diffTime(date, now());
        String suffix = diff > 0 ? "前" : "后";

        diff = Math.abs(diff);
        if (diff < MS_SECOND) {
            return "刚刚";
        } else if (diff < MS_MINUTE) {
            return diff + "秒" + suffix;
        } else if (diff < MS_HOUR) {
            return diff / MS_MINUTE + "分钟" + suffix;
        } else if (diff < MS_DAY) {
            return diff / MS_HOUR + "小时" + suffix;
        } else if (diff < MS_DAY * 7) {
            return diff / MS_DAY + "天" + suffix;
        } else if (diff < MS_DAY * 30) {
            return diff / (MS_DAY * 7) + "周" + suffix;
        } else if (diff < MS_DAY * 365) {
            return diff / (MS_DAY * 30) + "月" + suffix;
        } else {
            return format(date, FORMAT_DATE);
        }
    }

    /**
     * 在文件末尾添加内容
     *
     * @param fileName 待追加的文件名
     * @param content  追加的文本内容
     * @return 成功返回true，失败返回false
     */
    public static boolean appendFile(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 从文件名全路径里面，提取文件名部分，例如：
     * /abc/def/xyz.txt ==> xyz.txt
     *
     * @param fullPath 文件名完整路径
     * @return 文件名
     */
    public static String extractFileName(String fullPath) {
        return new File(fullPath).getName();
    }

    /**
     * 从流中读取所有内容，并返回String
     *
     * @param in 待读取的流
     * @return 返回流中所有数据的内容字符串
     * @throws IOException 读写异常
     */
    public static String streamToString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static String streamToString(Reader in) throws IOException {
        StringBuilder out = new StringBuilder();
        char[] b = new char[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    /**
     * 以同步模式向服务器发送一个HTTP GET请求
     *
     * @param uri 待请求的URL地址
     * @return 返回服务器的信息
     */
    public static String httpGet(String uri) {
        URL url;
        HttpURLConnection urlConnection = null;
        String result;
        try {
            url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            result = streamToString(in);
        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    /**
     * 上传文件到指定的URL地址，返回服务器返回的结果，同步模式
     * 不能再主线程中运行，如果有需要，请使用下面的方式调用
     * <pre>{@code
     * new Thread(new Runnable() {
     *     @Override
     *     public void run() {
     *         httpPostFile("http://www.baidu.com", "/path/to/save.html");
     *     }
     * }).start();
     * }</pre>
     *
     * @param uri           服务器URL地址，完整路径
     * @param fileName      本地文件名
     * @param secondTimeout 超时，单位秒
     * @return 返回服务器响应字符串
     */
    public static String httpPostFile(String uri, String fileName, final int secondTimeout) {
        final String BOUNDARY = "*****";
        final String TWO_HYPHENS = "--";
        final String LINE_END = "\r\n";
        final String HEAD_END = "\r\n\r\n";

        class InterruptThread implements Runnable {
            final Thread parent;
            final HttpURLConnection con;

            public InterruptThread(Thread parent, HttpURLConnection con) {
                this.parent = parent;
                this.con = con;
            }

            public void run() {
                try {
                    Thread.sleep(1000 * secondTimeout * 2L);
                    // 无论如何，在超时后，强行断开链接，防止吊死
                    con.disconnect();
                } catch (Exception e) {
                    // Nothing
                }
            }
        }

        String sName = extractFileName(fileName);
        try {
            URL url = new URL(uri);
            FileInputStream fis = new FileInputStream(fileName);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            new Thread(new InterruptThread(Thread.currentThread(), connection)).start();

            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(1000 * secondTimeout);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Close");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            connection.setRequestProperty("File", sName);

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            request.writeBytes("Content-Disposition: form-data; name=\"File\"; filename=\"" + sName + "\"" + HEAD_END);

            byte[] buf = new byte[4096];
            int len;
            while ((len = fis.read(buf)) != -1) {
                request.write(buf, 0, len);
            }
            request.writeBytes(LINE_END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
            request.flush();
            request.close();

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                StringBuilder out = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                connection.disconnect();
                return out.toString();
            } else {
                return connection.getResponseMessage();
            }
        } catch (SocketTimeoutException e) {
            return "Socket Timeout";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 把类似 12:34:56 的字符串转成 Time 输出，日期为当前日期
     * 用于保存时间戳时分秒，可用于数据交换，如Date
     *
     * @param s 时间字符串，例如: 12:45:30
     * @return 返回当前日期对应时间的 Date 变量
     */
    public static Date dateFromString(String s) {
        String[] my = s.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(my[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(my[1]));
        calendar.set(Calendar.SECOND, Integer.parseInt(my[2]));
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 按给定的启动时间（hh:mm:ss）设置定时任务，如果时间已过，则自动到下一个周期开始
     *
     * @param time       定时时刻，按hh:mm:ss 例如 12:20:35 表示12点20分35秒开始运行，以后每隔指定周期运行
     * @param periodMSec 周期间隔，单位毫秒
     * @param task       定时的任务
     * @return 返回定时器
     */
    public static Timer scheduleTask(String time, long periodMSec, TimerTask task) {
        Timer timer = new Timer();
        Date date = dateFromString(time);
        long begin = date.getTime();
        long now = new Date().getTime();

        if (now > begin) // 如果当前时间 > 定时开始的时刻，需要调整到下一次开始的时刻开始！
        {
            long diff = now - begin;
            date = addTime(date, periodMSec * roundUp(1.0 * diff / periodMSec));
        }
        timer.scheduleAtFixedRate(task, date, periodMSec);
        return timer;
    }

    /**
     * 从文件读取全部内容并返回字符串，默认UTF-8编码
     *
     * @param file    待读取的文件名
     * @param charset 字符编码，默认UTF-8
     * @return 返回文件全部内容
     * @throws IOException 读写失败抛异常
     */
    public static String readFile(String file, String charset) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[fileInputStream.available()];
        int length = fileInputStream.read(buffer);
        fileInputStream.close();
        return new String(buffer, 0, length, charset == null ? "UTF-8" : charset);
    }

    /**
     * 获取进程命令执行打印出来的信息
     *
     * @param command 待执行的命令
     * @return 返回命令的输出
     */
    public static List<String> exec(String command) {
        List<String> lists = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), Charset.forName("GBK"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                lists.add(line);
            }
            reader.close();
            inputStreamReader.close();
            process.destroy();
        } catch (Exception e) {
            lists.add(e.getMessage());
        }

        return lists;
    }

    /**
     * 字符串拼接
     *
     * @param separator 连接符
     * @param strArr    待拼接字符串
     * @return 返回连接后的字符串
     */
    public static String join(String separator, String... strArr) {
        Optional<String> optional = Arrays.stream(strArr).filter(Objects::nonNull).reduce((a, b) -> a + separator + b);
        return optional.orElse("");
    }

    /**
     * 字符串分隔 StringTokenizer效率是三种分隔方法中最快的
     *
     * @param str  待分隔字符串
     * @param sign 分隔符
     * @return 分隔后的数组
     */
    public static String[] split(String str, String sign) {
        if (str == null) {
            return new String[]{};
        }
        StringTokenizer token = new StringTokenizer(str, sign);
        String[] strArr = new String[token.countTokens()];

        int i = 0;
        while (token.hasMoreElements()) {
            strArr[i] = token.nextElement().toString();
            i++;
        }
        return strArr;
    }

    /**
     * 获取本机IP
     *
     * @return 本机IP，失败返回 ""
     */
    public static String getIP() {
        try {
            InetAddress inet = InetAddress.getLocalHost();
            return inet.getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 返回 application.properties 中的属性值
     *
     * @param key          属性名
     * @param defaultValue 找不到时的默认值
     * @return 属性值
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 返回 application.properties 中指定 key 的值，失败或不存在返回 defaultValue
     *
     * @param key   配置名称
     * @param value 配置值
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 正则表达式匹配支持
     *
     * @param regex 正则表达式
     * @param value 待匹配的字符串
     * @return 匹配返回 true，否则返回 false
     */
    public static boolean match(String regex, String value) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 把字符串转换 boolean, 支持 true, yes, t, y 1 等支付转换为true，其他情况转换为 false
     *
     * @param s 字符串
     * @return bool 值
     */
    public static boolean strToBoolean(String s) throws IllegalArgumentException {
        if (s != null) {
            s = s.trim().toLowerCase();
        }

        if (inList(new String[]{"true", "yes", "t", "y", "1"}, s)) return true;
        if (inList(new String[]{"false", "no", "f", "n", "0"}, s)) return false;
        throw new IllegalArgumentException("Bad boolean string");
    }

    /***
     * 给定字符串，返回合法文件名
     *
     * @param str 字符串
     * @return 合法文件名
     */
    public static String getValidFilename(String str) {
        // 文件名不能包含以下字符: \ / : * ? " < > |
        return str.replaceAll("\\|", "_")
                .replaceAll(">", "_")
                .replaceAll("<", "_")
                .replaceAll(":", "_")
                .replaceAll("\"", "_")
                .replaceAll("\\\\", "_")
                .replaceAll("\\?", "_")
                .replaceAll("/", "_")
                .replaceAll("\\*", "_");
    }

    /**
     * 根据输入数组，返回数组中，每个元素在所有数据中大小顺序的索引，重复元素返回的索引相同，索引为1开始
     * 例如
     * [200, 500, 300, 100, 10, 100] => [3, 5, 4, 2, 1, 2]
     *
     * @param array 升序数组
     * @return 返回数组中每个元素对应的大小顺序，从1开始
     */
    public static long[] getOrderIndexAscending(long[] array) {
        if (array == null || array.length == 0) return null;

        // 利用 TreeSet 去掉重复的数据
        TreeSet<Long> set = new TreeSet<>();
        for (long value : array) {
            set.add(value);
        }

        // 利用HashMap 保存每个值的在 TreeSet 中的顺序
        HashMap<Long, Long> map = new HashMap<>();
        long index = 0;
        for (Long value : set) {
            map.put(value, index++);
        }

        // 从 TreeSet 中返回每个值的对应的索引值
        long[] order = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            order[i] = map.get(array[i]) + 1;
        }
        return order;
    }

    /**
     * 根据值从数组中查找元素的下标，数组可以为无序数组
     *
     * @param array 元素数组
     * @param value 待查找的值
     * @return 返回索引顺序，找不到返回 -1
     */
    public static int search(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 二分法查找元素，数组必须为升序排列
     *
     * @param ascendArray 升序数组
     * @param key         数值
     * @return 找到返回索引，找不到返回 -1
     */
    public static int searchBin(int[] ascendArray, int key) {
        int left = 0;
        int right = ascendArray.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (ascendArray[mid] > key) {
                right = mid - 1;
            } else if (ascendArray[mid] < key) {
                left = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    /**
     * 二分法查找元素，数组必须为升序排列
     *
     * @param ascendArray 升序数组
     * @param key         数值
     * @return 找到返回索引，找不到返回 -1
     */
    public static int searchBin(long[] ascendArray, long key) {
        int left = 0;
        int right = ascendArray.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (ascendArray[mid] > key) {
                right = mid - 1;
            } else if (ascendArray[mid] < key) {
                left = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    /**
     * 从字符串中提取所有整数，支持正负符号 <br/>
     * <pre>{@code
     * getNumberFromString("a-100.01 divided by 5 is 20") => [-100, 1, 5, 20]
     * }</pre>
     *
     * @param s 待提取的字符串
     * @return 返回整数列表
     */
    public static List<Long> getNumberFromString(String s) {
        Pattern pattern = Pattern.compile("([+-]?\\d+)");
        Matcher matcher = pattern.matcher(s);

        ArrayList<Long> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(Long.parseLong(matcher.group(1)));
        }
        return list;
    }

    /**
     * 从字符串中提取所有整数和小数，支持正负符号 <br/>
     * <pre>{@code
     * getFloatFromString("a-100.01 divided by 5 is 20") => -100.01,5.0,20.0
     * }</pre>
     *
     * @param s 待提取的字符串
     * @return 返回整数列表
     */
    public static List<Double> getFloatFromString(String s) {
        Pattern pattern = Pattern.compile("([-+]?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(s);

        ArrayList<Double> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(Double.parseDouble(matcher.group(1)));
        }
        return list;
    }

    /**
     * 主机字节序转网络字节序
     *
     * @param x 带转换的数字
     * @return 返回4字节的byte数据
     */
    public static byte[] htonl(int x) {
        // 下面的方法可以达到同样的效果
        // return ByteBuffer.allocate(4).putInt(x).array();

        byte[] res = new byte[4];
        res[0] = (byte) ((x & 0xFF000000) >> 24);
        res[1] = (byte) ((x & 0x00FF0000) >> 16);
        res[2] = (byte) ((x & 0x0000FF00) >> 8);
        res[3] = (byte) (x & 0x000000FF);
        return res;
    }

    /**
     * 网络字节序转主机字节序
     *
     * @param b 只能传入4字节数组
     * @return 返回主机字节序整数
     */
    public static int ntohl(byte[] b) {
        return (b[3] & 0xFF)
                + ((b[2] & 0xFF) << 8)
                + ((b[1] & 0xFF) << 16)
                + ((b[0] & 0xFF) << 24);
    }

    /**
     * 压缩文件为.zip文件
     *
     * @param srcFile 待压缩的源文件
     * @param dstFile 压缩后的zip文件路径和文件名
     * @return 成功返回 true ，失败返回 false
     */
    public static boolean zipFile(String srcFile, String dstFile) {
        File file = new File(srcFile);
        if (!file.exists()) return false;

        try {
            file = new File(dstFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(file), new CRC32()));
            zipOutputStream.putNextEntry(new ZipEntry(extractFileName(srcFile))); // 写入文件名到输出的.zip中
            FileInputStream input = new FileInputStream(srcFile);
            byte[] buf = new byte[4096];

            int len;
            while ((len = input.read(buf)) != -1) {
                zipOutputStream.write(buf, 0, len);
            }

            zipOutputStream.flush();
            input.close();
            zipOutputStream.flush();
            zipOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 对数组中的元素去重
     *
     * @param array 输入数组
     * @param <T>   对象类型
     * @return 去重后的数据
     */
    public static <T> T[] unique(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        HashSet<T> set = new HashSet<>(Arrays.asList(array));

        // Java 不支持直接创建泛型对象数组: new T[]{} not allowed
        T[] ret = (T[]) Array.newInstance(array[0].getClass(), set.size());
        return set.toArray(ret);
    }

    /**
     * 对数组中的元素去重
     *
     * @param list 输入数组
     * @param <T>  对象类型
     * @return 去重后的数据
     */
    public static <T> List<T> unique(Collection<T> list) {
        if (list == null) return null;

        HashSet<T> set = new HashSet<>();
        set.addAll(list);

        List<T> ret = new ArrayList<>();
        ret.addAll(set);
        return ret;
    }

    /**
     * 对给定的代码进行性能评估，你也可以建立一个IDEA Live Template 模板来做
     * <pre>{@code
     * long benchStart = System.currentTimeMillis();
     * $SELECTION$
     * System.out.println("$name$ 运行时长: " + (System.currentTimeMillis() - benchStart) + " 毫秒");
     * }</pre>
     *
     * @param callable 要运行的代码
     * @param <T>      运行代码返回值类型
     * @return 返回运行代码的返回值
     * @throws Exception 抛出运行代码的异常
     */
    public static <T> T benchmark(Callable<T> callable) throws Exception {
        long startTime = System.currentTimeMillis();
        T retVal = callable.call();
        long endTime = System.currentTimeMillis();
        logInfo(String.format("%s completed %,d millis.", callable, endTime - startTime));
        return retVal;
    }

    /**
     * 生成密钥对象
     */
    private static SecretKey aesGenerateKey(String password) throws Exception {
        // 指定的RNG算法名称, 创建安全随机数生成器
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        //设置加密用的种子，密钥
        random.setSeed(password.getBytes());
        // 创建AES算法生成器
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        // 初始化算法生成器
        gen.init(128, random);
        // 生成 AES密钥对象
        return gen.generateKey();
        // 也可以直接创建密钥对象: return new SecretKeySpec(key, ALGORITHM);
        // return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * AES 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return 加密后的HEX字符串，失败返回 null
     */
    public static String aesEncrypt(String content, String password) {
        if (content == null || password == null) return null;

        try {
            SecretKey key = aesGenerateKey(password);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return bin2hex(result, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES 解密
     *
     * @param content  待解密内容，为HEX字符串
     * @param password 解密密钥
     * @return 解密后的字符串，失败返回 null
     */
    public static String aesDecrypt(String content, String password) {
        if (content == null || password == null) return null;

        try {
            SecretKey key = aesGenerateKey(password);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(hex2bin(content));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 分割字符串，支持特殊转义，例如 "" 号内的不分割
     * <p>
     * 例如: "123,456,"abc,def",xyz" => [123, 456, "abc,def", xyz]
     *
     * @param source     源字符串
     * @param separator  分隔符
     * @param escapeChar 转义字符，例如 "
     * @return 返回分割后的数组
     */
    public static String[] split(String source, String separator, char escapeChar) {
        // 下面代码，可以在逗号 , 分割时，把引号内的逗号忽略，不进行分割
        // source.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
        boolean isSpecialChar = ".?![]{}()<>*+-=^$|".indexOf(escapeChar) >= 0;
        String escape = isSpecialChar ? "\\" + escapeChar : String.valueOf(escapeChar);
        String reg = String.format("%s(?=([^%s]*%s[^%s]*%s)*[^%s]*$)", separator, escape, escape, escape, escape, escape);
        return source.split(reg);
    }

    /**
     * 根据变量和字段名，返回对象实例指定变量对应的对象数据，无论是否是私有还是公开
     *
     * @param instance  包含字段的对象实例
     * @param fieldName 变量的名字
     * @return 返回实例的给定的fieldName变量的值，出错返回 null
     */
    public static <T> T getField(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 给指定对象设定实例的变量值
     *
     * @param instance  对象实例
     * @param fieldName 字段名称
     * @param object    要给变量赋值的新值
     * @return 成功设置返回 true，否则返回 false
     */
    public static boolean setField(Object instance, String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, object);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取对象实例的方法属性
     *
     * @param instance   对象实例
     * @param methodName 方法名称
     * @return 返回对象的类方法属性，失败返回 null
     */
    public static Method getMethod(Object instance, String methodName, Class<?>... params) {
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 调用对象的方法，无论是否是私有还是公开的
     * <p>
     * 用法:<pre>{@code
     * MyClass myClass = new MyClass();
     * invoke(myClass, "myMethod", new class[]{String.class, String.class, long.class}, "aaaa", "bbb", 1L);
     * }</pre>
     *
     * @param instance   对象实例
     * @param methodName 方法名称
     * @param params     参数列表
     * @return 返回方法调用后的返回值
     * @throws NoSuchMethodException     异常
     * @throws InvocationTargetException 异常
     * @throws IllegalAccessException    异常
     */
    public static <T> T invoke(Object instance, String methodName, Class<?>[] clazz, Object... params) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getDeclaredMethod(methodName, clazz);
        method.setAccessible(true);

        return (T) method.invoke(instance, params);
    }

    /**
     * 史上最强 C 语言风格 scanf 函数 <br/>
     * The best and the powerful scanf function like C-Style ever.
     *
     * <p>
     * 使用示例: <pre>{@code
     *   List<Object> out = new ArrayList<>();
     *   if (scanf("%c %s %d %l abc", "A -123 -456 9876543210 def", out)) {
     *      // 处理 out.get(0); out.get(1) ...
     *   }
     *   out.clear();
     *   if (scanf("%c%c%c %s %d %f %g %G %l abcd", "AB中 123 456 7.89 -10.123 111.222 -9876543210 abcd", out)) {
     *      // 处理 out.get(0); out.get(1);
     *   }
     *
     *   if (scanf("%C %s%S %F %L ab中cd %o %O %x %x", "A xyz中文def\t\t 456 12345678 ab中cd 1234567 054321 1234 0x1a2b3c4d", out)) {
     *      // 处理 out.get(0); out.get(1);
     *   }
     *
     *   if (scanf("% . * / \\ %d", "% . * / \\ 123", out)) {
     *      // 处理 out.get(0); out.get(1);
     *   }
     * }</pre>
     * 支持的格式
     * <ul>
     *     <li>%c: 任意字符，除换行符外</li>
     *     <li>%C: 字母、数字、下划线</li>
     *     <li>%d: 正负整数，支持 + - 符号</li>
     *     <li>%D: 正整数</li>
     *     <li>%f: 正负浮点数</li>
     *     <li>%F: 正浮点数</li>
     *     <li>%g: 双精度 double，可支持正负</li>
     *     <li>%G: 双精度整数 double</li>
     *     <li>%l: 长整型，可支持正负</li>
     *     <li>%L: 长整型正整数</li>
     *     <li>%s: 非空白字符串</li>
     *     <li>%S: 空白字符串，如制表符，空格，回车，换行等</li>
     *     <li>%o: 可选前导0八进制数值，返回数据为long</li>
     *     <li>%O: 前导0八进制数值，返回数据为long</li>
     *     <li>%x: 可选前导0x十六进制数值，返回数据为long</li>
     *     <li>%X: 前导0x十六进制数值，返回数据为long</li>
     * </ul>
     *
     * @param format 格式字符串
     * @param source 源字符串
     * @param out    输出结果列表，与占位符一一对应，list必须为空队列
     * @return 成功匹配并解释数据返回 true，否则返回 false，数据解释错误抛异常
     */
    public static boolean scanf(String format, String source, List<Object> out) {
        if (out == null || out.size() > 0) {
            throw new InvalidParameterException("Out can't be null and must be empty");
        }

        List<Character> fmt = new ArrayList<>();
        for (int i = 1; i < format.length(); i++) {
            if (format.charAt(i - 1) != '%') continue;
            char ch = format.charAt(i);
            if ("cdfglsoxCDFGLSOX".indexOf(ch) == -1) continue;

            fmt.add(ch);
        }

        String s = escapeRegexp(format);
        for (char c : fmt) {
            if (c == 'c') { // 任意字符，除换行符外
                s = s.replaceFirst("%c", "(.)");
            } else if (c == 'C') { // 字母数字下划线
                s = s.replaceFirst("%C", "(\\\\w+)");
            } else if (c == 'd') { // 正负整数
                s = s.replaceFirst("%d", "([+-]?\\\\d+)");
            } else if (c == 'D') { // 正整数
                s = s.replaceFirst("%D", "(\\\\d+)");
            } else if (c == 'f') { // 浮点数，带符号
                s = s.replaceFirst("%f", "([-+]?\\\\d+(?:\\\\.\\\\d+)?)");
            } else if (c == 'F') { // 正浮点数
                s = s.replaceFirst("%F", "(\\\\d+(?:\\\\.\\\\d+)?)");
            } else if (c == 'g') { // 双精度浮点数，支持正负
                s = s.replaceFirst("%g", "([-+]?\\\\d+(?:\\\\.\\\\d+)?)");
            } else if (c == 'G') { // 双精度正数
                s = s.replaceFirst("%G", "(\\\\d+(?:\\\\.\\\\d+)?)");
            } else if (c == 'l') { // 正负长整形
                s = s.replaceFirst("%l", "([+-]?\\\\d+)");
            } else if (c == 'L') { // 长整型正数
                s = s.replaceFirst("%L", "(\\\\d+)");
            } else if (c == 's') { // 非空白符
                s = s.replaceFirst("%s", "(\\\\S+)");
            } else if (c == 'S') { // 匹配空白符
                s = s.replaceFirst("%S", "(\\\\s+)");
            } else if (c == 'o') { // 可选带 0 前缀八进制
                s = s.replaceFirst("%o", "([0-7]*)");
            } else if (c == 'O') { // 必带 0 前缀八进制
                s = s.replaceFirst("%O", "(0[0-7]*)");
            } else if (c == 'x') { // 可选带 0x 前缀十六进制
                s = s.replaceFirst("%x", "(0[xX]?[0-9A-Fa-f]+|[0-9A-Fa-f]+)");
            } else if (c == 'X') { // 必带 0x 前缀十六进制
                s = s.replaceFirst("%X", "(0[xX]?[0-9A-Fa-f]+)");
            }
        }
        Pattern pattern = Pattern.compile(s);
        Matcher matcher = pattern.matcher(source);
        if (!matcher.matches()) return false;

        for (int i = 0; i < matcher.groupCount(); i++) {
            char c = fmt.get(i);
            String value = matcher.group(i + 1);
            if (c == 'c' || c == 'C') { // 字符
                out.add(value.charAt(0));
            } else if (c == 'd' || c == 'D') { // 整数
                out.add(Integer.valueOf(value));
            } else if (c == 'f' || c == 'F') { // 浮点数
                out.add(Float.valueOf(value));
            } else if (c == 'g' || c == 'G') { // 双精度浮点数
                out.add(Double.valueOf(value));
            } else if (c == 'l' || c == 'L') { // 长整形
                out.add(Long.valueOf(value));
            } else if (c == 's' || c == 'S') { // 字符串
                out.add(value);
            } else if (c == 'o' || c == 'O') { // 八进制
                out.add(Long.parseLong(value, 8));
            } else if (c == 'x' || c == 'X') { // 十六进制
                value = value.replace("0x", "").replace("0X", "");
                out.add(Long.parseLong(value, 16));
            }
        }
        return true;
    }

    /**
     * 通配符比较，支持 * 和 ?
     *
     * @param pattern 通配符模板
     * @param str     带验证的字符串
     * @return 符合通配符返回 true，否则返回 false
     */
    public static boolean matchWildcard(String pattern, String str) {
        int s = 0, p = 0, match = 0, startIdx = -1;
        while (s < str.length()) {

            if (p < pattern.length() && (pattern.charAt(p) == '?' || str.charAt(s) == pattern.charAt(p))) {
                //匹配到了
                p++;
                s++;
            } else if (p < pattern.length() && pattern.charAt(p) == '*') {
                //遇到通配符 * 了,记录下位置,规则字符串+1,定位到非通配字符串

                startIdx = p;
                match = s;
                p++;
            } else if (startIdx != -1) {
                p = startIdx + 1;
                match++;
                s = match;
            } else {
                return false;
            }
        }
        //当s的每一个字段都匹配成功以后,判断p剩下的串,是*则放行
        while ((p < pattern.length() && pattern.charAt(p) == '*')) {
            p++;
        }

        //检测到最后就匹配成功
        return p == pattern.length();
    }

    /**
     * 大数值字符串数字相乘，支持负数符号
     * <p>
     * 使用示例:
     * <pre>{@code
     * multiply("123456789", "987654321");
     * multiply("123456789", "-987654321");
     * }</pre>
     *
     * @param n1 乘数1，必须为正常整数形式
     * @param n2 乘数2，必须为正常整数形式
     * @return 结果
     */
    public static String multiply(String n1, String n2) {
        // 本算法可以用 BigDecimal 来实现
        // return new BigDecimal(n1).add(new BigDecimal(n2);

        String num1 = "-".equals(left(n1, 1)) ? n1.substring(1) : n1;
        String num2 = "-".equals(left(n2, 1)) ? n2.substring(1) : n2;
        int m = num1.length(), n = num2.length();
        int[] pos = new int[m + n];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                int mul = (num1.charAt(i) - '0') * (num2.charAt(j) - '0');
                int p1 = i + j, p2 = i + j + 1;
                int sum = mul + pos[p2];

                pos[p1] += sum / 10;
                pos[p2] = sum % 10;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int p : pos) {
            if (!(sb.length() == 0 && p == 0)) sb.append(p);
        }
        String ret = sb.length() == 0 ? "0" : sb.toString();
        int sign1 = "-".equals(left(n1, 1)) ? -1 : 1;
        int sign2 = "-".equals(left(n2, 1)) ? -1 : 1;

        return sign1 * sign2 == 1 ? ret : ("-" + ret);
    }

    /**
     * 两个大数相除
     *
     * @param n1 第一个数字
     * @param n2 第二个数字
     * @return 计算结果
     */
    public static String divide(String n1, String n2) {
        BigDecimal num1 = new BigDecimal(n1);
        BigDecimal num2 = new BigDecimal(n2);
        return num1.divide(num2).toString();
    }

    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     */
    public static String desEncrypt(String data, String key) {
        if (data == null) return null;

        try {
            // 指定的RNG算法名称, 创建安全随机数生成器
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            //设置加密用的种子，密钥
            random.setSeed(key.getBytes());
            // 创建AES算法生成器
            KeyGenerator gen = KeyGenerator.getInstance("DES");
            // 初始化算法生成器
            gen.init(56, random);
            Key secretKey = gen.generateKey();

            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //用密匙初始化Cipher对象,ENCRYPT_MODE用于将 Cipher 初始化为加密模式的常量
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);

            return bin2hex(cipher.doFinal(data.getBytes()), false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DES算法，解密
     *
     * @param data 待解密字符串
     * @param key  解密私钥，长度不能够小于8位
     * @return 解密后的字节数组
     */
    public static String desDecrypt(String data, String key) {
        if (data == null) return null;
        try {
            // 指定的RNG算法名称, 创建安全随机数生成器
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            //设置加密用的种子，密钥
            random.setSeed(key.getBytes());
            // 创建AES算法生成器
            KeyGenerator gen = KeyGenerator.getInstance("DES");
            // 初始化算法生成器
            gen.init(56, random);
            Key secretKey = gen.generateKey();

            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //用密匙初始化Cipher对象,ENCRYPT_MODE用于将 Cipher 初始化为加密模式的常量
            cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
            return new String(cipher.doFinal(hex2bin(data)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换枚举变量为字符串
     *
     * @param t   待转换的枚举变量
     * @param <T> 枚举类型
     * @return 字符串
     */
    public static <T extends Enum<T>> String enumToString(T t) {
        return t.toString();
    }

    /**
     * 获取枚举类型所有的枚举值
     * <p>
     * 使用方法：
     * <pre>{@code
     *     enum Currencies {USD, YEN, EUR, INR}
     *     String[] ret = enumToStringValues(Currencies.class);
     * }</pre>
     *
     * @param enumClass 枚举类
     * @param <T>       泛型变量
     * @return 返回枚举类型的所有枚举值字符串数组
     */
    public static <T extends Enum<T>> String[] enumToStringValues(Class<T> enumClass) {
        List<String> list = new ArrayList<>();
        for (T t : enumClass.getEnumConstants()) {
            list.add(t.toString());
        }
        return list.toArray(new String[0]);
    }

    /**
     * 通用泛型方法把字符串转任意枚举类型
     * <p>
     * 使用示例：
     * <pre>{@code
     *     enum Currencies {USD, YEN, EUR, INR}
     *
     *     Currencies cur = stringToEnum("inr", Currencies.class);
     * }</pre>
     *
     * @param str 待转换的字符串，可不区分大小写
     * @param t   枚举类型类
     * @param <T> 泛型对象
     * @return 成功返回泛型对象对应的变量
     * @throws UnsupportedOperationException 字符串不对抛出异常
     */
    public static <T extends Enum<T>> Enum<T> stringToEnum(String str, Class<T> t) throws
            UnsupportedOperationException {
        // 不区分大小写，也可以直接用下面的方法
//         return Enum.valueOf(t, str);

        Enum<T>[] all = t.getEnumConstants();
        for (Enum<T> one : all) {
            if (equal(one.toString(), str, false)) {
                return one;
            }
        }
        throw new UnsupportedOperationException("Wrong enum value");
    }

    /**
     * 字符串左填充到指定长度
     *
     * @param source      源字符串，可为 "" 或 null
     * @param pendingChar 填充字符
     * @param maxLength   字符串最大长度
     * @return 若源字符串长度>maxLength返回源字符串，否则返回填充后的字符串
     */
    public static String padLeft(String source, char pendingChar, int maxLength) {
        // 如果 source 为空，就是需要填充最大长度
        // 如果 source 长度大于 maxLength，那么无需填充，返回源字符串
        int n = source == null ? maxLength : Math.max(maxLength - source.length(), 0);

        return String.valueOf(pendingChar).repeat(n) + toStr(source);
    }

    /**
     * 字符串右填充到指定长度
     *
     * @param source      源字符串，可为 "" 或 null
     * @param pendingChar 填充字符
     * @param maxLength   字符串最大长度
     * @return 若源字符串长度>maxLength返回源字符串，否则返回填充后的字符串
     */
    public static String padRight(String source, char pendingChar, int maxLength) {
        // 如果 source 为空，就是需要填充最大长度
        // 如果 source 长度大于 maxLength，那么无需填充，返回源字符串
        int n = source == null ? maxLength : Math.max(maxLength - source.length(), 0);

        return toStr(source) + String.valueOf(pendingChar).repeat(n);
    }

    /**
     * 将int数字转换成ipv4地址
     *
     * @param ip 整数 IPV4 地址
     * @return IP 地址，例如 192.168.1.1
     */
    public static String intToIp(long ip) {
        String[] str = new String[4];
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Long temp = ip & 255;
            ip = ip >> 8;
            str[i] = String.valueOf(temp);
        }
        for (int i = str.length - 1; i >= 0; i--) {
            result.add(str[i]);
        }
        return String.join(".", result);
    }

    /**
     * IPv4地址转换为int类型数字
     * <p>
     * IP地址换换成数字地址的方法如下：<br/>
     * 例子：219.239.110.138 <br/>
     * 具体计算过程如下： <br/>
     * 219*2563+ 239*2562+110*2561+138*2560=3689901706 <br/>
     * 219.239.110.138-->3689901706
     *
     * @param ip IP字符串，例如 192.168.1.1
     * @return 正整数IP
     */
    public static long ipToInt(String ip) {
        String[] ipStr = ip.split("\\.");
        Long result = 0L;
        int j;
        int i;
        for (i = ipStr.length - 1, j = 0; i >= 0; i--, j++) {
            long temp = Long.parseLong(ipStr[i]);
            temp = temp << (8 * j);
            result = result | temp;
        }
        System.out.println(result);
        return result;
    }

    /**
     * 判断是否为ipv4地址
     *
     * @param ipv4Addr IPV4地址，例如 192.168.1.1
     * @return 符合IP格式返回 true，否则返回 false
     */
    public static boolean isIPv4Address(String ipv4Addr) {
        String lower = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])"; // 0-255的数字
        String regex = lower + "(\\." + lower + "){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ipv4Addr);
        return matcher.matches();
    }

    /**
     * 求最大值
     *
     * @param values 值列表
     * @return 返回多个值中最大的值
     */
    public static double maxValue(double... values) {
        double max = Double.MIN_VALUE;
        for (double value : values) max = Math.max(max, value);
        return max;
    }

    /**
     * 求最大值
     *
     * @param values 值列表
     * @return 返回多个值中最大的值
     */
    public static long maxValue(long... values) {
        long max = Long.MIN_VALUE;
        for (long value : values) max = Math.max(max, value);
        return max;
    }

    /**
     * 求最小值
     *
     * @param values 值列表
     * @return 返回多个值中最小的值
     */
    public static double minValue(double... values) {
        double min = Double.MAX_VALUE;
        for (double value : values) min = Math.min(min, value);
        return min;
    }

    /**
     * 求最小值
     *
     * @param values 值列表
     * @return 返回多个值中最小的值
     */
    public static long minValue(long... values) {
        long min = Long.MAX_VALUE;
        for (long value : values) min = Math.min(min, value);
        return min;
    }


    /**
     * 大写数字
     */
    private static final String[] NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    /**
     * 整数部分的单位
     */
    private static final String[] IUNIT = {"元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟"};
    /**
     * 小数部分的单位
     */
    private static final String[] DUNIT = {"角", "分", "厘"};

    /**
     * 得到大写金额
     *
     * @param str 阿拉伯数字金额
     * @return 转换后的中文大写金额字符串
     */
    public static String toChinese(String str) {
        str = str.replaceAll(",", "");      // 去掉","
        String integerStr;                                    // 整数部分数字
        String decimalStr;                                    // 小数部分数字

        // 初始化：分离整数部分和小数部分
        if (str.indexOf(".") > 0) {
            integerStr = str.substring(0, str.indexOf("."));
            decimalStr = str.substring(str.indexOf(".") + 1);
        } else if (str.indexOf(".") == 0) {
            integerStr = "";
            decimalStr = str.substring(1);
        } else {
            integerStr = str;
            decimalStr = "";
        }

        // integerStr去掉首0，不必去掉decimalStr的尾0(超出部分舍去)
        if (!integerStr.equals("")) {
            integerStr = Long.toString(Long.parseLong(integerStr));
            if (integerStr.equals("0")) {
                integerStr = "";
            }
        }

        // overflow超出处理能力，直接返回
        if (integerStr.length() > IUNIT.length) {
            throw new UnsupportedOperationException("超出处理能力");
        }
        int[] integers = toArray(integerStr);   // 整数部分数字
        boolean isMust5 = isMust5(integerStr);  // 设置万单位
        int[] decimals = toArray(decimalStr);   // 小数部分数字
        return getChineseInteger(integers, isMust5) + getChineseDecimal(decimals);
    }

    /**
     * 整数部分和小数部分转换为数组，从高位至低位
     */
    private static int[] toArray(String number) {
        int[] array = new int[number.length()];
        for (int i = 0; i < number.length(); i++) {
            array[i] = Integer.parseInt(number.substring(i, i + 1));
        }
        return array;
    }

    /**
     * 得到中文金额的整数部分。
     */
    private static String getChineseInteger(int[] integers, boolean isMust5) {
        StringBuilder chineseInteger = new StringBuilder();
        int length = integers.length;
        for (int i = 0; i < length; i++) {

            // 0出现在关键位置：1234(万)5678(亿)9012(万)3456(元)
            // 特殊情况：10(拾元、壹拾元、壹拾万元、拾万元)
            String key = "";
            if (integers[i] == 0) {
                if ((length - i) == 13)         // 万(亿)(必填)
                    key = IUNIT[4];
                else if ((length - i) == 9)     // 亿(必填)
                    key = IUNIT[8];
                else if ((length - i) == 5 && isMust5)  // 万(不必填)
                    key = IUNIT[4];
                else if ((length - i) == 1)             // 元(必填)
                    key = IUNIT[0];

                // 0遇非0时补零，不包含最后一位
                if ((length - i) > 1 && integers[i + 1] != 0)
                    key += NUMBERS[0];
            }
            chineseInteger.append(integers[i] == 0 ? key : (NUMBERS[integers[i]] + IUNIT[length - i - 1]));
        }
        return chineseInteger.toString();
    }

    /**
     * 得到中文金额的小数部分。
     */
    private static String getChineseDecimal(int[] decimals) {
        StringBuilder chineseDecimal = new StringBuilder();
        for (int i = 0; i < decimals.length; i++) {
            // 舍去3位小数之后的
            if (i == 3)
                break;
            chineseDecimal.append(decimals[i] == 0 ? "" : (NUMBERS[decimals[i]] + DUNIT[i]));
        }
        return chineseDecimal.toString();
    }

    /**
     * 判断第5位数字的单位"万"是否应加。
     */
    private static boolean isMust5(String integerStr) {
        int length = integerStr.length();
        if (length > 4) {
            String subInteger;
            if (length > 8) {
                // 取得从低位数，第5到第8位的字串
                subInteger = integerStr.substring(length - 8, length - 4);
            } else {
                subInteger = integerStr.substring(0, length - 4);
            }
            return Integer.parseInt(subInteger) > 0;
        } else {
            return false;
        }
    }

}
