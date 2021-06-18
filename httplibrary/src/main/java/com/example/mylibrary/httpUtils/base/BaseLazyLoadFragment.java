package com.example.mylibrary.httpUtils.base;


import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.mylibrary.httpUtils.utils.DensityUtil;
import com.example.mylibrary.httpUtils.utils.LogUtils;
import com.example.mylibrary.httpUtils.utils.ScreenUtils;
import com.example.mylibrary.httpUtils.view.CustomDialog;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseLazyLoadFragment extends Fragment {

    private CustomDialog customDialog;
    private View rootView;
    protected Context context;
    protected int mNoPermissionIndex = 0;
    protected final int PERMISSION_REQUEST_CODE = 1;
    protected String isWriting = "功能开发中...";
    protected final String[] permissionManifest = {
            Manifest.permission.CAMERA,
//            Manifest.permission.BLUETOOTH,
//            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    protected final String[] permissionManifestLocation = {
            Manifest.permission.ACCESS_COARSE_LOCATION};

    //FIXME 控件是否初始化完成
    private boolean isViewCreated;
    //FIXME 数据是否已加载完毕
    protected boolean isLoadDataCompleted;
    protected int updateIndex = 0;
    protected String GAODE_NAME = "com.autonavi.minimap";
    protected String BAIDU_NAME = "com.baidu.BaiduMap";

    public abstract void loadData();

    public abstract int getLayoutId();

    public abstract void initViews(Bundle savedInstanceState, View view);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        initViews(savedInstanceState, rootView);
        //FIXME 控件初始化完成
        isViewCreated = true;
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewCreated && !isLoadDataCompleted) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    /*loadData()会在两个地方执行？
    因为，ViewPager 默认显示第一页，第一页肯定要先加载数据，
    而且 setUserVisibleHint 的执行顺序又是在 onCreatView 之前，
    同时 onCreatView 需要初始化界面和修改 isViewCreated 的值。
    所以就需要在 onActivityCreated 里执行一次。*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getUserVisibleHint()) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    protected String addD(String num){
        if(num.length()==1){
            return "0." + num;
        }else{
            String endT = num.substring(num.length()-1);
            return num.substring(0, num.length() - 1) +"."+ endT;
        }
    }

    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
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

    protected void showLoadingDialog(String hint) {
        if (customDialog == null) {
            customDialog = new CustomDialog(context, hint);
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
                        if (customDialog.isShowing()) {
                            customDialog.dismiss();  //fixme 需要查看所有，延时取消
                        }
                    }
                }
            }, 500);
        } catch (Exception e) {
        }
    }

    //复制文字
    protected void fzText(String con) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(con);
        showToast("复制成功");
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

    //显示软键盘
    protected void showKeyboard(EditText et_chat) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_chat, InputMethodManager.SHOW_FORCED);
    }

    //获取软键盘高度
    protected void getSoftHeight(final LinearLayout ll_inputparent, final RelativeLayout parentLayout, final boolean needPlusBottomViewH) {
        int height = ScreenUtils.getNavigationBarHeight(getActivity());
        LogUtils.logE("测量底部状态栏: ", "是否有=" + ScreenUtils.checkDeviceHasNavigationBar(getActivity()));
        LogUtils.logE("测量底部状态栏: ", "height=" + height);
        LogUtils.logE("测量底部状态栏: ", "底部栏显示=" + ScreenUtils.isNavigationBarShow(getActivity()));
        if (height > 0) {
            if (ScreenUtils.isHuaWei_Vivo() && !ScreenUtils.isNavigationBarShow(getActivity())) {
                //单独适配华为,有自己的间距
                height = 0;
            }
        } else {
            //魅族软键盘底部定制了间距
//            if (ScreenUtils.isMEIZU()) {
//                height = DensityUtil.dip2px(getActivity(), 16);
//            }
        }
        LogUtils.logE("测试", "getSoftHeight=" + height);
        final View myLayout = ((Activity) context).getWindow().getDecorView();
        final int finalHeight = height;
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
                    statusBarHeight = context.getResources().getDimensionPixelSize(x);
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
                            p.bottomMargin = realKeyboardHeight - DensityUtil.dip2px(context, 50) - finalHeight;
                        }
                        ll_inputparent.requestLayout();
                        ll_inputparent.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    //隐藏软键盘
    public void hideKeyboard(EditText et_chat) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
    }

    // 判定是否需要隐藏
    protected boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            int dp10 = DensityUtil.dip2px(context, 10);
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

    //检测是否有权限
    protected boolean permissionCheck() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        String permission;
        for (int i = 0; i < permissionManifest.length; i++) {
            permission = permissionManifest[i];
            mNoPermissionIndex = i;
            if (PermissionChecker.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    //toast
    protected void showToast(String tip) {
        Toast.makeText(context.getApplicationContext(), tip, Toast.LENGTH_LONG).show();
    }

    //changeAct
    protected void changeAct(Bundle bundle, Class<?> toClass) {
        Intent intent = new Intent();
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        intent.setClass(context, toClass);
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
}

