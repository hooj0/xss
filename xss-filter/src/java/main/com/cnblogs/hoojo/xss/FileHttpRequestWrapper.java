package com.cnblogs.hoojo.xss;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.cnblogs.hoojo.xss.configuration.FilterConfiguration;
import com.cnblogs.hoojo.xss.configuration.PropertyConfiguration;
import com.cnblogs.hoojo.xss.rejector.InjectedRejectorUtils;
import com.cnblogs.hoojo.xss.wrapper.FileServletInputStream;

/**
 * <b>function:</b> 文件上传过滤
 * 
 * @author hoojo
 * @createDate 2018年1月10日 下午4:56:15
 * @file XssHttpRequestWrapper.java
 * @package com.cnblogs.hoojo.xss
 * @project xss
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class FileHttpRequestWrapper extends TextHttpRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(FileHttpRequestWrapper.class);
	
	private static final byte CR = '\r';
	private static final byte LF = '\n';
	
	private static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	private static final byte[] FIELD_SEPARATOR = { CR, LF };
	
	private static final String CHARSET_PROP_KEY = "file.charset";
	private static final String MAX_FILE_SIZE_PROP_KEY = "file.max.size";
	private static final String LIMIT_FILE_TYPE_PROP_KEY = "file.limit.type";
	private static final String ALLOWED_TYPE_PROP_KEY = "file.allowed.type";
	
	/** 过滤的文件类型 contentType */
	private static final String LIMIT_FILE_TYPE = "application/octet-stream";
	/** 允许上传的文件后缀 */
	private static final String ALLOWED_TYPE = ".zip,.rar,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.gif,.jpeg,.jpg,.pdf,.png,.bmp,.mp3,.txt,.xml";

	private static final Splitter SPLITTER = Splitter.on(CharMatcher.anyOf(",;")).omitEmptyStrings();
	private static final long MB = 1024L * 1024;

	private Long maxFileSize = MB * 50; //50M
	private String charset = "UTF-8";
	
	private Map<String, FileItem> escapedParametersFileItemsMap = new HashMap<>();
	private List<String> contentTypes;
	private List<String> allowedTypes;
	
	private HttpServletRequest request;
	private File tmpFile;
	private String url;
	
	public FileHttpRequestWrapper(HttpServletRequest request) throws Exception {
		super(request);
		
		this.request = request;
		this.url = request.getRequestURI();
		
		try {
			this.charset = PropertyConfiguration.getInstance().getProp(CHARSET_PROP_KEY, charset);
			this.maxFileSize = Long.valueOf(PropertyConfiguration.getInstance().getProp(MAX_FILE_SIZE_PROP_KEY, String.valueOf(maxFileSize)));
			this.contentTypes = SPLITTER.splitToList(PropertyConfiguration.getInstance().getProp(LIMIT_FILE_TYPE_PROP_KEY, LIMIT_FILE_TYPE).toLowerCase());
			this.allowedTypes = SPLITTER.splitToList(PropertyConfiguration.getInstance().getProp(ALLOWED_TYPE_PROP_KEY, ALLOWED_TYPE).toLowerCase());
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			
			//Create a progress listener
			ProgressListener progressListener = new ProgressListener() {
				private long megaBytes = -1;

				public void update(long pBytesRead, long pContentLength, int pItems) {
					long mBytes = pBytesRead / 1000000;
					if (megaBytes == mBytes) {
						return;
					}
					megaBytes = mBytes;
					if (pContentLength == -1) {
						logger.debug("So far, " + pBytesRead / 1024.0 / 1024.0 + " have been read.");
					} else {
						logger.debug("So far, " + pBytesRead / 1024.0 / 1024.0 + "\tof " + pContentLength / 1024.0 / 1024.0);
					}
				}
			};
			fileUpload.setProgressListener(progressListener);

			List<FileItem> list = fileUpload.parseRequest(request);
			if (list != null && !list.isEmpty()) {
				transformTmpFile(list);
			}
		} catch (Exception e) {
			logger.error("Failed to read multipart stream.", e);
			throw e;
		}
	}
	
	public List<FileItem> getFileItems() {
		return new ArrayList<FileItem>(escapedParametersFileItemsMap.values());
	}

	public FileItem getFileItem(String fieldName) {
		return escapedParametersFileItemsMap.get(fieldName);
	}
	
	private void checkFile(FileItem fileItem) throws RuntimeException {
		String fileName = fileItem.getName();
		if (fileName != null) {
			
			int index = fileName.lastIndexOf(".");
			if (fileName.lastIndexOf(".") != -1) {
				
				String contentType = fileItem.getContentType().toLowerCase();
				if (FilterConfiguration.getInstance().isCheckFileType(url) && contentTypes.contains(contentType)) {
					
					String fileType = fileName.substring(index).toLowerCase();
					if (!allowedTypes.contains(fileType)) {
						throw new RuntimeException("上传的文件格式不对");
					}
				}
			}
		}

		long size = fileItem.getSize();
		logger.debug("fileSize: {}, maxFileSize: {}", size, maxFileSize);
		if (FilterConfiguration.getInstance().isCheckFileSize(url) && (this.maxFileSize != null) && size > this.maxFileSize.longValue()) {
			throw new RuntimeException("请确认上传的文件小于" + (maxFileSize.longValue() / MB) + "M");
		}
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.tmpFile == null) {
			return request.getInputStream();
		}
		
		byte[] streamBytes = readInputStreamBytes(new FileInputStream(this.tmpFile));
		return new FileServletInputStream(streamBytes);
	}
	
	private File createTmpFile() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String tempFileName = "upload_" + UUID.randomUUID().toString() + ".tmp";
		File tempFile = new File(tempDir, tempFileName);
		return tempFile;
	}
	
	private void transformTmpFile(List<FileItem> list) throws Exception {
		byte[] boundary = getBoundary(getContentType());
		
		this.tmpFile = createTmpFile();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(this.tmpFile);
			bos = new BufferedOutputStream(fos);
			bos.write("--".getBytes());
			bos.write(boundary);
			
			for (FileItem fileItem : list) {
				writeFile(fileItem, bos, boundary);
			}
		} catch (Exception e) {
			logger.error("保存临时文件异常：", e);
			throw e;
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					logger.error("Failed to read multipart stream.", e1);
					throw new RuntimeException("提交数据存在安全隐患");
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					logger.error("Failed to read multipart stream.", e1);
					throw new RuntimeException("提交数据存在安全隐患");
				}
			}
		}
	}
	
	private void writeFile(FileItem fileItem, BufferedOutputStream bos, byte[] boundary) throws Exception {
		bos.write(FIELD_SEPARATOR);
		if (fileItem.isFormField()) {
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"".getBytes()).getBytes());
			bos.write(HEADER_SEPARATOR);
			
			String data = InjectedRejectorUtils.invokeAll(url, fileItem.getFieldName(), fileItem.getString(this.charset));
			if (data != null) {
				bos.write(data.getBytes());
			}
			
			escapedParametersValuesMap.put(fileItem.getFieldName(), new String[] { data });
		} else {
			checkFile(fileItem);

			String data = InjectedRejectorUtils.invokeAll(url, fileItem.getFieldName(), fileItem.getName());
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"; filename=\"" + data + "\"").getBytes());
			bos.write(FIELD_SEPARATOR);
			bos.write(("Content-Type: " + fileItem.getContentType()).getBytes());
			bos.write(HEADER_SEPARATOR);
			bos.write(readInputStreamBytes(fileItem.getInputStream()));
			
			escapedParametersFileItemsMap.put(fileItem.getFieldName(), fileItem);
		}
		bos.write(FIELD_SEPARATOR);
		bos.write("--".getBytes());
		
		bos.write(boundary);
	}

	private byte[] getBoundary(String contentType) {
		ParameterParser parser = new ParameterParser();
		parser.setLowerCaseNames(true);

		Map<String, String> params = parser.parse(contentType, new char[] { ';', ',' });
		String data = (String) params.get("boundary");

		if (data == null) {
			return null;
		}
		
		byte[] boundary;
		try {
			boundary = data.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			boundary = data.getBytes();
		}
		
		return boundary;
	}

	private byte[] readInputStreamBytes(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] buff = new byte[1024];
		int len = 0;
		byte[] in2b = null;
		try {
			while ((len = is.read(buff)) > 0) {
				baos.write(buff, 0, len);
			}
		} catch (IOException e) {
			logger.error("Failed to read multipart stream.", e);
		} finally {
			try {
				in2b = baos.toByteArray();
				if (baos != null) {
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				logger.error("Failed to close multipart stream.", e);
			}
		}

		return in2b;
	}
}
