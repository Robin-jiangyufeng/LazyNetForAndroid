package com.robin.lazy.net.http.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * http网络请求最基础的数据管理类
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class HttpResponseHandler implements HttpResponseHandlerBase {
	public final String DEFAULT_CHARSET = "UTF-8";

	public final String UTF8_BOM = "\uFEFF";

	/**
	 * 编码类型
	 */
	private String responseCharset = DEFAULT_CHARSET;

	/**
	 * 字节数组大小
	 */
	public final int BYTE_SIZE = 1024 * 4;

	/**
	 * 当前请求是否取消
	 */
	private boolean isCancel;

	@Override
	public void resetRequestData() {
		isCancel = false;
	}

	@Override
	public void setConnectProperty(HttpURLConnection urlConnection,
			ConcurrentHashMap<String, String> sendHeaderMap) {
		// 设置标题信息
		if (sendHeaderMap != null) {
			for (String key : sendHeaderMap.keySet()) {
				String value = sendHeaderMap.get(key);
				urlConnection.addRequestProperty(key, value);
			}
		}
	}

	@Override
	public void sendSuccessMessage(int messageId,
			Map<String, List<String>> headers, byte[] responseByteData) {

	}

	@Override
	public void sendFailMessage(int messageId, int statusCode,
			Map<String, List<String>> headers, byte[] responseErrorByteData) {

	}

	@Override
	public boolean sendResponseMessage(HttpURLConnection urlConnection,
			RequestParam request) {
		if (urlConnection != null && request != null) {
			int responseCode = 0;
			byte[] data = null;
			try {
				sendhttpRequest(urlConnection, request);
				responseCode = urlConnection.getResponseCode();
				data = readResponseData(urlConnection);
			} catch (UnknownServiceException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.UNKNOW_SERVICE_ERROR;
				}
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.READ_TIME_OUT;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.SECURITY_ERROR;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.FILE_NOT_FOUND_EXCEPTION;
				}
			} catch (ProtocolException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.PROTOCOL_EXCEPTION;
				}
			} catch (SocketException e) {
				e.printStackTrace();
				if (responseCode == 0) {
					responseCode = HttpError.CONNECT_ERROR;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Map<String, List<String>> headers = urlConnection
						.getHeaderFields();
				if (responseCode == HttpError.RESPONSE_CODE_200
						&& !isCancelRequest()) {
					sendSuccessMessage(request.getMessageId(), headers, data);
					return true;
				} else if (isCancelRequest()) {
					sendFailMessage(request.getMessageId(),
							HttpError.USER_CANCEL, headers, data);
				} else {
					sendFailMessage(request.getMessageId(), responseCode,
							headers, data);
				}
			}
		}
		return false;
	}

	@Override
	public boolean isCancelRequest() {
		synchronized (this) {
			return isCancel;
		}
	}

	@Override
	public void cancelRequest() {
		synchronized (this) {
			isCancel = true;
		}
	}

	/**
	 * 发送http请求
	 *
	 * @param urlConnection
	 *            HttpURLConnection连接
	 * @param request
	 *            请求数据
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	protected void sendhttpRequest(HttpURLConnection urlConnection,
			RequestParam request) {
		DataOutputStream out = null;
		OutputStream outStream = null;
		try {
			if (!request.isEmptyForData()) {
				outStream = urlConnection.getOutputStream();
				out = new DataOutputStream(outStream);
				out.write(request.getSendData().getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
					out = null;
				}
				if (outStream != null) {
					outStream.flush();
					outStream.close();
					outStream = null;
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * 获得服务端反馈数据
	 *
	 * @param urlConnection
	 *            HttpURLConnection连接
	 * @return
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	protected byte[] readResponseData(HttpURLConnection urlConnection) {
		byte[] data = null;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		long contentLength = 0;
		try {
			is = urlConnection.getInputStream();
			String content_encode = urlConnection.getContentEncoding();
			if (null != content_encode && !"".equals(content_encode)
					&& content_encode.equals("gzip")) {
				is = new GZIPInputStream(is);
			}
			contentLength = urlConnection.getContentLength();
			if (contentLength > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"HTTP entity too large to be buffered in memory");
			}
		} catch (IOException e) {
			is = urlConnection.getErrorStream();
		}

		if (is == null) {
			return null;
		}

		try {
			int buffersize = (contentLength <= 0) ? BYTE_SIZE
					: (int) contentLength;
			baos = new ByteArrayOutputStream(buffersize);
			byte[] tmp = new byte[BYTE_SIZE];
			int c = 0;
			while ((c = is.read(tmp)) != -1) {
				if (isCancelRequest()) {
					break;
				}
				baos.write(tmp, 0, c);
			}
			if (!isCancelRequest()) {
				data = baos.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
					baos = null;
				}

				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		return data;
	}

	public String getResponseCharset() {
		return responseCharset;
	}

	public void setResponseCharset(String responseCharset) {
		this.responseCharset = responseCharset;
	}

	@Override
	public void clean() {

	}
}
