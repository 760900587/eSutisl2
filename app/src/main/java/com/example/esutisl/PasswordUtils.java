package com.example.esutisl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PasswordUtils {
    /* return FIIST = 9        表示首次登录  或  首次设置密码
     * return SUCCESSFULLU = 3 表示登录成功  或   设置密码成功
     * return  LOGON_FAILED =4 表示登录失败  或   设置密码失败
     * COUNT 剩余次数
     */
    private static int FIIST = 9;
    private static int SUCCESSFULLU = 3;
    private static int LOGON_FAILED = 4;
    private static int COUNT = 3;
    private static int SETCOUNT = 3;
    //3次登录机会
    private static int LOGIN_CHANCES = 3;
    //还剩几次登录机会的标志，初始值就是LOGIN_CHANCES
    //多次认证失败时需要等待的时间
    private static int fist = LOGIN_CHANCES;
    //修改次数
    private static int modifyFist = LOGIN_CHANCES;
    private static float WAIT_TIME = 30000L;

    /*
    /设置密码接口
    return Map<String,object>
     */
    public static Map<String, Object> setPassword(int level, String pNewPassword, String oldPassword, Context context) {
        Map<String, Object> map = new HashMap<>();
        //创建数据库实例
        Mysql mysql = new Mysql(context);
        // 调用查询方法 如果i retrun 0 ，表示没有数据 表示首次登录
        int i = mysql.selectQuery();
        if (i == 0) {
            //首次设置密码
            boolean is_setPassword = First_password(level, context, pNewPassword);
            if (is_setPassword) {
                map.put("FIST", FIIST);
                map.put(" FAILURE_number", 0L);
                map.put("TIMER", SETCOUNT);
                return map;
            }
        } else {
            //获取开机时间
            long l = SystemClock.elapsedRealtime();
            Log.i("liuhongliang", "Loagin: 开机时间" + l);
            @SuppressLint("WrongConstant")
            SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_APPEND);
            long modifyErrorTime = sp.getLong("modifyErrorTime", 0L);
            int modifyErrorNumber = sp.getInt("modifyErrorNumber", 0);
            long TIME=300000*modifyErrorNumber;
            Mysql mysql1 = new Mysql(context);
            Bean select = mysql1.select();
            int count = select.getCount();
            if(l-modifyErrorTime>TIME){
                // 第二次修改密码需要校验旧密码
                boolean b = SecondPassword(level, pNewPassword, oldPassword, context);
                if (b) {
                    mysql1.UpdataCount(0, 3);
                    map.put("FIST", SUCCESSFULLU);
                    map.put(" FAILURE_number", 0L);
                    map.put("TIMER", SETCOUNT);
                    return map;
                } else {
                    if (modifyFist == 1) {
                        //count值重置
                        modifyFist = LOGIN_CHANCES;
                        //Toast提醒
                        Log.i("liuhongliang", "Loagin: 三次修改失败 ");
                        //LOGIN_CHANCES次修改失败时，获取此时的Java虚拟机运行时刻并保存提交
                        long errorTime = SystemClock.elapsedRealtime();
                        Log.i("liuhongliang", "Loagin: 系统时间" + errorTime);
                        SharedPreferences sp1 = context.getSharedPreferences("data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp1.edit();
                        editor.putLong("modifyErrorTime", errorTime);
                        editor.putInt("modifyErrorNumber",sp1.getInt("modifyErrorNumber",0)+1);
                        editor.commit();
                        map.put("FIST", LOGON_FAILED);
                        map.put("FAILURE",select.getCount());
                        map.put("TIMER", TimeUtils.formatTime(TIME));
                        return map;
                    } else {
                        modifyFist--;
                        if (count >= 0) {
                            count--;
                        }
                        mysql.UpdataCount(0, count);
                    }
                    map.put("FIST", LOGON_FAILED);
                    map.put("FAILURE", select.getCount());
                    map.put("TIMER", 0);
                    return map;
                }
            }else {
                long time = l - modifyErrorTime;
                long a = TIME - time;
                map.put("FIST", LOGON_FAILED);
                map.put("FAILURE",select.getCount());
                map.put("TIMER", TimeUtils.formatTime(a));
                return map;
            }
        }
        return map;
    }

    // 校验旧密码
    private static boolean SecondPassword(int level, String pNewPassword, String oldPassword, Context context) {
        Mysql mysql = new Mysql(context);
        Bean select = mysql.select();//查询
        //校验密码 解密旧密码
//        String s = AESUtils.decryptPassword(select.getPassword());
//        Log.i("liu", s);
        //查询数据库校验旧密码
        if (level == 0 && oldPassword.equals(select.getPassword())) {
            //加密新密码
//            String password = AESUtils.encryptPassword(pNewPassword);
//            Log.i("liuhong", password);
            //修改根据id修改密码
            mysql.Updata(0, pNewPassword);
            return true;
        } else {
            return false;
        }
    }

    // 首次设置密码
    private static boolean First_password(int level, Context context, String password) {
        Mysql mysql = new Mysql(context);
        Bean bean = new Bean();
        //加密
        if (level == 0) {
            bean.setPassword(password);
            bean.setCount(COUNT);
            mysql.insert(bean);
            return true;
        } else {
            return false;
        }
    }

    /*
        return :登录接口
     */
    public static Map<String, Object> Loagin(int level, String password, Context context) {
        Map<String, Object> map = new HashMap<>();
        //p判断是否第一次登陆
        Mysql mysql = new Mysql(context);
        int i = mysql.selectQuery();
        boolean b = First_Loage(level);//首次登陆
        if (i == 0 && b) {
            //首次登陆
            Mysql mysql1 = new Mysql(context);
            Bean bean = new Bean();
            bean.setCount(COUNT);
            map.put("FIST", FIIST);
            map.put("FAILURE", 0);
            map.put("TIMER", 0L);
            return map;
        } else {
            Mysql mysql1 = new Mysql(context);
            Bean bean = mysql1.select();
            int count = bean.getCount();
            /*
               b1 = true 登录成功
               b1 =false "登录失败" 失败三次需要倒计时

             */
            @SuppressLint("WrongConstant")
            SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_APPEND);
            int errorNumber = sp.getInt("errorNumber", 1);
            long TIME = 300000L*errorNumber;
            boolean b1 = Second_landing(level, password, context);
            //获取开机时间
            long l = SystemClock.elapsedRealtime();
            Log.i("liuhongliang", "Loagin: 开机时间" + l);
            long errorTime = sp.getLong("errorTime", 0L);
            if (l - errorTime > TIME) {
                if (b1) {
                    mysql1.UpdataCount(0, 3);
                    Bean select = mysql1.select();
                    Log.i("liuhongliang", select.toString());
                    map.put("FIST", SUCCESSFULLU);
                    map.put(" FAILURE_number", 0);
                    map.put("TIMER", 3);
                    return map;
                } else {
                    if (fist == 1) {
                        //count值重置
                        fist = LOGIN_CHANCES;
                        //Toast提醒
                        Log.i("liuhongliang", "Loagin: 三次登录失败 ");
                        //LOGIN_CHANCES次登录失败时，获取此时的Java虚拟机运行时刻并保存提交
                        errorTime = SystemClock.elapsedRealtime();
                        Log.i("liuhongliang", "Loagin: 系统时间" + errorTime);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putLong("errorTime", errorTime);
                        editor.putInt("errorNumber",sp.getInt("errorNumber",0)+1);
                        editor.commit();
                        int errorNumber1 = sp.getInt("errorNumber", 0);
                        map.put("FIST", LOGON_FAILED);
                        map.put("FAILURE",bean.getCount());
                        map.put("TIMER", TimeUtils.formatTime(300000L*errorNumber1));
                        return map;
                    } else {
                        fist--;
                        if (count >= 0) {
                            count--;
                        }
                        mysql.UpdataCount(0, count);
                    }
                    Bean select = mysql.select();
                    map.put("FIST", LOGON_FAILED);
                    map.put("FAILURE", select.getCount());
                    map.put("TIMER", 0);
                    return map;
                }
            } else {
                long time = l - errorTime;
                long a = TIME - time;
                map.put("FIST", LOGON_FAILED);
                map.put("FAILURE", bean.getCount());
                map.put("TIMER", TimeUtils.formatTime(a));
                return map;
            }

        }
    }

    public static boolean Second_landing(int level, String password, Context context) {
        Mysql mysql = new Mysql(context);
        Bean select = mysql.select();
        if (select.getPassword().equals(password) && level == 0) {
            return true;
        } else {
            return false;
        }
    }

    //首次登陆
    public static boolean First_Loage(int level) {
        if (level == 0) {
            return true;
        } else {
            return false;
        }
    }

}
