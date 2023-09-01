package string2excel;

import com.google.gson.*;
import com.google.gson.reflect.*;
import data.*;
import org.apache.commons.io.*;
import util.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;

public class String2Excel {

    private static Gson gson = new Gson();
    private String projectPath;
    private String outputPath;

    private boolean isNewWeb;

    public String2Excel(String path, String output, boolean isNewWeb) {
        this.projectPath = path;
        this.outputPath = output;
        this.isNewWeb = isNewWeb;
    }

    public void execute() {
        if (projectPath == null || projectPath.isEmpty()) {
            System.out.println("项目路径不能为空");
            return;
        }
        File project = new File(this.projectPath);
        if (!project.exists()) {
            System.out.println("项目路径不存在");
            return;
        }
        if (!project.isDirectory()) {
            System.out.println("项目路径不是文件夹");
            return;
        }
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = this.projectPath + File.separator + "翻译";
        }
        File outputDir = new File(outputPath);
        try {
            FileUtils.forceDelete(outputDir);
        } catch (IOException e) {
        }
        try {
            FileUtils.forceMkdir(outputDir);
        } catch (IOException e) {
        }

        Map<File, File> stringPath = new HashMap<>();
        if (isNewWeb) {
            //新Web项目
            stringPath.putAll(WenextWebMono.findStringPath(project));
        } else {
            //旧Web项目
            stringPath.putAll(WepartyWeb.findStringPath(project));
        }
        string2Excel(stringPath, outputDir);
    }

    public static void string2Excel(Map<File, File> stringPath, File output) {
        if (stringPath.isEmpty()) {
            System.out.println("没有内容需要转化的");
            return;
        }

        //统计中文字数
        List<CharCountData> chineseCountList = new ArrayList<>();

        //导出
        stringPath.forEach((modelDir, localDir) -> {
            File[] jsonsFile = localDir.listFiles((dir, name) -> name.endsWith("json"));
            if (jsonsFile == null || jsonsFile.length == 0) {
                return;
            }
            //<语言, <key, value>>
            Map<String, List<MultiLanguageBean>> languages = new HashMap<>();
            for (File jsonFile : jsonsFile) {
                String name = jsonFile.getName();
                String languageCode = name.substring(0, name.length() - 5);

                String json = null;
                try {
                    json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    json = "";
                }
                if (json == null || json.isEmpty()) {
                    continue;
                }
                LinkedHashMap<String, String> content = gson.fromJson(json, new TypeToken<LinkedHashMap<String, String>>() {
                }.getType());

                List<MultiLanguageBean> languageBeans = new ArrayList<>();
                Iterator<String> iterator = content.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String value = content.get(key);
                    MultiLanguageBean bean = new MultiLanguageBean();
                    bean.setLanguage(languageCode);
                    bean.setLanguageCode(languageCode);
                    bean.setName(key);
                    bean.setValue(value);
                    languageBeans.add(bean);
                }
                languages.put(languageCode, languageBeans);
            }

            File outputFile = new File(output.getAbsoluteFile() + File.separator + modelDir.getName() + ".xls");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ExcelUtil.generateExcelFile(outputFile, languages);

            CharCountData chineseCharCount = collectChineseInfo(modelDir.getName(), languages);
            if (chineseCharCount.count > 0) {
                chineseCountList.add(chineseCharCount);
            }
        });
        saveChineseInfo(chineseCountList, output.getAbsolutePath());
    }

    private static CharCountData collectChineseInfo(String modelName, Map<String, List<MultiLanguageBean>> languages) {
        if (languages == null || languages.isEmpty()) {
            return new CharCountData(modelName, 0);
        }
        //统计中文字数
        List<MultiLanguageBean> zh = languages.get("zh");
        int count = 0;
        if (zh != null) {
            for (MultiLanguageBean bean : zh) {
                String value = bean.getValue();
                count += chineseCount(value);
            }
        }
        return new CharCountData(modelName, count);
    }

    /**
     * 计算中文字数(不包含标点符号)
     */
    private static int chineseCount(String content) {
        if (content == null || content.length() == 0) {
            return 0;
        }
        int count = 0;
        Set<Character> withoutC = new HashSet<>();
        withoutC.add('，');
        withoutC.add('。');
        withoutC.add('！');
        withoutC.add('“');
        withoutC.add('”');
        withoutC.add('？');
        char[] c = content.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (withoutC.contains(c[i])) {
                continue;
            }
            String len = Integer.toBinaryString(c[i]);
            if (len.length() > 8) {
                count++;
            }
        }
        return count;
    }

    private static void saveChineseInfo(List<CharCountData> lines, String saveFileDir) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        int allCount = 0;
        for (CharCountData line : lines) {
            allCount += line.count;
        }
        lines.add(0, new CharCountData("全部", allCount));
        List<String> strLines = lines.stream().map(CharCountData::toString).collect(Collectors.toList());
        File output = new File(saveFileDir, "中文字数统计(去除标点符号).txt");
        //输出统计结果
        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            org.apache.commons.io.FileUtils.writeLines(output, strLines);
        } catch (IOException e) {
        }
    }


}
