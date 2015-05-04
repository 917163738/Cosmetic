
package com.ii.cosmetic.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.ii.cosmetic.base.CosmeticConfig;
import com.ii.cosmetic.bean.Cosmetic;
import com.ii.cosmetic.listener.OnCosmeticListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CosmeticManager {

    private SharedPreferences mSp;

    private static CosmeticManager sCosmeticManager;

    //保存注册的所有OnCosmeticListener
    private final CopyOnWriteArrayList<OnCosmeticListener> mOnCosmeticListeners = new CopyOnWriteArrayList<OnCosmeticListener>();

    private Context mContext;

    /**
     * 当前程序支持皮肤的最低版本
     */
    private int mMinVersion = -1;

    /**
     * 当前支持皮肤包最高版本
     */
    private int mTargetVersion = -1;

    private CosmeticManager(Context context) {
        mContext = context;
        mSp = context.getSharedPreferences(CosmeticConfig.SP_COMSETICS, Activity.MODE_PRIVATE);
        getVersion(context);
        Log.d("SkinManager", "minVersion:" + mMinVersion + ",targetVersion:" + mTargetVersion);
    }

    public synchronized static CosmeticManager getInstance(Context context) {
        if (sCosmeticManager == null) {
            sCosmeticManager = new CosmeticManager(context);
        }
        return sCosmeticManager;
    }

    /**
     * 获得支持最低的皮肤包版本
     * 
     * @return -1为没有获取到版本
     */
    public int getMinVersion() {
        return mMinVersion;
    }

    /**
     * 获得当前支持的皮肤包版本
     * 
     * @return -1为没有获取到版本
     */
    public int getTargetVersion() {
        return mTargetVersion;
    }

    /**
     * 检测皮肤包是否在支持版本范围内
     * 
     * @param cosmetic 皮肤包
     * @return true支持，false不支持
     */
    public boolean isCosmeticAvailable(Cosmetic cosmetic) {
        return isCosmeticAvailable(cosmetic.mVersion);
    }

    /**
     * 检测皮肤包是否在支持版本范围内
     * 
     * @param skinTargetVersion 皮肤包版本
     * @return true支持，false不支持
     */
    public boolean isCosmeticAvailable(int skinTargetVersion) {
        if (skinTargetVersion >= mMinVersion && skinTargetVersion <= mTargetVersion) {
            return true;
        }
        return false;
    }

    /**
     * 改变皮肤
     * 
     * @param cosmetic 皮肤包
     */
    public void changeSkin(Cosmetic cosmetic) {
        this.setCurCosmetic(cosmetic);
        for (OnCosmeticListener listener : mOnCosmeticListeners) {
            if (listener.isChangeSkin(cosmetic)) {
                listener.onChangeSkin(mContext, cosmetic);
            }
        }
    }

    /**
     * 增加OnSkinListener
     * 
     * @param onCosmeticListener
     */
    public void addOnCosmeticListener(OnCosmeticListener onCosmeticListener) {
        if (onCosmeticListener != null) {
            mOnCosmeticListeners.add(onCosmeticListener);
        }
    }

    /**
     * 移除onSkinListener
     * 
     * @param onCosmeticListener
     */
    public void removeOnCosmeticListener(OnCosmeticListener onCosmeticListener) {
        if (onCosmeticListener != null) {
            mOnCosmeticListeners.remove(onCosmeticListener);
        }
    }

    /**
     * 获取当前使用的皮肤路径
     * 
     * @return 皮肤包skin
     */
    public Cosmetic getCurCosmetic() {
        String cosmeticPath = mSp.getString(CosmeticConfig.SP_COMSETICS_PATH, "");
        if (TextUtils.isEmpty(cosmeticPath)) {
            return null;
        }
        File file = new File(cosmeticPath);
        if (!file.exists()) {
            return null;
        }
        Cosmetic skin = new Cosmetic();
        skin.mName = file.getName().replace(CosmeticConfig.FIEL_COMSETICS_SUFFIX, "");
        skin.mPath = cosmeticPath;
        skin.mFileSize = file.length();
        return skin;
    }

    /**
     * 保存当前使用的皮肤路径
     * 
     * @param cosmetic 皮肤包
     */
    public void setCurCosmetic(Cosmetic cosmetic) {
        mSp.edit()
                .putString(CosmeticConfig.SP_COMSETICS_PATH, cosmetic == null ? "" : cosmetic.mPath)
                .commit();
    }

    /**
     * 获取skinfolderPath文件夹下的所有皮肤包
     * 
     * @param skinfolderPath 皮肤包所在文件夹路径
     * @return 皮肤包集合
     */
    public Cosmetic[] getCosmetics(String skinfolderPath) {
        if (TextUtils.isEmpty(skinfolderPath)) {
            return null;
        }
        return getCosmetics(new File(skinfolderPath));
    }

    /**
     * 获取skinfolder文件夹下的所有皮肤包
     * 
     * @param cosmeticfolder 皮肤包所在文件夹
     * @return 皮肤包集合
     */
    public Cosmetic[] getCosmetics(final File cosmeticfolder) {
        if (cosmeticfolder == null || !cosmeticfolder.exists()) {
            return null;
        }
        File[] skinFiles = cosmeticfolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                //获取符合的皮肤包
                return filename.endsWith(CosmeticConfig.FIEL_COMSETICS_SUFFIX);
            }
        });
        if (skinFiles == null || skinFiles.length == 0) {
            return null;
        }
        Cosmetic[] skins = new Cosmetic[skinFiles.length];
        for (int i = 0; i < skins.length; i++) {
            skins[i] = new Cosmetic();
            //去掉文件后缀名
            skins[i].mName = skinFiles[i].getName().replace(CosmeticConfig.FIEL_COMSETICS_SUFFIX,
                    "");
            skins[i].mPath = skinFiles[i].getAbsolutePath();
            skins[i].mFileSize = skinFiles[i].length();
            skins[i].mVersion = getCosmeticVersion(cosmeticfolder + File.separator + skins[i].mName
                    + CosmeticConfig.FIEL_COMSETICS_SUFFIX);
            skins[i].mIcPath = zipCosmeticIcon(skins[i].mPath);//解压其中的皮肤包icon图片
        }

        return skins;
    }

    /**
     * 获得应用支持的皮肤包版本
     * 
     * @param context
     */
    private void getVersion(Context context) {
        InputStream stream;
        try {
            stream = context.getAssets().open(CosmeticConfig.APPLICATION_INI);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() > 0 && !line.startsWith("#")) {
                    line = line.trim();
                    if (line.startsWith(CosmeticConfig.MIN_VERSION)) {
                        line = line.substring(line.lastIndexOf("=") + 1, line.length());
                        mMinVersion = Integer.valueOf(line);
                    } else if (line.startsWith(CosmeticConfig.TARGET_VERSION)) {
                        line = line.substring(line.lastIndexOf("=") + 1, line.length());
                        mTargetVersion = Integer.valueOf(line);
                    }
                }
            }
            in.close();
            stream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 得到皮肤包的版本
     * 
     * @param cosmeticPath 皮肤包路径
     * @return 此皮肤包的版本，-1获取版本失败，其他获取成功
     */
    private int getCosmeticVersion(String cosmeticPath) {
        int targetVersion = -1;
        try {
            ZipFile zipfile = new ZipFile(cosmeticPath);
            ZipEntry entry = zipfile.getEntry(CosmeticConfig.COMSETICS_INI);
            if (entry != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        zipfile.getInputStream(entry)));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.length() > 0 && !line.startsWith("#")) {
                        line = line.trim();
                        if (line.startsWith(CosmeticConfig.TARGET_VERSION)) {
                            line = line.substring(line.lastIndexOf("=") + 1, line.length());
                            targetVersion = Integer.valueOf(line);
                            Log.d("SkinManager", "targetVersion:" + targetVersion);
                        }
                    }
                }
                in.close();
            }
            zipfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetVersion;
    }

    /**
     * 解压皮肤压缩包里的icon图片
     * 
     * @param cosmeticPath skinPath 皮肤包路径
     * @return 返回icon解压出的路径，路径为null表示解压失败
     * @throws IOException
     */
    private String zipCosmeticIcon(String cosmeticPath) {
        try {
            ZipFile zipfile = new ZipFile(cosmeticPath);
            @SuppressWarnings("unchecked")
            Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipfile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                String icPath = entry.getName();
                if (icPath.startsWith(CosmeticConfig.COMSETICS_ICON)) {
                    //获取icon图片后缀
                    icPath = icPath.substring(icPath.lastIndexOf("."), icPath.length());
                    //得到保存icon图片的路径
                    icPath = cosmeticPath.replace(CosmeticConfig.FIEL_COMSETICS_SUFFIX, icPath);
                    File icFile = new File(icPath);
                    if (!icFile.exists()) {
                        BufferedInputStream is = new BufferedInputStream(
                                zipfile.getInputStream(entry));
                        int size = (int) entry.getSize();
                        if (size > 100) {
                            size = 100;
                        }
                        byte data[] = new byte[size];
                        BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(
                                icFile), size);
                        int count;
                        while ((count = is.read(data, 0, size)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                    zipfile.close();
                    return icPath;
                }
            }
            zipfile.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
