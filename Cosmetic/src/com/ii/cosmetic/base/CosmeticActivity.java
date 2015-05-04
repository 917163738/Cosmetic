
package com.ii.cosmetic.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.ii.cosmetic.bean.Cosmetic;
import com.ii.cosmetic.listener.OnCosmeticListener;
import com.ii.cosmetic.manager.CosmeticManager;

public abstract class CosmeticActivity extends Activity implements OnCosmeticListener {

    private Context context;

    //皮肤包管理器
    private CosmeticManager mSkinManager;

    /**
     * 设置自己的Context
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        if (context == null) {
            context = onCreateContext(newBase);
            if (context != null) {
                mSkinManager = CosmeticManager.getInstance(context);
                mSkinManager.addOnCosmeticListener(this);
                onCreateResources(context, mSkinManager.getCurCosmetic());
            }
        }
        super.attachBaseContext(context == null ? newBase : context);
    }

    /**
     * 返回应用原有的Context
     */
    @Override
    public Context getBaseContext() {
        Context context = super.getBaseContext();
        if (context instanceof CosmeticContextWrapper) {
            context = ((CosmeticContextWrapper) context).getBaseContext();
        }
        return context;
    }

    @Override
    public Resources getResources() {
        return context != null ? context.getResources() : super.getResources();
    }

    @Override
    protected void onDestroy() {
        if (mSkinManager != null) {
            mSkinManager.removeOnCosmeticListener(this);
        }
        super.onDestroy();
    }

    /**
     * 创建自己的Context
     * 
     * @param newBase
     * @return
     */
    public Context onCreateContext(Context newBase) {
        return new CosmeticContextWrapper(newBase);
    }

    /**
     * 创建Resources
     * 
     * @param context
     * @param skinName
     */
    public void onCreateResources(Context context, Cosmetic skin) {
        if (context instanceof CosmeticContextWrapper && isChangeSkin(skin)) {
            ((CosmeticContextWrapper) context).setSkinName(skin);
            ((CosmeticContextWrapper) context).reset();
            onInitSkin(context, skin);
        }
    }

    /**
     * 验证此皮肤包是否符合此界面，每个需要换肤的界面都需覆盖
     * 
     * @param skin 皮肤包
     * @return true此皮肤包可以在此界面上使用,false则不符合
     */
    @Override
    public boolean isChangeSkin(Cosmetic skin) {
        if (skin == null) {
            return true;
        }
        return false;
    }

}
