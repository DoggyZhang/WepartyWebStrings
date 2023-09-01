package web;

import excel2string.*;
import string2excel.*;

import java.util.*;

public class Main {

    private static final String MODE = "mode";
    private static final String NEW_WEB = "newWeb";
    private static final String PROJECT = "project";
    private static final String OUTPUT = "output";

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            showHelp();
            return;
        }

        Map<String, String> argsMap = parseArgs(args);

        String mode = argsMap.get(MODE);
        if (mode == null) {
            System.out.println("必须要设置一个模式, string2excel / excel2string");
            return;
        }
        switch (mode) {
            case "string2excel": {
                new String2Excel(
                        argsMap.get(PROJECT),
                        argsMap.get(OUTPUT),
                        Boolean.parseBoolean(argsMap.get(NEW_WEB))
                ).execute();
            }
            case "excel2string": {
                new Excel2String(
                        argsMap.get(PROJECT),
                        argsMap.get(OUTPUT),
                        Boolean.parseBoolean(argsMap.get(NEW_WEB))
                ).execute();
            }
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> argsMap = new HashMap<>();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                continue;
            }
            String type = split[0];
            String value = split[1];
            argsMap.put(type, value);
        }
        return argsMap;
    }

    private static void showHelp() {
        System.out.println("设置转化模式");
        System.out.println("1. String转Excel");
        System.out.println("   输入以下内容");
        System.out.println("   mode=string2excel   string转excel模式");
        System.out.println("   newWeb=true|false   新/旧Web项目");
        System.out.println("   project=[path]         [path]为项目根路径");
        System.out.println("   output=[output]     [output]为结果输出路径,为空则默认为path");
        System.out.println();
        System.out.println("2. Excel转String");
        System.out.println("   输入以下内容");
        System.out.println("   mode=excel2string   excel转string模式");
        System.out.println("   newWeb=true|false   新/旧Web项目");
        System.out.println("   project=[path]         [path]为项目根路径");
        System.out.println("   output=[output]     [output]为结果输出路径,为空则默认为path");
        System.out.println();
    }
}