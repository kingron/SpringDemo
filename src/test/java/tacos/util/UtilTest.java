package tacos.util;

import mockit.Mock;
import mockit.MockUp;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import static tacos.util.Util.*;

public class UtilTest {

    @Test
    public void testDummy() throws IOException {
        System.out.println("每周有 " + MS_WEEK + " ms");
        System.out.println("当前时间: " + format(now(), FORMAT_SHORT_TIME));
        Properties ret = loadProperties("not exist");
        assertEquals(0, ret.size());
        assertFalse(isFloat("0.2.5"));
        assertFalse(isFloat("0.2ab"));
        assertTrue(isFloat("2.5"));
        assertTrue(isFloat("123."));
        assertTrue(isFloat("123"));
        System.out.println(getFilename(".tmp"));
        System.out.println(currentTime());
        assertEquals(12, getDay(date(2022, 10, 12)));
        Object[] obj = scanf("%d %d %d %f", "1 2 3 4.5");
        assertEquals(1, obj[0]);
        assertEquals(4.5, obj[3]);

        Map<String, Integer> map = new HashMap<>();
        map.put("key", 123);
        String filename = getTempFilename("dummy", ".bin");
        saveObject(map, filename);
        Map<String, Integer> readObj = loadObject(filename);
        assertEquals(map.get("key"), readObj.get("key"));
        System.out.println((appendFile(filename, "Come on!")));
        zipFile(filename, filename + ".zip");
        System.out.println(filename);
        System.out.println(httpGet("https://www.baidu.com"));
        System.out.println(httpPostFile("https://www.baidu.com/", filename, 20));

        System.out.println(readFile(filename, "utf-8"));
        System.out.println(getIP());
        System.out.println(getProperty("server.port", "8080"));
        setProperty("server.port", "1234");
        assertTrue(match("\\d+", "1234"));

        assertTrue(strToBoolean("true"));
        assertFalse(strToBoolean("f"));
        assertEquals(2, searchBin(new int[]{100, 200, 300, 400, 400}, 300));
        assertEquals(2, searchBin(new long[]{100, 200, 300, 400, 400}, 300));

        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("abc");
        list.add("abc");
        list.add("123");
        list.add("def");
        list.add("");
        list.add("");
        list.add(null);
        list.add(null);
        assertEquals(",,123,abc,def", join(unique(list), ","));

        scheduleTask("12:00:00", MS_MINUTE, new TimerTask() {
            @Override
            public void run() {
                System.out.println(currentTime());
            }
        });

        assertTrue(isIPv4Address("192.168.1.1"));
        assertFalse(isIPv4Address("256.190.1.1"));

        assertEquals("壹元贰角叁分", toChinese("1.23"));
        assertEquals("壹仟贰佰叁拾肆万伍仟陆佰柒拾捌亿玖仟零壹拾贰万叁仟肆佰伍拾陆元壹角贰分叁厘", toChinese("1234567890123456.123"));
        assertEquals("柒分玖厘", toChinese("0.0798"));
        assertEquals("壹仟万零壹仟元玖分", toChinese("10,001,000.09"));
        assertEquals("壹元壹角柒厘", toChinese("01.107700"));

        assertEquals(-100, minValue(200, -50, 100, -100));
        assertEquals(200, maxValue(200L, -50L, 100L, -100L));
        assertTrue(equal(-100L, minValue(200.0, -50, 100, -100)));
        assertTrue(equal(200, maxValue(200.0, -50, 100, -100)));

        // 这里演示如何使用 JMock 来 mock 需要的类方法
        MockUp<BigDecimal> mock = new MockUp<BigDecimal>() {
            @Mock
            public BigDecimal divide(BigDecimal divisor) {
                return BigDecimal.ONE;
            }
        };
        assertEquals("1", divide("123333", "123"));
    }

    @Test
    public void testDigest() {
        assertEquals("c4ca4238a0b923820dcc509a6f75849b", md5("1"));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5(""));
        assertEquals("", md5(null));

        assertEquals("9ce3bd4224c8c1780db56b4125ecf3f24bf748b7", sha1("OK"));

        String text = "I'm OK";
        String key = "very good";
        String encrypt = aesEncrypt(text, key);
        String decrypt = aesDecrypt(encrypt, key);
        assertEquals(text, decrypt);
    }

    @Test
    public void testLeft() {
        assertEquals("abc+def", left("abc+def", "="));
        assertEquals("abc", left("abc=def", "="));
        assertEquals("abc", left("abc=def", 3));
        assertEquals("abc=def", left("abc=def", 8));
        assertEquals("", left("abc=def", 0));
        assertEquals("a", left("a", 1));
    }

    @Test
    public void testRight() {
        assertEquals("abc+def", right("abc+def", "="));
        assertEquals("def", right("abc=def", "="));
        assertEquals("def", right("abc=def", 3));
        assertEquals("abc=def", right("abc=def", 8));
        assertEquals("", right("abc=def", 0));
        assertEquals("f", right("abc=def", 1));
    }

    @Test
    public void testInList() {
        assertFalse(inList(new String[]{"abc", "def", "123"}, null));
        assertTrue(inList(new String[]{"abc", "def", "123", null}, null));
        assertFalse(inList(new String[]{"abc", "def", "123"}, "xyz"));

        assertTrue(inList(new int[]{1, 2, 3}, 1));
        assertFalse(inList(new int[]{1, 2, 3}, 0));
        assertFalse(inList(new long[]{1, 2, 3}, 4));
        assertTrue(inList(new long[]{1, 2, 3}, 1));

        assertEquals(1, search(2L, 1L, 2L, 3L));
        assertTrue(among(2L, 1L, 2L, 3L));
        assertFalse(among(20L, 1L, 2L, 3L));
    }

    @Test
    public void testisNumeric() {
        assertTrue(isNumeric("12345"));
        assertTrue(isNumeric("-12345"));
        assertTrue(isNumeric("+12345"));
        assertFalse(isNumeric("a+12345"));
        assertFalse(isNumeric("a12345"));
        assertFalse(isNumeric("--a12345"));
        assertFalse(isNumeric("+++++a12345"));
        assertFalse(isNumeric("1245aaa"));
        assertFalse(isNumeric("aaa1245"));
        assertFalse(isNumeric("aaa0000"));
    }

    @Test
    public void testDownloadFile() {
        String s = getTempFilename("util_", ".html");
        downloadFile("https://www.baidu.com", s);
        assertTrue(new File(s).exists());
    }

    @Test
    public void testToday() {
        logInfo("今天是: " + today());
        logWarn("现在是: " + now());
        logWarn("现在是: " + now(FORMAT_FULL_DATETIME));
    }

    @Test
    public void testSameDay() {
        assertFalse(sameDay(null, now()));
        assertFalse(sameDay(now(), null));
        assertFalse(sameDay(now(), addTime(now(), MS_DAY * 5)));

        assertTrue(sameDay(null, null));
        assertTrue(sameDay(now(), today()));

        Date date = now();
        assertTrue(sameDay(date, date));
    }

    @Test
    public void testDiffTime() {
        Date now = now();
        assertEquals(1000, diffTime(now, addSecond(now, 1)));
        assertEquals(0, diffTime(today(), today()));

        assertEquals(MS_MINUTE, diffTime(today(), addTime(today(), MS_MINUTE)));
    }

    @Test
    public void testBeginOfDay() {
        assertEquals(today(), beginDay(now()));
    }

    @Test
    public void testUnixToDate() throws ParseException {
        Date date = unixToDate(0);
        Date date1 = Util.date("1970-01-01 08:00:00", FORMAT_DATETIME);
        Date dateUTC = Util.dateUTC("1970-01-01 00:00:00", FORMAT_DATETIME);
        assertTrue(sameDate(date, date1));
        assertTrue(sameDate(date1, dateUTC));
    }

    @Test
    public void testAddTime() throws ParseException {
        Date date = Util.date("2010-10-20 12:34:56", FORMAT_DATETIME);
        Date before = addTime(date, -5 * MS_HOUR);
        assertEquals(5, diffHour(before, date));
    }

    @Test
    public void testDate() throws ParseException {
        Date date = date(1970, 1, 1, 8, 0, 0, 0);
        Date dateUTC = dateUTC(1970, 1, 1, 0, 0, 0, 0);
        Date date1 = unixToDate(0);
        Date date2 = date("1970-1-1 8:0:0.0");
        Date date3 = date("1970-1-1 8:0:0", FORMAT_DATETIME);
        assertTrue(sameDate(date, date1));
        assertTrue(sameDate(date, dateUTC));
        assertTrue(sameDate(date, date2));
        assertTrue(sameDate(date, date3));
    }

    @Test
    public void testEndMonth() throws ParseException {
        Date date = date("2022-10-05 12:34:56.0");
        date = endMonth(date);
        assertEquals(date, date(2022, 10, 31, 23, 59, 59, 999));

        date = date("2000-2-1 0:0:0.0");
        date = endMonth(date);
        assertEquals(date, date(2000, 2, 29, 23, 59, 59, 999));

        date = date("2022-10-31 23:59:59.999");
        date = endMonth(date);
        assertEquals(date, date(2022, 10, 31, 23, 59, 59, 999));

        date = date("2022-9-3 0:59:59.999");
        date = endMonth(date);
        assertEquals(date, date(2022, 9, 30, 23, 59, 59, 999));
    }

    @Test
    public void testBeginMonth() throws ParseException {
        assertEquals(beginMonth(date("2020-10-3 1:2:3.0")), date("2020-10-1", FORMAT_DATE));
        assertEquals(beginMonth(date("2020-2-1 23:59:59.999")), date("2020-2-1", FORMAT_DATE));
        assertEquals(beginMonth(date("2022-12-31 23:59:59.999")), date("2022-12-1", FORMAT_DATE));
    }

    @Test
    public void testIsLeapYear() throws ParseException {
        assertTrue(isLeapYear(2000));
        assertTrue(isLeapYear(1996));
        assertTrue(isLeapYear(2020));

        assertFalse(isLeapYear(2021));
        assertFalse(isLeapYear(date("2001-10-1 0:0:0.0")));
    }

    @Test
    public void testSameDate() {
        assertFalse(sameDate(null, today()));
        assertFalse(sameDate(today(), null));
        assertFalse(sameDate(unixToDate(0), today()));

        assertTrue(sameDate(null, null));
        assertTrue(sameDate(now(), now()));
        assertTrue(sameDate(today(), today()));
        assertTrue(sameDate(unixToDate(0), dateUTC(1970, 1, 1, 0, 0, 0, 0)));
    }

    @Test
    public void testSameTime() {
        Date date = date(1970, 1, 1, 8, 0, 0, 0);
        Date dateUTC = dateUTC(1970, 1, 1, 0, 0, 0, 0);
        Date date1 = unixToDate(0);
        assertTrue(sameTime(date, date1));
        assertTrue(sameTime(date, dateUTC));
        assertTrue(sameTime(date(2022, 10, 30, 4, 46, 0, 0),
                date(2021, 10, 30, 4, 46, 0, 0)));
        assertTrue(sameTime(date(2022, 10, 30, 8, 1, 0, 0),
                dateUTC(2021, 10, 30, 0, 1, 0, 0)));

        assertTrue(sameTime(null, null));
        assertFalse(sameTime(now(), date));
        assertFalse(sameTime(now(), null));
        assertFalse(sameTime(null, today()));
    }

    @Test
    public void testEqualString() {
        assertTrue(equal(null, null));
        assertTrue(equal("", ""));
        assertTrue(equal("111", "111"));

        assertFalse(equal("111", "1111"));
        assertFalse(equal("111", "def"));
        assertFalse(equal(null, "def"));
        assertFalse(equal("", "def"));
        assertFalse(equal("null", null));
    }

    @Test
    public void testEqualFloat() {
        assertTrue(equal(1.0f - 0.9f, 0.9f - 0.8f));
        assertTrue(equal(1.0d - 0.9d, 0.9d - 0.8d));

        assertFalse(equal(1.0f - 0.9f, 0.9f - 0.7f));
        assertFalse(equal(1.0d - 0.9d, 0.9d - 0.7d));
    }

    @Test
    public void testRound() {
        assertEquals(0, roundDown(0));
        assertEquals(1, roundDown(1.0));
        assertEquals(1, roundDown(1.1));
        assertEquals(1, roundDown(1.8));

        assertEquals(2, roundUp(2.0));
        assertEquals(3, roundUp(2.1));
        assertEquals(3, roundUp(2.9));
    }

    @Test
    public void testBetween() {
        assertTrue(between(0, 0, 10));
        assertTrue(between(1, 0, 10));
        assertTrue(between(10, 0, 10));

        assertFalse(between(1, 2, 10));
        assertFalse(between(11, 1, 10));

        assertTrue(between(0.2 - 0.1, 0.05, 1.0));
        assertTrue(between(0.8 - 0.1, 0.05, 1.0));

        assertTrue(between(2.0 - 1.0, 0.05, 1.0));
        assertFalse(between(1.8 - 0.1, 0.05, 1.0));
    }

    @Test
    public void testSeasonOfDate() throws ParseException {
        assertEquals(1, getSeason(date("2022-1-1 1:2:3.0")));
        assertEquals(1, getSeason(date("2022-2-28 1:2:3.0")));
        assertEquals(1, getSeason(date("2022-3-1 1:2:3.0")));
        assertEquals(2, getSeason(date("2022-4-1 1:2:3.0")));
        assertEquals(2, getSeason(date("2022-5-1 1:2:3.0")));
        assertEquals(2, getSeason(date("2022-6-1 1:2:3.0")));
        assertEquals(3, getSeason(date("2022-7-20 1:2:3.0")));
        assertEquals(3, getSeason(date("2022-8-20 1:2:3.0")));
        assertEquals(3, getSeason(date("2022-09-20 1:2:3.0")));
        assertEquals(4, getSeason(date("2022-10-30 1:2:3.0")));
        assertEquals(4, getSeason(date("2022-11-30 1:2:3.0")));
        assertEquals(4, getSeason(date("2022-12-30 1:2:3.0")));
        assertEquals(0, getSeason(null));
    }

    @Test
    public void testAddMonth() throws ParseException {
        Date date = date("2022-10-1 1:2:3");
        Date nextMonth = addMonth(date, 1);
        assertEquals(date("2022-11-1 1:2:3"), nextMonth);
    }

    @Test
    public void testBetweenTime() throws ParseException {
        assertFalse(between("10:00:00", "12:00:00", "15:00:00"));
        assertFalse(between("16:00:00", "12:0:00", "15:0:00"));

        assertTrue(between("10:00:00", "2:0:00", "15:0:00"));
        assertTrue(between("10:00:00", "10:0:00", "15:0:1"));
    }

    @Test
    public void testAddDay() {
        Date now = now();
        Date nextDay = addDay(now, 1);
        assertEquals(1, diffDay(now, nextDay));
    }

    @Test
    public void testAddHour() {
        Date now = now();
        Date nextDay = addHour(now, 1);
        assertEquals(1, diffHour(now, nextDay));
    }


    @Test
    public void testAddMinute() {
        Date now = now();
        Date nextDay = addMinute(now, 1);
        assertEquals(1, diffMinute(now, nextDay));
    }

    @Test
    public void testAddSecond() {
        Date now = now();
        Date nextDay = addSecond(now, 1);
        assertEquals(1, diffSecond(now, nextDay));
    }

    @Test
    public void testEndWeek() throws ParseException {
        Date date = endWeek(date("2022-10-29 1:2:3.999"));
        assertEquals(date(2022, 10, 29, 23, 59, 59, 999), date);

        date = endWeek(date("2022-10-30 1:2:3.999"));
        assertEquals(date(2022, 11, 5, 23, 59, 59, 999), date);
    }

    @Test
    public void testEndYear() throws ParseException {
        Date date = endYear(date("2022-10-29 1:2:3.999"));
        assertEquals(date(2022, 12, 31, 23, 59, 59, 999), date);
    }

    @Test
    public void testTime() {
        String s = time();
        logError("当前时间: " + time());
        assertNotNull(s);
    }

    @Test
    public void testTestToStr() {
        assertEquals("", toStr(null));
        assertEquals("", toStr(""));
        assertEquals("123", toStr("123"));
        assertEquals("def", toStr("def"));

        Date date = mock(Date.class);
        when(date.toString()).thenThrow(new RuntimeException(("oops")));
        assertEquals("", toStr(date));
    }

    @Test
    public void testTomorrow() {
        assertEquals(tomorrow(), addDay(today(), 1));
    }

    @Test
    public void testBeginYear() {
        assertEquals(beginYear(date(2020, 10, 20, 1, 2, 3, 0)), date(2020, 1, 1));
    }

    @Test
    public void testEndSeason() throws ParseException {
        assertEquals(endSeason(date("2022-1-2 3:4:5")), date(2022, 3, 31, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-2-2 3:4:5")), date(2022, 3, 31, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-3-2 3:4:5")), date(2022, 3, 31, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-4-2 3:4:5")), date(2022, 6, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-5-2 3:4:5")), date(2022, 6, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-6-2 3:4:5")), date(2022, 6, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-7-2 3:4:5")), date(2022, 9, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-8-2 3:4:5")), date(2022, 9, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-9-2 3:4:5")), date(2022, 9, 30, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-10-2 3:4:5")), date(2022, 12, 31, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-11-2 3:4:5")), date(2022, 12, 31, 23, 59, 59, 999));
        assertEquals(endSeason(date("2022-12-2 3:4:5")), date(2022, 12, 31, 23, 59, 59, 999));
    }

    @Test
    public void testBeginSeason() throws ParseException {
        assertEquals(beginSeason(date("2022-1-2 3:4:5")), date(2022, 1, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-2-2 3:4:5")), date(2022, 1, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-3-2 3:4:5")), date(2022, 1, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-4-2 3:4:5")), date(2022, 4, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-5-2 3:4:5")), date(2022, 4, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-6-2 3:4:5")), date(2022, 4, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-7-2 3:4:5")), date(2022, 7, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-8-2 3:4:5")), date(2022, 7, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-9-2 3:4:5")), date(2022, 7, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-10-2 3:4:5")), date(2022, 10, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-11-2 3:4:5")), date(2022, 10, 1, 0, 0, 0, 0));
        assertEquals(beginSeason(date("2022-12-2 3:4:5")), date(2022, 10, 1, 0, 0, 0, 0));
    }

    @Test
    public void testBeginWeek() {
        assertEquals(beginWeek(date(2022, 11, 5)), date(2022, 10, 30));
        assertEquals(beginWeek(date(2022, 10, 30)), date(2022, 10, 30));
        assertEquals(beginWeek(date(2022, 10, 29)), date(2022, 10, 23));
        assertEquals(beginWeek(date(2022, 10, 28)), date(2022, 10, 23));
        assertEquals(beginWeek(date(2022, 10, 23)), date(2022, 10, 23));
    }

    @Test
    public void testYesterday() {
        assertEquals(yesterday(), addDay(today(), -1));
    }

    @Test
    public void testGetSeasonRange() throws ParseException {
        String[] ret;
        ret = getSeasonRange("2022", 1);
        assertEquals("2022-01-01 00:00:00", ret[0]);
        assertEquals("2022-03-31 23:59:59", ret[1]);
        ret = getSeasonRange(date("2022-1-1 0:0:0"));
        assertEquals("2022-01-01 00:00:00", ret[0]);
        assertEquals("2022-03-31 23:59:59", ret[1]);

        ret = getSeasonRange("2022", 2);
        assertEquals("2022-04-01 00:00:00", ret[0]);
        assertEquals("2022-06-30 23:59:59", ret[1]);

        ret = getSeasonRange("2022", 3);
        assertEquals("2022-07-01 00:00:00", ret[0]);
        assertEquals("2022-09-30 23:59:59", ret[1]);
        ret = getSeasonRange("2022", 4);
        assertEquals("2022-10-01 00:00:00", ret[0]);
        assertEquals("2022-12-31 23:59:59", ret[1]);
    }

    @Test
    public void testEndDay() {
        long d = diffTime(endDay(now()), tomorrow());
        assertEquals(1, d);
    }

    @Test
    public void testSetDate() {
        Calendar calendar = Calendar.getInstance();
        setDate(calendar, 2022, 1, 1);
        setTime(calendar, 0, 0, 0, 0);
        assertEquals(calendar.getTime(), beginYear(date(2022, 10, 20)));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(isEmpty("abc"));
        assertTrue(isEmpty(""));
        assertTrue(isEmpty(null));
        assertTrue(isEmpty("null"));
    }

    @Test
    public void testNow() {
        assertEquals(now(FORMAT_DATE), format(today(), FORMAT_DATE));
    }

    @Test
    public void testDateToUnix() {
        assertEquals(dateToUnix(today()), today().getTime());
    }

    @Test
    public void testYear() {
        assertEquals(2022, getYear(date(2022, 1, 1)));
        assertEquals(1970, getYear(unixToDate(0)));
        assertEquals(1, getMonth(unixToDate(0)));
        assertEquals(12, getMonth(date(2022, 12, 20, 1, 1, 1, 1)));
    }

    @Test
    public void testStringToFile() throws IOException {
        String txt = "Line1\r\nLine2";
        String tmp = getTempFilename(null, null);
        logDebug("临时文件名: " + tmp);
        assertTrue(stringToFile(tmp, txt));
        List<String> buff = loadFromFile(tmp);
        String read = join(buff, "\r\n");
        assertEquals(txt, read);

        String s = stringFromFile(tmp);
        assertEquals(s, txt);
    }

    @Test
    public void testStrToDate() {
        assertEquals(new Date(-8 * MS_HOUR), strToDate("1970-01-01"));
        assertEquals(date(2020, 10, 30, 0, 0, 1, 0), strToDate("2020-10-30 00:00:01"));
        assertEquals(date(2020, 10, 30, 0, 0, 1, 0), strToDate("2020-10-30T00:00:01"));
        assertEquals(date(2020, 10, 30, 0, 0, 1, 123), strToDate("2020-10-30T00:00:01.123"));
        assertEquals(date(2022, 4, 24), strToDate("24, April 2022"));
        assertEquals(date(2022, 10, 30), strToDate("Oct 30, 2022"));
        assertEquals(date(1995, 8, 12, 21, 30, 0, 0), strToDate("Sat, 12 Aug 1995 13:30:00 GMT"));
    }

    private boolean checkEachCharInString(char[] chars, String value) {
        for (char ch : chars) {
            if (value.indexOf(ch) > 0) return true;
        }
        return false;
    }

    @Test
    public void testGetRandString() {
        String s = getRandString(100, true, true, true, true);
        logInfo(s);

        s = getRandString(100, true, true, true, false);
        logInfo(s);
        assertFalse(checkEachCharInString("`~!@#$%^&*()_+\\][{}|:\"';/.,<>?".toCharArray(), s));

        s = getRandString(100, true, true, false, false);
        logInfo(s);
        assertFalse(checkEachCharInString("0123456789".toCharArray(), s));

        s = getRandString(100, true, false, false, false);
        logInfo(s);
        assertFalse(checkEachCharInString("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), s));

        s = getRandString(100, false, true, false, false);
        logInfo(s);
        assertFalse(checkEachCharInString("abcdefghijklmnopqrstuvwxyz".toCharArray(), s));
    }

    @Test
    public void testGetTimeLine() {
        logInfo(getTimeLine(now()));
        logInfo(getTimeLine(today()));
        logInfo(getTimeLine(yesterday()));
        logInfo(getTimeLine(beginWeek(now())));
        logInfo(getTimeLine(beginSeason(now())));
        logInfo(getTimeLine(beginYear(now())));
        logInfo(getTimeLine(tomorrow()));
        logInfo(getTimeLine(endDay(now())));
        logInfo(getTimeLine(endWeek(now())));
        logInfo(getTimeLine(endSeason(now())));
        logInfo(getTimeLine(endYear(now())));
        logInfo(getTimeLine(addDay(now(), -25)));
        assertEquals("3周前", getTimeLine(addDay(now(), -25)));
    }

    @Test
    public void testJoinStr() {
        String s = join("-", "1", "2", "3");
        assertEquals("1-2-3", s);

        s = join("-", 1L, 2L, 3L);
        assertEquals("1-2-3", s);

        assertEquals("1,2,3", join(new int[]{1, 2, 3}, ","));
    }

    @Test
    public void testValidateID() {
        assertTrue(isValidID("340202197104106891"));
        assertTrue(isValidID("340202710410689"));

        assertFalse(isValidID("390202710410689"));
        assertFalse(isValidID("340202197104106892"));
    }

    @Test
    public void testValidateEmail() {
        assertTrue(isValidEmail("abc@123.net"));
        assertFalse(isValidEmail("abc@123net"));
        assertFalse(isValidEmail("abc123.net"));
    }

    @Test
    public void testListFile() {
        Set<String> set = listFile("C:\\windows\\system32\\drivers");
        System.out.println(set);
    }

    @Test
    public void testValidatePhoneNumber() {
        assertTrue(isValidPhoneNumber("13912345678"));
        assertTrue(isValidPhoneNumber("13312345678"));

        assertFalse(isValidPhoneNumber("1331234567"));
        assertFalse(isValidPhoneNumber("133123456a7"));
        assertFalse(isValidPhoneNumber("1331234560007"));
    }

    @Test
    public void testRand() {
        assertNotEquals(rand(), rand(100));
        assertTrue(rand(1, 10) < rand(20, 100));
    }

    @Test
    public void testHex2Bin() {
        String s = "112233445566778899";
        assertEquals(s, bin2hex(hex2bin(s), false));
        assertEquals(s, bin2hex(hex2bin(s), false));
        assertNotEquals(s, bin2hex(hex2bin(s), 5, false));
    }

    @Test
    public void testCrc8() {
        assertEquals(53, crc8(hex2bin("31 32 33 34")));
    }

    @Test
    public void testDumpStack() {
        logWarn(dumpExceptionStack(new IOException("oops")));
        logWarn(dumpThreadStack(Thread.currentThread()));
    }

    @Test
    public void testReadableSize() {
        assertEquals("1.00 KB", readableSize(1024L, false));
        assertEquals("2.00 GB", readableSize(2 * 1024 * 1024 * 1024L, false));
        assertEquals("2.10 GB", readableSize(2 * 1024 * 1024 * 1024L + 1024 * 1024 * 100, false));
    }

    @Test
    public void testDiffYear() {
        assertEquals(1, diffYear(addDay(today(), -366L), today()));
        assertEquals(0, diffYear(addDay(today(), -363L), today()));
    }

    @Test
    public void testExtractFileName() {
        assertEquals("xyz.txt", extractFileName("/abc/def/xyz.txt"));
        assertEquals("xyz", extractFileName("/abc/def/xyz"));
    }

    @Test
    public void testExec() {
        List<String> ret = exec("cmd /c dir C:\\");
        String s = join(ret, "\r\n");
        assertTrue(s.indexOf("C:\\") > 0);
        logInfo(s);

    }

    @Test
    public void testSplit() {
        String s = "123,456,abc";
        assertEquals(split(s, ","), s.split(","));

        s = "123,456,\"abc,def\",xyz";
        assertEquals(4, split(s, ",", '"').length);
        assertEquals("\"abc,def\"", split(s, ",", '"')[2]);

        s = "123,456,'abc,def',xyz";
        assertEquals(4, split(s, ",", '\'').length);
    }

    @Test
    public void testGetIp() {
        logInfo(getIP());

    }

    @Test
    public void testGetUptime() {
        assertEquals("1 天", getUptime(86400));
        assertEquals("1 天 1 秒", getUptime(86401));
        assertEquals("1 分钟", getUptime(60));
        assertEquals("59 秒", getUptime(59));
        assertEquals("2 分钟", getUptime(120));
        assertEquals("1 小时", getUptime(3600));
        assertEquals("53 分钟 21 秒", getUptime(3201));
    }

    @Test
    public void testValidDate() {
        assertTrue(Util.isValidDate("2000-02-2 1:2:3"));
        assertTrue(Util.isValidDate("2000-02-29 01:02:03"));
        assertTrue(Util.isValidDate("2022-02-01 12:34:59"));
        assertTrue(Util.isValidDate("2022-02-01 12:34:59"));
        assertTrue(Util.isValidDate("2022-12-31 2:00:00"));
        assertTrue(Util.isValidDate("8000-10-31 2:00:00"));
        assertTrue(Util.isValidDate("8000-7-31 2:00:00"));
        assertTrue(Util.isValidDate("0001-5-31 2:00:00"));
        assertTrue(Util.isValidDate("0000-02-29 2:00:00"));
        assertTrue(Util.isValidDate("2008-02-29 00:00:00"));
        assertTrue(Util.isValidDate("2008-1-29 00:00:00"));
        assertTrue(Util.isValidDate("2008-3-29 00:00:00"));
        assertTrue(Util.isValidDate("2008-4-29 00:00:00"));
        assertTrue(Util.isValidDate("2008-5-1 00:00:00"));
        assertTrue(Util.isValidDate("1900-6-29 00:00:00"));
        assertTrue(Util.isValidDate("2004-2-29 00:00:00"));
        assertTrue(Util.isValidDate("2020-2-29 00:00:00"));

        assertFalse(Util.isValidDate("1999-02-30 02:00:00"));
        assertFalse(Util.isValidDate("1999-02-00 02:00:00"));
        assertFalse(Util.isValidDate("1999-02-01 60:00:00"));
        assertFalse(Util.isValidDate("1999-02-01 01:60:00"));
        assertFalse(Util.isValidDate("1999-02-01 01:01:90"));
        assertFalse(Util.isValidDate("-1998-02-01 01:01:05"));
        assertFalse(Util.isValidDate("2022-04-31 01:01:05"));
        assertFalse(Util.isValidDate("2022-06-31 01:01:05"));
        assertFalse(Util.isValidDate("2022-09-31 01:01:05"));
        assertFalse(Util.isValidDate("2022-11-31 01:01:05"));
        assertFalse(Util.isValidDate("0001-11-31 01:01:05"));
        assertFalse(Util.isValidDate("0001-02-31 01:01:05"));
    }

    @Test
    public void testConvert() {
        assertFalse(isLong("111aaaa"));
        assertFalse(isLong("-111abc"));
        assertFalse(isLong("aaaa000"));
        assertFalse(isLong("ox11111"));
        assertFalse(isLong("11111111111111111111111111111111111111111111111111111111111111"));
        assertFalse(isInt("11111aaaa"));
        assertFalse(isInt("bbbbb"));
        assertFalse(isInt("uiog009"));
        assertFalse(isInt("111111111111111111111111111111111"));

        assertTrue(isLong("00000"));
        assertTrue(isLong("0"));
        assertTrue(isLong("9999999"));
        assertTrue(isLong("-1117595738"));
        assertTrue(isLong("2147483649"));
        assertTrue(isInt("2147483647"));
        assertTrue(isInt("214748364"));
        assertTrue(isInt("214748364"));
        assertTrue(isInt("-2147483647"));
        assertTrue(isInt("-147483647"));
    }

    @Test
    public void testIsValidDate() {
        assertTrue(isValidDate("1999-02-01 01:01:50", null));
        assertTrue(isValidDate("1999-02-01", "%y-%m-%d"));
        assertTrue(isValidDate("1999-02-01 01:01:58", "%y-%m-%d %h:%n:%s"));
        assertTrue(isValidDate("1999年02月01日 01:01:1", "%y年%m月%d日 %h:%n:%s"));
        assertTrue(isValidDate("1999年02月01日 01:01:1 000", "%y年%m月%d日 %h:%n:%s 000"));
        assertTrue(isValidDate("1999-02-01(01:01:6)", "%y-%m-%d(%h:%n:%s)"));
        assertTrue(isValidDate("1999-02-01((01:01:6)", "%y-%m-%d((%h:%n:%s)"));
        assertTrue(isValidDate("1999-02.*-01((01:01:6)", "%y-%m.*-%d((%h:%n:%s)"));
        assertTrue(isValidDate("1999/02/01[01:01:6)", "%y/%m/%d[%h:%n:%s)"));
        assertTrue(isValidDate("1999-02-01%01:01:6)", "%y-%m-%d%%h:%n:%s)"));
        assertTrue(isValidDate("10-30, 2022 12:34:56", "%m-%d, %y %h:%n:%s"));

        assertFalse(isValidDate("8000-02-01 01:01:1", "%y-%m-%d"));
        assertFalse(isValidDate("1999- 02-01 01:01:2", "%y-%m-%d %h:%n:%s"));
        assertFalse(isValidDate("1999年02月01日 01:01:2", "%y-%m-%d %h:%n:%s"));
        assertFalse(isValidDate("1999年02月01日 01:01:2", "%y年%m-%日 %h:%n:%s"));
        assertFalse(isValidDate("1999-01 01:01:3", "%y-%m-%d"));
        assertFalse(isValidDate("1999-02-01 01:01:4 000", "%y-%m-%d"));
        assertFalse(isValidDate("1999-02-01", "%y-%m-%d 000"));
        assertFalse(isValidDate("1999-02-01(01:01:6)", "%y-%m-%d %h:%n:%s"));
        assertFalse(isValidDate("1999-02-01(01:01:6)", "%y-%m-%d %h:%n:%s)"));
        assertFalse(isValidDate("1999-%02-01(01:01:6)", "%y-%%m-%d %h:%n:%s)"));
        assertFalse(isValidDate("1999-02-01(01:01:6)", "%y-%m-%d (%h:%n:%s"));

        assertFalse(isValidDate("1999年02月01日 01:01:90", "%y年%m月%d %h:%n:%s"));
    }

    @Test
    public void testGetOrderIndexAscending() {
        assertEquals("3, 5, 4, 2, 1, 2", join(getOrderIndexAscending(new long[]{200L, 500L, 300L, 100L, 10L, 100L}), ", "));
    }

    @Test
    public void testGetNumberFromString() {
        String strContent = "a-100.01 divided by 5 is 20";

        assertEquals("-100,1,5,20", join(getNumberFromString(strContent), ","));
        assertEquals("-100.01,5.0,20.0", join(getFloatFromString(strContent), ","));
    }

    @Test
    public void testGetValidFilename() {
        assertEquals("a_b_c_d_e_f_g_h_i_j.jpg", getValidFilename("a\\b/c:d*e?f\"g<h>i|j.jpg"));
    }

    @Test
    public void testByteOrder() {
        long benchStart = System.currentTimeMillis();
        assertEquals(0, ntohl(htonl(0)));
        assertEquals(1, ntohl(htonl(1)));
        assertEquals(2147483647, ntohl(htonl(2147483647)));
        assertEquals(-1, ntohl(htonl(-1)));
        assertEquals(-2, ntohl(htonl(-2)));
        assertEquals(-2147483648, ntohl(htonl(-2147483648)));
        assertEquals(0x12345678, ntohl(htonl(0x12345678)));
        assertEquals(0xF1F2F3F4, ntohl(htonl(0xF1F2F3F4)));
        assertEquals(11223344, ntohl(htonl(11223344)));
        System.out.println(" 运行时长: " + (System.currentTimeMillis() - benchStart) + " 毫秒");
    }

    @Test
    public void testMultiply() {
        assertEquals(multiply("1", "42695872905829052789402842908462"), "42695872905829052789402842908462");
        assertEquals(multiply("1", "-42695872905829052789402842908462"), "-42695872905829052789402842908462");
        assertEquals(multiply("0", "42695872905829052789402842908462"), "0");
        assertEquals(multiply("100", "123"), "12300");
        assertEquals(multiply("-100", "-123"), "12300");
        assertEquals(multiply("-100", "123"), "-12300");
        assertEquals(multiply("100", "-123"), "-12300");
        assertEquals(multiply("123456789", "987654321"), "121932631112635269");

        assertEquals("-0.1", divide("42695872905829052789402842908462", "-426958729058290527894028429084620"));
    }

    @Test
    public void testIP() {
        String ipStr = "192.168.1.1";
        long ipInt = ipToInt(ipStr);
        assertEquals(ipStr, intToIp(ipInt));
        assertEquals(3232235777L, ipToInt(ipStr));
    }

    @Test
    public void testUnique() {
        String[] dupliate = new String[]{"", "", "abc", "abc", "123", "def", null, null, "xyz"};
        String[] ret = unique(dupliate);
        assertEquals(6, ret.length);


        Object[] nullObj = null;
        assertNull(unique(nullObj));
        List<Object> nulList = null;
        assertNull(unique(nulList));
    }

    @Test
    public void testMatchWildcard() {
        assertTrue(matchWildcard("*.jpg", "abc.jpg"));
        assertTrue(matchWildcard("ab*.jpg", "abc.jpg"));
        assertTrue(matchWildcard("???.jpg", "abc.jpg"));

        assertFalse(matchWildcard("???.jpg", "abcd.jpg"));
        assertFalse(matchWildcard("a?b.jpg", "abc.jpg"));
        assertFalse(matchWildcard("a?b.jpg", "abc.jpg"));
        assertFalse(matchWildcard("*.jpg", "abc.cad"));
    }

    enum Currencies {
        USD,
        YEN,
        EUR,
        INR
    }

    @Test
    public void testEnum() {
        Currencies currencies = Currencies.EUR;
        assertEquals("EUR", enumToString(currencies));
        assertEquals("USD,YEN,EUR,INR", join(enumToStringValues(Currencies.class), ","));

        assertEquals(Currencies.INR, stringToEnum("InR", Currencies.class));
    }

    @Test
    public void testPadString() {
        assertEquals("          ", padLeft("", ' ', 10));
        assertEquals("     12345", padLeft("12345", ' ', 10));
        assertEquals("*****12345", padLeft("12345", '*', 10));
        assertEquals("abcde12345", padLeft("abcde12345", ' ', 8));
        assertEquals("**********", padLeft(null, '*', 10));

        assertEquals("12345*****", padRight("12345", '*', 10));
        assertEquals("12345     ", padRight("12345", ' ', 10));
        assertEquals("abcde12345", padRight("abcde12345", ' ', 8));
        assertEquals("          ", padRight(null, ' ', 10));
        assertEquals("          ", padRight("", ' ', 10));
    }

    @Test
    public void testDES() {
        String text = "This is OK";
        String key = "123";
        String enc = desEncrypt(text, key);
        String dec = desDecrypt(enc, key);
        assertEquals(text, dec);
    }
}