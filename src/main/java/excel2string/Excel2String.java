package excel2string;

import com.google.gson.*;
import com.google.gson.reflect.*;
import data.*;
import org.apache.commons.io.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import util.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import static data.Constant.*;

public class Excel2String {
    private final Gson gson = new Gson();
    private final String projectPath;
    private final String excelPath;
    private final boolean isNewWeb;

    public Excel2String(String projectPath, String excelPath, boolean isNewWeb) {
        this.projectPath = projectPath;
        this.excelPath = excelPath;
        this.isNewWeb = isNewWeb;
    }

    public void execute() {
        //检查Excel路径
        if (excelPath == null || excelPath.isEmpty()) {
            System.out.println("Excel目录路径错误");
            return;
        }
        File excelDir = new File(excelPath);
        if (!excelDir.exists() || !excelDir.isDirectory()) {
            System.out.println("Excel目录路径不存在");
            return;
        }
        String medal2localePath = excelPath + File.separator + MEDAL_2_LOCALE_FILE;
        File medal2LocaleFile = new File(medal2localePath);
        if (!medal2LocaleFile.exists()) {
            System.out.println("文件 " + MEDAL_2_LOCALE_FILE + " 不存在,无法执行批量导入");
            return;
        }

        //检查Project路径
        if (projectPath == null || projectPath.isEmpty()) {
            System.out.println("项目目录路径错误");
            return;
        }
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            System.out.println("项目目录路径不存在");
            return;
        }

        Map<String, String> medal2LocaleMap = getMedal2Locale(medal2LocaleFile);
        if (medal2LocaleMap == null || medal2LocaleMap.isEmpty()) {
            System.out.println("解析locale映射文件内容为空");
            return;
        }

        excel2String(projectDir, excelDir, medal2LocaleMap);
    }

    private void excel2String(File projectDir, File excelDir, Map<String, String> medal2Locale) {
        File[] excelFiles = excelDir.listFiles((dir, name) -> name.endsWith("xls"));
        Map<String, File> excelNameMap = new HashMap<>();
        if (excelFiles != null) {
            for (File file : excelFiles) {
                excelNameMap.put(file.getName(), file);
            }
        }
        if (excelNameMap.isEmpty()) {
            System.out.println(excelDir.getAbsoluteFile() + " 没有xls文件");
            return;
        }
        medal2Locale.forEach((xls, localePath) -> {
            File excelFile = excelNameMap.get(xls);
            if (excelFile == null) {
                System.out.println("[ " + xls + "] 找不到, excel路径: " + excelDir.getAbsolutePath());
                return;
            }
            Map<String, List<ElementBean>> excelDatas = parseExcel(excelFile);
            if (excelDatas == null) {
                System.out.println("[ " + xls + "] 解析失败");
                return;
            }
            System.out.println(excelDatas);
            converExcel2Json(excelDatas, localePath);
        });
    }

    private void converExcel2Json(Map<String, List<ElementBean>> excelDatas, String localePath) {
        excelDatas.forEach((languageCode, elementBeans) -> {
            String code = languageCode.toLowerCase();
            File localeFile = new File(localePath + File.separator + code + ".json");
            if (localeFile.exists()) {
                //已存在,需要合并更新
                replaceAndUpdateLocaleJson(localeFile, elementBeans);
            } else {
                createLocaleJson(localeFile, elementBeans);
            }
        });
    }

    private void replaceAndUpdateLocaleJson(File localeFile, List<ElementBean> newContent) {
        //读取旧的翻译
        Map<String, String> contentMap = new HashMap<>();
        if (localeFile.exists()) {
            String json = null;
            try {
                json = FileUtils.readFileToString(localeFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
            }
            if (json != null && !json.isEmpty()) {
                Map<String, String> old = gson.fromJson(json, new TypeToken<LinkedHashMap<String, String>>() {
                }.getType());
                contentMap.putAll(old);
            }
        }
        //合并翻译
//        Map<String, String> newContentMap = new HashMap<>();
//        for (ElementBean elementBean : newContent) {
//            newContentMap.put(elementBean.getKey(), elementBean.getValue());
//        }
        for (ElementBean elementBean : newContent) {
            contentMap.put(elementBean.getKey(), elementBean.getValue());
        }
        saveLocaleFile(localeFile, contentMap);
    }

    private void createLocaleJson(File localeFile, List<ElementBean> content) {
        try {
            FileUtils.forceDelete(localeFile);
        } catch (IOException e) {
        }
        try {
            localeFile.createNewFile();
        } catch (IOException e) {
        }
        Map<String, String> contentMap = new HashMap<>();
        for (ElementBean elementBean : content) {
            contentMap.put(elementBean.getKey(), elementBean.getValue());
        }
        saveLocaleFile(localeFile, contentMap);
    }

    private void saveLocaleFile(File localeFile, Map<String, String> contentMap) {
        String jsonStr = gson.toJson(contentMap);
        String json = toPrettyFormat(jsonStr);
        try {
            FileUtils.write(localeFile, json, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
        }
    }

    private String toPrettyFormat(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }

    private Map<String, List<ElementBean>> parseExcel(File excelFile) {
        Workbook wb = null;
        //根据文件后缀（xls/xlsx）进行判断
        try {
            wb = new HSSFWorkbook(Files.newInputStream(excelFile.toPath()));
        } catch (Exception e) {
            System.out.println(excelFile.getAbsolutePath() + " 打开失败");
            e.printStackTrace();
            return null;
        }
        boolean isForceReplace = true;
        Sheet sheet = wb.getSheet("Sheet0");
        return ExcelUtil.parseExcelForMap2(sheet);
    }

    private Map<String, String> getMedal2Locale(File medal2LocaleFile) {
        //读取模块的映射文件
        String json = "";
        try {
            json = FileUtils.readFileToString(medal2LocaleFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
        }
        if (json == null || json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
