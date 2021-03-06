
package com.skin.verification.tool;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.FileDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author fei.bao
 */
public class CosmeticVerificationTool {

    public static final String CLASS_ = "public static final class";

    public static final String INT_ = "public static final int";

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            printInfo();
            return;
        }
        if (args.length == 1) {
            printInfo();
            return;
        }

        if (isCusmetictool(args)) {//判断是否可以执行id匹配验证
            if (isFile(args[1]) && isCosmeticFile(args[2])) {
                int type = isRFileOrApk(args[1]);
                if (Cusmetictool(args[1], args[2], type)) {
                    System.out.println("结果：可用");
                    return;
                }
                System.out.println("结果：不可用");
            }
        } else {
            printInfo();
            return;
        }
    }

    /**
     * 验证cmd输入的参数是否可以执行匹配ID验证
     * 
     * @param args cmd输入参数
     * @return
     */
    public static boolean isCusmetictool(String[] args) {
        if (args[0].toLowerCase().equals("v")) {
            if (args.length == 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是R.java还是apk文件
     * 
     * @param filePath 文件路径
     * @return -1错误，0 R.java文件，1 apk文件
     */
    public static int isRFileOrApk(String filePath) {
        File file = new File(filePath);
        if (file.getName().equals("R.java")) {
            return 0;
        } else if (file.getName().endsWith(".apk")) {
            return 1;
        }
        System.out.println("文件\"" + filePath + "\"不符合，必须是apk或R.java文件！");
        return -1;
    }

    /**
     * 判断是否是标准文件
     * 
     * @param filePath 文件路径
     * @return true 是标准文件， false 不是标准文件
     */
    public static boolean isFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                return true;
            }
            System.out.println("文件\"" + filePath + "\"不是标准文件！");
            return false;
        }
        System.out.println("文件\"" + filePath + "\"不存在或不可读！");
        return false;
    }

    /**
     * 是否是皮肤包文件
     * 
     * @param filePath 皮肤包路径
     * @return true是皮肤包文件，false不是或文件不存在不可读
     */
    public static boolean isCosmeticFile(String filePath) {
        if (isFile(filePath)) {
            if (new File(filePath).getName().endsWith(".cosm")) {
                return true;
            } else {
                System.out.println("文件\"" + filePath + "\"不符合，必须是.cosm后缀的皮肤包文件！");
            }
        }
        return false;
    }

    /**
     * 打印信息
     */
    public static void printInfo() {
        System.out.println("Cusmetictool v1.0 -验证皮肤包是否可使用于当前应用版本");
        System.out.println("Copyright 2015 11-team");
        System.out.println("Updated by baofei <wysbaofei@gmail.com>\n");
        System.out.println("suage:Cusmetictool");
        System.out.println("-version,--version    prints the version then exits\n");

        System.out.println("usage: Cusmetictool v[verification] <file_R> <file_cosmetic>");
        System.out.println("<file_R> 项目R.java文件路径");
        System.out.println("<file_cosmetic> 皮肤包路径\n");

        System.out.println("usage: Cusmetictool v[verification] <file_APK> <file_cosmetic>");
        System.out.println("<file_APK> apk文件路径");
        System.out.println("<file_cosmetic> 皮肤包路径");
    }

    /**
     * 执行id匹配验证
     * 
     * @param r R.java文件路径或apk文件路径
     * @param cosmeticPath 皮肤包路径
     * @param type 匹配类型， 0 R.java文件匹配， 1 apk文件匹配
     * @return true匹配成功，false匹配失败
     */
    public static boolean Cusmetictool(String r, String cosmeticPath, int type) {
        if (type == -1) {
            System.out.println("未知错误...");
            return false;
        }
        try {
            System.out.println("verification...");
            HashMap<String, HashMap<String, Integer>> rMap;
            if (type == 0) {
                rMap = readRFile(r);
            } else {
                //rMap = readApkFile(r);
                rMap = getIds(r, "apk");
                if (rMap == null) {
                    return false;
                }
            }
            HashMap<String, HashMap<String, Integer>> cosmeticMap = getIds(cosmeticPath, "皮肤包");
            Iterator rIter = rMap.entrySet().iterator();
            while (rIter.hasNext()) {
                Map.Entry entry = (Map.Entry) rIter.next();
                if (!verification((String) entry.getKey(), rMap, cosmeticMap)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 验证resId
     * 
     * @param key id类型
     * @param rMap apk或R.java文件id集合
     * @param cosmeticMap 皮肤包id集合
     * @return true匹配成功， false匹配失败
     */
    public static boolean verification(String key, HashMap<String, HashMap<String, Integer>> rMap,
            HashMap<String, HashMap<String, Integer>> cosmeticMap) {
        System.out.print("verification " + key + ":");
        HashMap<String, Integer> r = rMap.get(key);
        HashMap<String, Integer> cosmetic = cosmeticMap.get(key);
        if (r == null && cosmetic == null) {
            System.out.println("匹配;");
            return true;
        } else if (r == null && cosmetic != null && cosmetic.size() == 0) {
            System.out.println("匹配;");
            return true;
        } else if (r != null && cosmetic == null && r.size() == 0) {
            System.out.println("匹配;");
            return true;
        } else if (r != null && cosmetic != null) {
            if (r.size() != cosmetic.size()) {
                System.out.println("不匹配;原因：resId数目不对");
                return false;
            }
            for (Entry<String, Integer> pair : r.entrySet()) {
                if (!cosmetic.containsKey(pair.getKey())
                        || !cosmetic.get(pair.getKey()).equals(pair.getValue())) {
                    System.out.print("不匹配;");
                    if (!cosmetic.containsKey(pair.getKey())) {
                        System.out.println("皮肤包中不存在" + pair.getKey());
                    } else {
                        System.out.println("皮肤包中" + pair.getKey() + "值不相等");
                    }
                    return false;
                }
            }
            System.out.println("匹配;");
            return true;
        }
        System.out.println("不匹配;");
        return false;
    }

    /**
     * 提取R.java文件的id
     * 
     * @param r R.java文件的路径
     * @return id集合
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, Integer>> readRFile(String r) throws IOException {
        System.out.println("提取\"" + r + "\"文件");
        File file = new File(r);
        HashMap<String, HashMap<String, Integer>> rMap = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, Integer> intMap = null;
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        boolean isNone = false;
        boolean isClass = false;
        String className = null;
        while ((line = in.readLine()) != null) {
            if (line.length() > 0) {
                if (line.trim().startsWith("/*") || line.trim().startsWith("//")) {
                    if (line.trim().startsWith("/*")) {
                        isNone = true;
                    }
                    continue;
                } else if (isNone) {
                    if (line.trim().endsWith("*/")) {
                        isNone = false;
                    }
                    continue;
                } else {
                    line = line.trim().replaceAll("\\s+", " ");
                    //System.out.println(line);
                    if (line.startsWith(CLASS_)) {
                        className = line.substring(line.indexOf("class "), line.indexOf(" {"))
                                .replace("class ", "");
                        isClass = true;
                        intMap = new HashMap<String, Integer>();
                        //rMap.put(key, value)
                    } else if (isClass) {
                        if (line.startsWith(INT_)) {
                            line = line.substring(line.lastIndexOf(" "), line.indexOf(";")).trim();
                            String[] values = line.split("=");
                            intMap.put(values[0], values[1].hashCode());
                        }
                        if (line.endsWith("}")) {
                            rMap.put(className, intMap);
                            isClass = false;
                        }
                    }
                }
            }
        }
        in.close();
        return rMap;
    }

    /**
     * 提取apk中的id
     * 
     * @param apkPath apk路径
     * @return
     */
    public static HashMap<String, HashMap<String, Integer>> getIds(String apkPath, String tips) {
        System.out.println("提取" + tips + "文件ids...");
        HashMap<String, HashMap<String, Integer>> rMap = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, Integer> intMap = null;
        ApkDecoder apk = new ApkDecoder();
        apk.setApkFile(new File(apkPath));
        try {
            Directory out = new FileDirectory(new File("."));
            for (ResPackage pkg : apk.getResTable().listMainPackages()) {
                //generatePublicXml(pkg, out, xmlSerializer);
                for (ResResSpec spec : pkg.listResSpecs()) {
                    String type = spec.getType().getName();
                    intMap = rMap.get(type);
                    if (intMap == null) {
                        intMap = new HashMap<String, Integer>();
                        rMap.put(type, intMap);
                    }
                    String name = spec.getName();
                    String id = String.format("0x%08x", spec.getId().id);
                    intMap.put(name, id.hashCode());
                }

            }
            System.out.println("提取" + tips + "文件ids成功");
            return rMap;
        } catch (AndrolibException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DirectoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("提取apk文件id失败");
        return rMap;
    }

}
