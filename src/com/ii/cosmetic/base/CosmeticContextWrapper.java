
package com.ii.cosmetic.base;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.text.TextUtils;

import com.ii.cosmetic.bean.Cosmetic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 重写Context，代替系统生成的，使程序可加载自己设置的Resources，从而达到换肤的效果 此方法只是用与XML的布局
 */
public class CosmeticContextWrapper extends ContextWrapper {

    /**
     * res资源
     */
    private Resources mResources;

    private Theme mTheme;

    /**
     * 皮肤包文件
     */
    private Cosmetic mSkin;

    /**
     * 设置皮肤
     * 
     * @param skin
     */
    public void setSkinName(Cosmetic skin) {
        this.mSkin = skin;
    }

    /**
     * 获取皮肤
     * 
     * @return
     */
    public Cosmetic getSkin() {
        return mSkin;
    }

    /**
     * 重置Resources（重新生成Resources）
     */
    public void reset() {
        mResources = null;
        getResources();
    }

    public CosmeticContextWrapper(Context base) {
        super(base);
    }

    /**
     * 生成Resources，用于替换应用原有的Resources
     */
    @Override
    public Resources getResources() {
        if (mResources == null) {
            if (mSkin == null || TextUtils.isEmpty(mSkin.mPath)) {
                //没有设置皮肤使用应用本身的Resources
                return getBaseContext().getResources();
            }
            AssetManager am = getSystemAssetManager();
            int cookie = addAssetPath(am, mSkin.mPath);
            if (cookie == 0) {
                //没有设置皮肤使用应用本身的Resources
                return getBaseContext().getResources();
            } else {
                //生成相应皮肤的Resources
                mResources = new Resources(am, getBaseContext().getResources().getDisplayMetrics(),
                        getBaseContext().getResources().getConfiguration());
                //mResources = new EnvSkinResourcesWrapper(getBaseContext().getResources(), am);
            }
        }
        return mResources;
    }

    @Override
    public Theme getTheme() {
        if (mTheme == null) {
            mTheme = getResources().newTheme();
            mTheme.setTo(getBaseContext().getTheme());
        }
        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {

        return super.getSystemService(name);
    }

    /**
     * Use reflection to create a default AssetManager
     * 
     * @return the system asset manager
     */
    private static AssetManager getSystemAssetManager() {
        Class<AssetManager> clazz = AssetManager.class;
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Use reflection to add resources path to asset manager.
     *
     * @param file the zip file or directory
     */
    private int addAssetPath(AssetManager am, String file) {
        Class<AssetManager> clazz = AssetManager.class;
        try {
            Method m = clazz.getDeclaredMethod("addAssetPath", String.class);
            return (Integer) m.invoke(am, file);
        } catch (NoSuchMethodException e) {

        } catch (InvocationTargetException e) {

        } catch (IllegalAccessException e) {

        }
        return 0;
    }
}
