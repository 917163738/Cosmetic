
package com.ii.cosmetic.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ii.cosmetic.R;
import com.ii.cosmetic.base.CosmeticActivity;
import com.ii.cosmetic.base.CosmeticConfig;
import com.ii.cosmetic.bean.Cosmetic;
import com.ii.cosmetic.manager.CosmeticManager;

import java.io.File;

/**
 * Demo
 * 
 * @author hao.zhong
 */
public class DemoActivity extends CosmeticActivity {

    private ListView listview;

    private CosmeticManager mCosmeticManager;

    private Cosmetic[] mCosmetic;

    private TextView mCurCosmeticTV;

    private String mStrCurCosmeticFormat;

    private String mStrCurCosmeticVersionFormat;

    private Cosmetic mCurCosmetic;

    private String mStrDefaultCosmetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrCurCosmeticFormat = getResources().getString(R.string.cur_skin);
        mStrCurCosmeticVersionFormat = getResources().getString(R.string.cur_skin_version);
        mStrDefaultCosmetic = getResources().getString(R.string.default_skin);
        mCosmeticManager = CosmeticManager.getInstance(this);
        mCosmetic = mCosmeticManager.getCosmetics(CosmeticConfig.URL_SDCARD + File.separator
                + getString(R.string.app_name));
        setContentView(R.layout.demo_activity);
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(new SkinAdapter(mCosmetic));
        mCurCosmeticTV = (TextView) findViewById(R.id.tv_skin);
        mCurCosmetic = mCosmeticManager.getCurCosmetic();
        mCurCosmeticTV.setText(String.format(mStrCurCosmeticFormat,
                mCurCosmetic == null ? mStrDefaultCosmetic : mCurCosmetic.mName));
        ((TextView) findViewById(R.id.tv_skin_version)).setText(String.format(
                mStrCurCosmeticVersionFormat, mCosmeticManager.getMinVersion(),
                mCosmeticManager.getTargetVersion()));
    }

    @Override
    public void onInitSkin(Context context, Cosmetic skin) {

    }

    @Override
    public boolean isChangeSkin(Cosmetic skin) {

        return false;
    }

    @Override
    public void onChangeSkin(Context context, Cosmetic skin) {
        this.recreate();
    }

    class SkinAdapter extends BaseAdapter {
        Cosmetic[] mSkins;

        public SkinAdapter(Cosmetic[] skins) {
            mSkins = skins;
        }

        @Override
        public int getCount() {
            return (mSkins != null ? mSkins.length : 0) + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                return null;
            }
            return mSkins != null ? mSkins[position - 1] : null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(DemoActivity.this, R.layout.cosmetic_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mBtnUsed = (Button) convertView.findViewById(R.id.btn_used);
                viewHolder.mIvIcSkin = (ImageView) convertView.findViewById(R.id.ic_skin);
                viewHolder.mTvSkinName = (TextView) convertView.findViewById(R.id.tv_skin_name);
                viewHolder.mTvSkinVersion = (TextView) convertView
                        .findViewById(R.id.tv_skin_version);
                convertView.setTag(convertView);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final Cosmetic skin = (Cosmetic) getItem(position);

            if (skin != null && !TextUtils.isEmpty(skin.mIcPath)) {
                Bitmap ic = BitmapFactory.decodeFile(skin.mIcPath);
                if (ic != null) {
                    Bitmap icOld = (Bitmap) viewHolder.mIvIcSkin.getTag();
                    viewHolder.mIvIcSkin.setImageBitmap(ic);
                    viewHolder.mIvIcSkin.setTag(ic);
                    if (icOld != null && !icOld.isRecycled()) {
                        icOld.recycle();
                    }
                }
            }
            viewHolder.mTvSkinName.setText(skin == null ? mStrDefaultCosmetic : skin.mName);
            viewHolder.mTvSkinVersion.setText("version:" + (skin == null ? "*" : skin.mVersion));
            viewHolder.mBtnUsed.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (skin == null || mCosmeticManager.isCosmeticAvailable(skin)) {
                        mCosmeticManager.changeSkin(skin);
                        mCurCosmeticTV.setText(String.format(mStrCurCosmeticFormat,
                                skin == null ? mStrDefaultCosmetic : skin.mName));
                    } else {
                        Toast.makeText(DemoActivity.this, "Version does not conform to the！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return convertView;
        }
    }

    class ViewHolder {
        ImageView mIvIcSkin;

        TextView mTvSkinVersion;

        TextView mTvSkinName;

        Button mBtnUsed;
    }
}
