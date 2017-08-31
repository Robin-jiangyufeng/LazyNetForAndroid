/*
 * 文 件 名:  ResponseDataEntry.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月17日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import com.robin.lazy.util.ObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存的http Response
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年12月17日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CacheResponseEntity implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 438428622740335388L;

	/** http返回的数据 */
	private byte[] resultData;

	/** http返回的请求的请求头 */
	private HashMap<String, ArrayList<String>> headers;

	public CacheResponseEntity(byte[] resultData,
			Map<String, List<String>> mHeaders) {
		this.resultData = resultData;
		headers = new HashMap<String, ArrayList<String>>();
		for (String key : mHeaders.keySet()) {
			List<String> list = mHeaders.get(key);
			headers.put(key, new ArrayList<String>(list));
		}
	}

	/**
	 * 获取缓存的http请求数据结果
	 * 
	 * @return byte[]
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public byte[] getResultData() {
		return resultData;
	}

	/**
	 * 设置缓存的http请求数据结果
	 * 
	 * @param resultData
	 *            void
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public void setResultData(byte[] resultData) {
		this.resultData = resultData;
	}

	/***
	 * 获取http返回的请求的请求头
	 * 
	 * @return http请求反馈的请求头 Map<String,List<String>>
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public Map<String, List<String>> getHeaders() {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.putAll(headers);
		return map;
	}

	/**
	 * 设置http返回的请求的请求头
	 * 
	 * @param headers
	 *            http请求反馈的请求头 void
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public void setHeaders(HashMap<String, ArrayList<String>> headers) {
		this.headers = headers;
	}

	/***
	 * 计算自身占用内存大小 void
	 * 
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public int sizeOf() {
		int size = 0;
		if (resultData != null) {
			size += resultData.length;
		}
		if (headers != null) {
			for (String key : headers.keySet()) {
				if (key != null && key != "") {
					size += (key.getBytes().length + 8);
				}
				List<String> list = headers.get(key);
				for (String str : list) {
					if (str != null && str != "") {
						size += (str.getBytes().length + 8);
					}
				}
			}
		}
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (getClass() == o.getClass()) {
			if (this == o) {
				return true;
			}
			CacheResponseEntity respone = (CacheResponseEntity) o;
			if (respone != null
					&& ObjectUtils.isEquals(headers, respone.headers)
					&& Arrays.equals(resultData, respone.resultData)) {
				return true;
			}
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		if (resultData != null && headers != null) {
			return resultData.hashCode() + headers.hashCode();
		}
		return super.hashCode();
	}
}
