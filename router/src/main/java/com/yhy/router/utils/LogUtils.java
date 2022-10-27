package com.yhy.router.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-12-23 14:15
 * version: 1.0.0
 * desc   : 日志工具
 */
public class LogUtils {

    static final int V = Log.VERBOSE;
    static final int D = Log.DEBUG;
    static final int I = Log.INFO;
    static final int W = Log.WARN;
    static final int E = Log.ERROR;
    static final int A = Log.ASSERT;

    private static final char[] T = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};

    private static final int FILE = 0x10;
    private static final int JSON = 0x20;
    private static final int XML = 0x30;

    private static ExecutorService sExecutor;
    private static String sDefaultDir;// log 默认存储目录
    private static String sDir;       // log 存储目录
    private static String sFilePrefix = "util";// log 文件前缀
    private static boolean sLogEnable = true;  // log 总开关，默认开
    private static boolean sLog2ConsoleEnable = true;  // logcat 是否打印，默认打印
    private static String sGlobalTag = null;  // log 标签
    private static boolean sTagIsSpace = true;  // log 标签是否为空白
    private static boolean sLogHeadEnable = true;  // log 头部开关，默认开
    private static boolean sLog2FileEnable = false; // log 写入文件开关，默认关
    private static boolean sLogBorderEnable = true;  // log 边框开关，默认开
    private static int sConsoleFilter = V;     // log 控制台过滤器
    private static int sFileFilter = V;     // log 文件过滤器
    private static int sStackDeep = 1;     // log 栈深度

    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String TOP_CORNER = "┌";
    private static final String MIDDLE_CORNER = "├";
    private static final String LEFT_BORDER = "│ ";
    private static final String BOTTOM_CORNER = "└";
    private static final String SIDE_DIVIDER = "────────────────────────────────────────────────────────";
    private static final String MIDDLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
    private static final String TOP_BORDER = TOP_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + MIDDLE_DIVIDER + MIDDLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;
    private static final int MAX_LEN = 4000;
    private static final Format FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault());
    private static final String NOTHING = "log nothing";
    private static final String NULL = "null";
    private static final String ARGS = "args";
    private static final Config CONFIG = new Config();
    @SuppressLint("StaticFieldLeak")
    private static Application mApp;

    /**
     * 构造函数
     */
    private LogUtils() {
        throw new UnsupportedOperationException("Can not instantiate utils class.");
    }

    /**
     * 获取默认配置
     *
     * @return 默认配置
     */
    public static Config getConfig() {
        return CONFIG;
    }

    /**
     * 日志级别 Log.v
     *
     * @param contents 日志内容
     */
    public static void v(final Object... contents) {
        log(V, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.v
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void v(final String tag, final Object... contents) {
        log(V, tag, contents);
    }

    /**
     * 日志级别 Log.d
     *
     * @param contents 日志内容
     */
    public static void d(final Object... contents) {
        log(D, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.d
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void d(final String tag, final Object... contents) {
        log(D, tag, contents);
    }

    /**
     * 日志级别 Log.i
     *
     * @param contents 日志内容
     */
    public static void i(final Object... contents) {
        log(I, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.i
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void i(final String tag, final Object... contents) {
        log(I, tag, contents);
    }

    /**
     * 日志级别 Log.w
     *
     * @param contents 日志内容
     */
    public static void w(final Object... contents) {
        log(W, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.w
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void w(final String tag, final Object... contents) {
        log(W, tag, contents);
    }

    /**
     * 日志级别 Log.e
     *
     * @param contents 日志内容
     */
    public static void e(final Object... contents) {
        log(E, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.e
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void e(final String tag, final Object... contents) {
        log(E, tag, contents);
    }

    /**
     * 日志级别 Log.a
     *
     * @param contents 日志内容
     */
    public static void a(final Object... contents) {
        log(A, sGlobalTag, contents);
    }

    /**
     * 日志级别 Log.a
     *
     * @param tag      tag
     * @param contents 日志内容
     */
    public static void a(final String tag, final Object... contents) {
        log(A, tag, contents);
    }

    /**
     * 输出到文件
     *
     * @param content 日志内容
     */
    public static void file(final Object content) {
        log(FILE | D, sGlobalTag, content);
    }

    /**
     * 输出到文件
     *
     * @param tag     tag
     * @param content 日志内容
     */
    public static void file(final String tag, final Object content) {
        log(FILE | D, tag, content);
    }

    /**
     * 输出到文件
     *
     * @param type    日志级别
     * @param tag     tag
     * @param content 日志内容
     */
    public static void file(final int type, final String tag, final Object content) {
        log(FILE | type, tag, content);
    }

    /**
     * 输出json
     *
     * @param content json字符串
     */
    public static void json(final String content) {
        log(JSON | D, sGlobalTag, content);
    }

    /**
     * 输出json
     *
     * @param type    日志级别
     * @param content json字符串
     */
    public static void json(final int type, final String content) {
        log(JSON | type, sGlobalTag, content);
    }

    /**
     * 输出json
     *
     * @param tag     tag
     * @param content json字符串
     */
    public static void json(final String tag, final String content) {
        log(JSON | D, tag, content);
    }

    /**
     * 输出json
     *
     * @param type    日志级别
     * @param tag     tag
     * @param content json字符串
     */
    public static void json(final int type, final String tag, final String content) {
        log(JSON | type, tag, content);
    }

    /**
     * 输出xml
     *
     * @param content xml字符串
     */
    public static void xml(final String content) {
        log(XML | D, sGlobalTag, content);
    }

    /**
     * 输出xml
     *
     * @param type    日志级别
     * @param content xml字符串
     */
    public static void xml(final int type, final String content) {
        log(XML | type, sGlobalTag, content);
    }

    /**
     * 输出xml
     *
     * @param tag     tag
     * @param content xml字符串
     */
    public static void xml(final String tag, final String content) {
        log(XML | D, tag, content);
    }

    /**
     * 打印日志
     *
     * @param type     日志级别
     * @param tag      tag
     * @param contents 日志内容
     */
    private static void log(final int type, final String tag, final Object... contents) {
        if (!sLogEnable || (!sLog2ConsoleEnable && !sLog2FileEnable)) return;
        int type_low = type & 0x0f, type_high = type & 0xf0;
        if (type_low < sConsoleFilter && type_low < sFileFilter) return;
        final TagHead tagHead = processTagAndHead(tag);
        String body = processBody(type_high, contents);
        if (sLog2ConsoleEnable && type_low >= sConsoleFilter && type_high != FILE) {
            print2Console(type_low, tagHead.tag, tagHead.consoleHead, body);
        }
        if ((sLog2FileEnable || type_high == FILE) && type_low >= sFileFilter) {
            print2File(type_low, tagHead.tag, tagHead.fileHead + body);
        }
    }

    /**
     * 生成日志头部
     *
     * @param tag tag
     * @return 日志头部
     */
    private static TagHead processTagAndHead(String tag) {
        if (!sTagIsSpace && !sLogHeadEnable) {
            tag = sGlobalTag;
        } else {
            final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            StackTraceElement targetElement = stackTrace[3];
            String fileName = targetElement.getFileName();
            String className;
            if (fileName == null) {// 混淆可能会导致获取为空 加-keepattributes SourceFile,LineNumberTable
                className = targetElement.getClassName();
                String[] classNameInfo = className.split("\\.");
                if (classNameInfo.length > 0) {
                    className = classNameInfo[classNameInfo.length - 1];
                }
                int index = className.indexOf('$');
                if (index != -1) {
                    className = className.substring(0, index);
                }
                fileName = className + ".java";
            } else {
                int index = fileName.indexOf('.');// 混淆可能导致文件名被改变从而找不到"."
                className = index == -1 ? fileName : fileName.substring(0, index);
            }
            if (sTagIsSpace) tag = isSpace(tag) ? className : tag;
            if (sLogHeadEnable) {
                String tName = Thread.currentThread().getName();
                final String head = new Formatter()
                        .format("%s, %s(%s:%d)",
                                tName,
                                targetElement.getMethodName(),
                                fileName,
                                targetElement.getLineNumber())
                        .toString();
                final String fileHead = " [" + head + "]: ";
                if (sStackDeep <= 1) {
                    return new TagHead(tag, new String[]{head}, fileHead);
                } else {
                    final String[] consoleHead = new String[Math.min(sStackDeep, stackTrace.length - 3)];
                    consoleHead[0] = head;
                    int spaceLen = tName.length() + 2;
                    String space = new Formatter().format("%" + spaceLen + "s", "").toString();
                    for (int i = 1, len = consoleHead.length; i < len; ++i) {
                        targetElement = stackTrace[i + 3];
                        consoleHead[i] = new Formatter()
                                .format("%s%s(%s:%d)",
                                        space,
                                        targetElement.getMethodName(),
                                        targetElement.getFileName(),
                                        targetElement.getLineNumber())
                                .toString();
                    }
                    return new TagHead(tag, consoleHead, fileHead);
                }
            }
        }
        return new TagHead(tag, null, ": ");
    }

    /**
     * 生成日志信息
     *
     * @param type     日志级别
     * @param contents 日志内容
     * @return 日志信息
     */
    private static String processBody(final int type, final Object... contents) {
        String body = NULL;
        if (contents != null) {
            if (contents.length == 1) {
                Object object = contents[0];
                if (object != null) body = object.toString();
                if (type == JSON) {
                    body = formatJson(body);
                } else if (type == XML) {
                    body = formatXml(body);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, len = contents.length; i < len; ++i) {
                    Object content = contents[i];
                    sb.append(ARGS)
                            .append("[")
                            .append(i)
                            .append("]")
                            .append(" = ")
                            .append(content == null ? NULL : content.toString())
                            .append(LINE_SEP);
                }
                body = sb.toString();
            }
        }
        return body.length() == 0 ? NOTHING : body;
    }

    /**
     * 解析json字符串
     *
     * @param json json
     * @return 格式化后的json字符串
     */
    private static String formatJson(String json) {
        try {
            if (json.startsWith("{")) {
                json = new JSONObject(json).toString(4);
            } else if (json.startsWith("[")) {
                json = new JSONArray(json).toString(4);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 解析xml
     *
     * @param xml xml
     * @return 格式化后的xml字符串
     */
    private static String formatXml(String xml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);
            xml = xmlOutput.getWriter().toString().replaceFirst(">", ">" + LINE_SEP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * 打印日志到控制台
     *
     * @param type 日志级别
     * @param tag  tag
     * @param head 头部
     * @param msg  日志信息
     */
    private static void print2Console(final int type, final String tag, final String[] head, final String msg) {
        printBorder(type, tag, true);
        printHead(type, tag, head);
        printMsg(type, tag, msg);
        printBorder(type, tag, false);
    }

    /**
     * 打印边框
     *
     * @param type  日志级别
     * @param tag   tag
     * @param isTop 是否是顶部
     */
    private static void printBorder(final int type, final String tag, boolean isTop) {
        if (sLogBorderEnable) {
            Log.println(type, tag, isTop ? TOP_BORDER : BOTTOM_BORDER);
        }
    }

    /**
     * 打印头部
     *
     * @param type 日志级别
     * @param tag  tag
     * @param head 头部信息
     */
    private static void printHead(final int type, final String tag, final String[] head) {
        if (head != null) {
            for (String aHead : head) {
                Log.println(type, tag, sLogBorderEnable ? LEFT_BORDER + aHead : aHead);
            }
            if (sLogBorderEnable) Log.println(type, tag, MIDDLE_BORDER);
        }
    }

    /**
     * 打印日志信息
     *
     * @param type 日志级别
     * @param tag  tag
     * @param msg  日志信息
     */
    private static void printMsg(final int type, final String tag, final String msg) {
        int len = msg.length();
        int countOfSub = len / MAX_LEN;
        if (countOfSub > 0) {
            int index = 0;
            for (int i = 0; i < countOfSub; i++) {
                printSubMsg(type, tag, msg.substring(index, index + MAX_LEN));
                index += MAX_LEN;
            }
            if (index != len) {
                printSubMsg(type, tag, msg.substring(index, len));
            }
        } else {
            printSubMsg(type, tag, msg);
        }
    }

    /**
     * 打印分段日志信息
     *
     * @param type 日志级别
     * @param tag  tag
     * @param msg  日志信息
     */
    private static void printSubMsg(final int type, final String tag, final String msg) {
        if (!sLogBorderEnable) {
            Log.println(type, tag, msg);
            return;
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = msg.split(LINE_SEP);
        for (String line : lines) {
            Log.println(type, tag, LEFT_BORDER + line);
        }
    }

    /**
     * 打印到文件
     *
     * @param type 日志级别
     * @param tag  tag
     * @param msg  日志信息
     */
    private static void print2File(final int type, final String tag, final String msg) {
        Date now = new Date(System.currentTimeMillis());
        String format = FORMAT.format(now);
        String date = format.substring(0, 5);
        String time = format.substring(6);
        final String fullPath = (sDir == null ? sDefaultDir : sDir) + sFilePrefix + "-" + date + ".txt";
        if (!createOrExistsFile(fullPath)) {
            Log.e(tag, "log to " + fullPath + " failed!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(time)
                .append(T[type - V])
                .append("/")
                .append(tag)
                .append(msg)
                .append(LINE_SEP);
        final String content = sb.toString();
        if (input2File(content, fullPath)) {
            Log.d(tag, "log to " + fullPath + " success!");
        } else {
            Log.e(tag, "log to " + fullPath + " failed!");
        }
    }

    /**
     * 创建日志文件
     *
     * @param filePath 文件路径
     * @return 是否创建成功
     */
    private static boolean createOrExistsFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            boolean isCreate = file.createNewFile();
            if (isCreate) printDeviceInfo(filePath);
            return isCreate;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 打印设备信息
     *
     * @param filePath 文件路径
     */
    private static void printDeviceInfo(final String filePath) {
        String versionName = "";
        int versionCode = 0;
        try {
            PackageInfo pi = mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final String head = "************* Log Head ****************" +
                "\nDevice Manufacturer: " + Build.MANUFACTURER +// 设备厂商
                "\nDevice Model       : " + Build.MODEL +// 设备型号
                "\nAndroid Version    : " + Build.VERSION.RELEASE +// 系统版本
                "\nAndroid SDK        : " + Build.VERSION.SDK_INT +// SDK 版本
                "\nApp VersionName    : " + versionName +
                "\nApp VersionCode    : " + versionCode +
                "\n************* Log Head ****************\n\n";
        input2File(head, filePath);
    }

    /**
     * 创建日志文件文件夹
     *
     * @param file 文件夹
     * @return 是否创建成功
     */
    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 是否是空字符串
     *
     * @param s 字符串
     * @return 是空字符串
     */
    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印到文件
     *
     * @param input    日志信息
     * @param filePath 文件路径
     * @return 是否打印成功
     */
    private static boolean input2File(final String input, final String filePath) {
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        Future<Boolean> submit = sExecutor.submit(() -> {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filePath, true));
                bw.write(input);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            return submit.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 日志配置信息
     */
    public static class Config {

        /**
         * 构造函数
         */
        private Config() {
        }

        /**
         * 设置Application
         *
         * @param app 当前Application
         * @return 当前对象
         */
        public Config setApp(Application app) {
            mApp = app;
            if (sDefaultDir != null) return this;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && mApp.getExternalCacheDir() != null)
                sDefaultDir = mApp.getExternalCacheDir() + FILE_SEP + "log" + FILE_SEP;
            else {
                sDefaultDir = mApp.getCacheDir() + FILE_SEP + "log" + FILE_SEP;
            }
            return this;
        }

        /**
         * 日志总开关
         *
         * @param logEnable 日志开关
         * @return 当前对象
         */
        public Config setLogEnable(final boolean logEnable) {
            sLogEnable = logEnable;
            return this;
        }

        /**
         * 控制台开关
         *
         * @param consoleEnable 控制台开关
         * @return 当前对象
         */
        public Config setConsoleEnable(final boolean consoleEnable) {
            sLog2ConsoleEnable = consoleEnable;
            return this;
        }

        /**
         * 设置全局tag
         *
         * @param tag 全局tag
         * @return 当前对象
         */
        public Config setGlobalTag(final String tag) {
            if (isSpace(tag)) {
                sGlobalTag = "";
                sTagIsSpace = true;
            } else {
                sGlobalTag = tag;
                sTagIsSpace = false;
            }
            return this;
        }

        /**
         * 日志头部开关
         *
         * @param logHeadEnable 头部开关
         * @return 当前对象
         */
        public Config setLogHeadEnable(final boolean logHeadEnable) {
            sLogHeadEnable = logHeadEnable;
            return this;
        }

        /**
         * 日志文件开关
         *
         * @param log2FileEnable 日志文件开关
         * @return 当前对象
         */
        public Config setLog2FileEnable(final boolean log2FileEnable) {
            sLog2FileEnable = log2FileEnable;
            return this;
        }

        /**
         * 设置日志文件夹路径
         *
         * @param dir 日志文件夹路径
         * @return 当前对象
         */
        public Config setDir(final String dir) {
            if (isSpace(dir)) {
                sDir = null;
            } else {
                sDir = dir.endsWith(FILE_SEP) ? dir : dir + FILE_SEP;
            }
            return this;
        }

        /**
         * 设置日志文件夹
         *
         * @param dir 日志文件夹
         * @return 当前对象
         */
        public Config setDir(final File dir) {
            sDir = dir == null ? null : dir.getAbsolutePath() + FILE_SEP;
            return this;
        }

        /**
         * 设置日志文件前缀
         *
         * @param filePrefix 前缀
         * @return 当前对象
         */
        public Config setFilePrefix(final String filePrefix) {
            if (isSpace(filePrefix)) {
                sFilePrefix = "util";
            } else {
                sFilePrefix = filePrefix;
            }
            return this;
        }

        /**
         * 日志边框开关
         *
         * @param borderEnable 边框开关
         * @return 当前对象
         */
        public Config setBorderEnable(final boolean borderEnable) {
            sLogBorderEnable = borderEnable;
            return this;
        }

        /**
         * 设置控制台过滤器
         *
         * @param consoleFilter 控制台过滤器
         * @return 当前对象
         */
        public Config setConsoleFilter(final int consoleFilter) {
            sConsoleFilter = consoleFilter;
            return this;
        }

        /**
         * 设置文件过滤器
         *
         * @param fileFilter 文件过滤器
         * @return 当前对象
         */
        public Config setFileFilter(final int fileFilter) {
            sFileFilter = fileFilter;
            return this;
        }

        /**
         * 设置栈深度
         *
         * @param stackDeep 栈深度
         * @return 当前对象
         */
        public Config setStackDeep(@IntRange(from = 1) final int stackDeep) {
            sStackDeep = stackDeep;
            return this;
        }

        /**
         * 输入日志
         *
         * @return 日志内容
         */
        @NonNull
        @Override
        public String toString() {
            return "switch: " + sLogEnable
                    + LINE_SEP + "console: " + sLog2ConsoleEnable
                    + LINE_SEP + "tag: " + (sTagIsSpace ? "null" : sGlobalTag)
                    + LINE_SEP + "head: " + sLogHeadEnable
                    + LINE_SEP + "file: " + sLog2FileEnable
                    + LINE_SEP + "dir: " + (sDir == null ? sDefaultDir : sDir)
                    + LINE_SEP + "filePrefix" + sFilePrefix
                    + LINE_SEP + "border: " + sLogBorderEnable
                    + LINE_SEP + "consoleFilter: " + T[sConsoleFilter - V]
                    + LINE_SEP + "fileFilter: " + T[sFileFilter - V]
                    + LINE_SEP + "stackDeep: " + sStackDeep;
        }
    }

    /**
     * 日志头部
     */
    private static class TagHead {
        String tag;
        String[] consoleHead;
        String fileHead;

        /**
         * 构造函数
         *
         * @param tag         tag
         * @param consoleHead 控制台头部
         * @param fileHead    文件头部
         */
        TagHead(String tag, String[] consoleHead, String fileHead) {
            this.tag = tag;
            this.consoleHead = consoleHead;
            this.fileHead = fileHead;
        }
    }
}
