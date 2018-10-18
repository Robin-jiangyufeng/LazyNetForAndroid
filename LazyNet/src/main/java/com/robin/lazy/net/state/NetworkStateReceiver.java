/*
 * Copyright (C) 2013  WhiteCat 白猫 (www.thinkandroid.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.lazy.net.state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.robin.lazy.net.http.log.NetLog;

import java.util.ArrayList;

/**
 * 是一个检测网络状态改变的，
 * 
 * 需要配置 <receiver
 * android:name="com.swisstar.microbutler.net.state.NetworkStateReceiver" >
 * <intent-filter> <action android:name="android.net.conn.CONNECTIVITY_CHANGE"
 * /> <action android:name="android.gzcpc.conn.CONNECTIVITY_CHANGE" />
 * </intent-filter> </receiver>
 * 
 * 需要开启权限 <uses-permission
 * android:name="android.permission.CHANGE_NETWORK_STATE" /> <uses-permission
 * android:name="android.permission.CHANGE_WIFI_STATE" /> <uses-permission
 * android:name="android.permission.ACCESS_NETWORK_STATE" /> <uses-permission
 * android:name="android.permission.ACCESS_WIFI_STATE" />
 * 
 * @author 江钰锋
 * @version [版本号, 2014年6月19日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class NetworkStateReceiver extends BroadcastReceiver {
	private final static String LOG_TAG=NetworkStateReceiver.class.getSimpleName();
	
	private static ArrayList<NetChangeObserver> netChangeObserverArrayList = new ArrayList<NetChangeObserver>();

	public final static String BUTLER_ANDROID_NET_CHANGE_ACTION = "robin.android.net.conn.CONNECTIVITY_CHANGE";

	/**
	 * 网络观察者
	 */
	private static BroadcastReceiver receiver;

	/**
	 * 是否有网络
	 */
	private static boolean isNetwork = true;

	/**
	 * 当前网络类型
	 */
	private static NetWorkUtil.NetType netType;

	private static BroadcastReceiver getReceiver() {
		if (receiver == null) {
			synchronized (NetworkStateReceiver.class) {
				if (receiver == null) {
					receiver = new NetworkStateReceiver();
				}
			}
		}
		return receiver;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		receiver = NetworkStateReceiver.this;
		if (intent.getAction().equalsIgnoreCase(
				ConnectivityManager.CONNECTIVITY_ACTION)
				|| intent.getAction().equalsIgnoreCase(
						BUTLER_ANDROID_NET_CHANGE_ACTION)) {
			if (!NetWorkUtil.isNetworkAvailable(context)) {
				isNetwork = false;
				netType = NetWorkUtil.NetType.NOT;
				NetLog.i(LOG_TAG,"没有网络连接.");
			} else {
				isNetwork = true;
				netType = NetWorkUtil.getAPNType(context);
				NetLog.i(LOG_TAG,"网络连接成功.");
			}
			notifyObserver();
		}
	}

	/**
	 * 注册网络状态广播
	 * 
	 * @param mContext
	 */
	public static void registerNetworkStateReceiver(Context mContext) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BUTLER_ANDROID_NET_CHANGE_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.getApplicationContext()
				.registerReceiver(getReceiver(), filter);
	}

	/**
	 * 注销网络状态广播
	 * 
	 * @param mContext
	 */
	public static void unregisterNetworkStateReceiver(Context mContext) {
		if (receiver != null) {
			try {
				mContext.getApplicationContext().unregisterReceiver(receiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 检查网络状态
	 * 
	 * @param mContext
	 */
	public static void checkNetworkState(Context mContext) {
		Intent intent = new Intent();
		intent.setAction(BUTLER_ANDROID_NET_CHANGE_ACTION);
		mContext.sendBroadcast(intent);
	}

	/**
	 * 通知所有观察者网络改变
	 * 
	 * @see [类、类#方法、类#成员]
	 */
	private void notifyObserver() {
		for (NetChangeObserver observer : netChangeObserverArrayList) {
			if (observer != null) {
				if (isNetwork) {
					observer.onConnect(netType);
				} else {
					observer.onDisConnect();
				}
			}
		}

	}

	/**
	 * 注册网络连接观察者
	 * 
	 * @param observer
	 *            observerKey
	 */
	public static void registerObserver(NetChangeObserver observer) {
		if (netChangeObserverArrayList == null) {
			netChangeObserverArrayList = new ArrayList<NetChangeObserver>();
		}
		netChangeObserverArrayList.add(observer);
	}

	/**
	 * 注销网络连接观察者
	 * 
	 * @param observer
	 *            observerKey
	 */
	public static void unregisterObserver(NetChangeObserver observer) {
		if (netChangeObserverArrayList != null
				&& netChangeObserverArrayList.contains(observer)) {
			netChangeObserverArrayList.remove(observer);
		}
	}

	/**
	 * 得到当前是否有网络
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isNetwork() {
		return isNetwork;
	}

	/**
	 * 得到当前的网络类型
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static NetWorkUtil.NetType getNetType() {
		return netType;
	}

	/**
	 * 关闭网络观察者
	 * 
	 * @see [类、类#方法、类#成员]
	 */
	public static void close() {
		if (netChangeObserverArrayList != null) {
			netChangeObserverArrayList.clear();
			netChangeObserverArrayList = null;
		}
		if (receiver != null) {
			receiver = null;
		}
	}

}