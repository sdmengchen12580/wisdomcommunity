package com.example.mylibrary.httpUtils.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylibrary.httpUtils.utils.AppUtils;
import com.example.mylibrary.httpUtils.utils.DensityUtil;
import com.example.mylibrary.httpUtils.utils.LogUtils;
import com.example.mylibrary.httpUtils.utils.ViewManager;
import com.example.mylibrary.httpUtils.view.CustomDialog;
import com.jaeger.library.StatusBarUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {

    private CustomDialog customDialog;
    protected int mNoPermissionIndex = 0;
    protected final int PERMISSION_REQUEST_CODE1 = 0x1;
    protected final int PERMISSION_REQUEST_CODE2 = 0x2;
    protected final int PERMISSION_REQUEST_CODE3 = 0x3;
    protected long lastClickTime;//按钮短时多触
    private long firstClick;//2次退出
    protected String GAODE_NAME = "com.autonavi.minimap";
    protected String BAIDU_NAME = "com.baidu.BaiduMap";
    protected boolean isStatusBarTextBlackColor = true;

    //相机+存储
    protected final String[] permissionManifestCamera = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //存储
    protected final String[] permissionManifestGetPic = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //获取手机信息+相机+存储+定位
    protected final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public abstract int getLayoutId();

    public abstract void initViews(Bundle savedInstanceState);

    public abstract void initClick();

    public abstract boolean isStatusBarTextBlackColor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.setLightMode(this);
        isStatusBarTextBlackColor = isStatusBarTextBlackColor();
        if (isStatusBarTextBlackColor) { //黑色
            StatusBarUtil.setLightMode(this);
        } else { //白色
            StatusBarUtil.setDarkMode(this);
        }
        super.onCreate(savedInstanceState);
        AppUtils.setDefaultDisplay(this); //字体大小影响
        ViewManager.getInstance().addActivity(this); //栈管理
        setContentView(getLayoutId()); //绑定
        initViews(savedInstanceState);
        initClick();
    }

    //字体不随app改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration newConfig = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (resources != null && newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            if (Build.VERSION.SDK_INT >= 17) {
                Context configurationContext = createConfigurationContext(newConfig);
                resources = configurationContext.getResources();
                displayMetrics.scaledDensity = displayMetrics.density * newConfig.fontScale;
            } else {
                resources.updateConfiguration(newConfig, displayMetrics);
            }
        }
        return resources;
    }

    protected void showLoadingDialog(String hint) {
        if (customDialog == null) {
            customDialog = new CustomDialog(this, hint);
        }
        customDialog.setContent(hint);
        customDialog.show();
    }

    protected void hideLoadingDialog() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (customDialog != null) {
                        if (BaseActivity.this.isDestroyed() || BaseActivity.this.isFinishing()) {
                            return;
                        }
                        if (customDialog.isShowing()) {
                            customDialog.dismiss();  //fixme 需要查看所有，延时取消
                        }
                    }
                }
            }, 500);
        } catch (Exception e) {
        }
    }

    protected void hideLoadingDialogNodelay() {
        try {
            if (customDialog != null) {
                if (customDialog.isShowing()) {
                    customDialog.dismiss();  //fixme 不延时的操作
                }
            }
        } catch (Exception e) {
        }
    }

    protected boolean isVisBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = recyclerView.getScrollState();
        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE) {
//            LogUtils.logE("测试刷新: ", "刷新");
            return true;
        } else {
//            LogUtils.logE("测试刷新: ", "不刷新");
            return false;
        }
    }

    //字体加粗
    public void setTextBold(TextView textView, boolean isBold) {
        try {
            if (textView != null) {
                Paint paint = textView.getPaint();
                if (paint != null) {
                    paint.setFakeBoldText(isBold);
                }
                if (isBold) {
                    paint.setTextSize(DensityUtil.dip2px(this, 15));
                } else {
                    paint.setTextSize(DensityUtil.dip2px(this, 14));
                }
                //设置后一定要刷新，否则无效
                textView.postInvalidate();
            }
        } catch (Throwable var3) {
        }
    }

    //两数相乘，防止精度丢失
    public double formatNum(String date1, String date2, boolean isX) {
        BigDecimal b = new BigDecimal(date1);
        BigDecimal c = new BigDecimal(date2);
        String money = String.valueOf(isX ? b.multiply(c) : b.divide(c, 3, BigDecimal.ROUND_DOWN));//保留3位有效小数
        return Double.parseDouble(money);
    }

    //检查手机上是否安装了指定的软件
    protected boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    // 判定是否需要隐藏
    protected boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            int dp10 = DensityUtil.dip2px(this, 10);
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            //在范围内
//            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
            if (ev.getY() > (top - dp10) && ev.getY() < (bottom + dp10)) {//上下边框+10dp
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void onAppExit() {
        if (System.currentTimeMillis() - this.firstClick > 2000L) {
            this.firstClick = System.currentTimeMillis();
            showToast("再按一次退出");
            return;
        }
        ViewManager.getInstance().finishActivity();
    }

    //检测是否有权限
    protected boolean permissionCheck(int type) {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        String permission;
        if (type == 1) {
            for (int i = 0; i < permissionManifestCamera.length; i++) {
                permission = permissionManifestCamera[i];
                mNoPermissionIndex = i;
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionCheck = PackageManager.PERMISSION_DENIED;
                }
            }
        } else if (type == 2) {
            for (int i = 0; i < permissionManifest.length; i++) {
                permission = permissionManifest[i];
                mNoPermissionIndex = i;
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionCheck = PackageManager.PERMISSION_DENIED;
                }
            }
        } else if (type == 3) {
            for (int i = 0; i < permissionManifestGetPic.length; i++) {
                permission = permissionManifestGetPic[i];
                mNoPermissionIndex = i;
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionCheck = PackageManager.PERMISSION_DENIED;
                }
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean isFastDoubleClick() {
        long time = SystemClock.uptimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    //toast
    protected void showToast(String tip) {
        Toast.makeText(this.getApplicationContext(), tip, Toast.LENGTH_LONG).show();
    }

    //changeAct
    protected void changeAct(Bundle bundle, Class<?> toClass) {
        Intent intent = new Intent();
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        intent.setClass(this, toClass);
        this.startActivity(intent);
    }

    //changeAct
    protected void changeAct(Bundle bundle, String actUrl) {
        Intent intent = new Intent(actUrl);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        this.startActivity(intent);
    }

    //设置edittext是否可编辑
    protected void setEdittextCanEdit(EditText editText, boolean canEdit) {
        if (canEdit) {
            editText.setFocusableInTouchMode(true);
            editText.setFocusable(true);
            editText.requestFocus();
            return;
        }
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    //隐藏软键盘
    public void hideKeyboard(EditText et_chat) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
    }

    //显示软键盘
    protected void showKeyboard(EditText et_chat) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_chat, InputMethodManager.SHOW_FORCED);
    }

    //获取软键盘高度
    protected void getSoftHeight(final LinearLayout ll_inputparent,
                                 final RelativeLayout parentLayout, final boolean needPlusBottomViewH) {
        final View myLayout = this.getWindow().getDecorView();
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int statusBarHeight;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                // 使用最外层布局填充，进行测算计算
                parentLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = myLayout.getRootView().getHeight();
                int heightDiff = screenHeight - (r.bottom - r.top);
                if (heightDiff > 100) {
                    // 如果超过100个像素，它可能是一个键盘。获取状态栏的高度
                    statusBarHeight = 0;
                }
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = BaseActivity.this.getResources().getDimensionPixelSize(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int realKeyboardHeight = heightDiff - statusBarHeight;
                LogUtils.logE("测试键盘", "keyboard height(单位像素) = " + realKeyboardHeight);
                if (realKeyboardHeight > 100) {
                    parentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (ll_inputparent.getLayoutParams() instanceof RelativeLayout.MarginLayoutParams) {
                        RelativeLayout.MarginLayoutParams p = (RelativeLayout.MarginLayoutParams) ll_inputparent.getLayoutParams();
                        if (needPlusBottomViewH) {
                            p.bottomMargin = realKeyboardHeight - DensityUtil.dip2px(BaseActivity.this, 49);
                        } else {
                            p.bottomMargin = realKeyboardHeight;
                        }
                        ll_inputparent.requestLayout();
                        ll_inputparent.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    protected void setViewLp(View view, int width, int height) {
        ViewGroup.LayoutParams vp = view.getLayoutParams();
        vp.width = width;
        vp.height = height;
        view.setLayoutParams(vp);
    }

    protected void setViewMargin(final View view, final int height, final double left,
                                 final double top, final double right) {
        view.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams vp = view.getLayoutParams();
                if (vp instanceof ViewGroup.MarginLayoutParams) {
                    vp.height = height;
                    ((ViewGroup.MarginLayoutParams) vp).leftMargin = (int) left;
                    ((ViewGroup.MarginLayoutParams) vp).topMargin = (int) top;
                    ((ViewGroup.MarginLayoutParams) vp).rightMargin = (int) right;
                    view.setLayoutParams(vp);
                }
            }
        });
    }
}
