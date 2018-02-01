package com.masget.xss.wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.masget.xss.TextHttpRequestWrapper;
import com.masget.xss.configuration.FilterConfiguration;
import com.masget.xss.configuration.PropertyConfiguration;
import com.masget.xss.rejector.InjectedRejectorUtils;

/**
 * <b>function:</b> 文件上传过滤
 * 
 * @author hoojo
 * @createDate 2018年1月10日 下午4:56:15
 * @file XssHttpRequestWrapper.java
 * @package com.masget.xss
 * @project xss
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class FileStreamHttpRequestWrapper extends TextHttpRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(FileStreamHttpRequestWrapper.class);
	
	private static final byte CR = 13;
	private static final byte LF = 10;
	
	private static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	private static final byte[] FIELD_SEPARATOR = { CR, LF };
	/** 过滤的文件类型 contentType */
	private static final String LIMIT_FILE_TYPE = "application/octet-stream";
	/** 允许上传的文件后缀 */
	private static final String ALLOWED_TYPE = ".zip,.rar,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.gif,.jpeg,.jpg,.pdf,.png,.bmp,.mp3,.txt,.xml";

	private static final Splitter SPLITTER = Splitter.on(CharMatcher.anyOf(",;")).omitEmptyStrings();
	private static final long MB = 1024L * 1024;

	private Long maxFileSize = MB * 50; //50M
	private String charset = "UTF-8";
	
	private List<String> contentTypes;
	private List<String> allowedTypes;
	
	private byte[] isBytes;
	private File tmpFile;
	private String url;
	
	public FileStreamHttpRequestWrapper(HttpServletRequest request) throws Exception {
		super(request);
		this.url = request.getRequestURI();
		
		try {
			this.maxFileSize = Long.valueOf(PropertyConfiguration.getInstance().getProp("file.max.size", String.valueOf(maxFileSize)));
			this.contentTypes = SPLITTER.splitToList(PropertyConfiguration.getInstance().getProp("file.limit.type", LIMIT_FILE_TYPE).toLowerCase());
			this.allowedTypes = SPLITTER.splitToList(PropertyConfiguration.getInstance().getProp("file.allowed.type", ALLOWED_TYPE).toLowerCase());
			this.charset = PropertyConfiguration.getInstance().getProp("file.charset", charset);
			
			this.isBytes = readInputStreamBytes(request.getInputStream());
			
			// 创建ServletFileUpload实例
			ServletFileUpload fileUpload = new ServletFileUpload();
			// 解析request请求 返回FileItemStream的iterator实例
			FileItemIterator iter = fileUpload.getItemIterator(this);
			
			transformTmpFile(iter);
			
		} catch (IOException e) {
			logger.error("Failed to read multipart stream.", e);
			throw e;
		} catch (FileUploadException e) {
			logger.error("Failed to read multipart stream.", e);
			throw e;
		} catch (Exception e) {
			logger.error("Failed to read multipart stream.", e);
			throw e;
		}
	}
	
	private void writeFile(FileItemStream fileItem, BufferedOutputStream bos, byte[] boundary) throws Exception {
		bos.write(FIELD_SEPARATOR);
		if (fileItem.isFormField()) {
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"".getBytes()).getBytes());
			bos.write(HEADER_SEPARATOR);
			
			String data = InjectedRejectorUtils.invokeAll(url, fileItem.getFieldName(), Streams.asString(fileItem.openStream(), charset));//增加新值
			if (data != null) {
				bos.write(data.getBytes());
			}
			
			escapedParametersValuesMap.put(fileItem.getFieldName(), new String[] { data });
		} else {
			InputStream is = fileItem.openStream();
			//System.out.println("fileSize: " + is.available());
			checkFile(fileItem, is.available());

			String data = InjectedRejectorUtils.invokeAll(url, fileItem.getFieldName(), fileItem.getName());
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"; filename=\"" + data + "\"").getBytes());
			bos.write(FIELD_SEPARATOR);
			bos.write(("Content-Type: " + fileItem.getContentType()).getBytes());
			bos.write(HEADER_SEPARATOR);
			bos.write(readInputStreamBytes(is));
			//bos.write(IOUtils.toByteArray(is));
		}
		bos.write(FIELD_SEPARATOR);
		bos.write("--".getBytes());
		
		bos.write(boundary);
	}
	
	private void checkFile(FileItemStream fileItem, int size) throws RuntimeException {
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

		logger.debug("fileSize: {}, maxFileSize: {}", size, maxFileSize);
		if (FilterConfiguration.getInstance().isCheckFileSize(url) && (this.maxFileSize != null) && size > this.maxFileSize.longValue()) {
			throw new RuntimeException("请确认上传的文件小于" + (maxFileSize.longValue() / MB) + "M");
		}
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.tmpFile == null) {
			return new FileServletInputStream(isBytes);
		}
		return new FileServletInputStream(readInputStreamBytes(new FileInputStream(this.tmpFile)));
	}

	private File createTmpFile() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String tempFileName = "upload_" + UUID.randomUUID().toString() + ".tmp";
		File tempFile = new File(tempDir, tempFileName);
		System.out.println("tmp file path: " + tempFile.getAbsolutePath());
		return tempFile;
	}
	
	private void transformTmpFile(FileItemIterator iter) throws Exception {
		byte[] boundary = getBoundary(getContentType());
		
		this.tmpFile = createTmpFile();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(this.tmpFile);
			bos = new BufferedOutputStream(fos);
			bos.write("--".getBytes());
			bos.write(boundary);
			
			while (iter.hasNext()) {
				FileItemStream item = iter.next();// 获取文件流
				writeFile(item, bos, boundary);
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

	private byte[] readInputStreamBytes(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		
		byte[] buff = new byte[1024];
		int len = 0;
		byte[] in2b = null;
		try {
			while ((len = bis.read(buff)) > 0) {
				baos.write(buff, 0, len);
			}
		} catch (IOException e) {
			logger.error("Failed to read multipart stream.", e);
			throw e;
		} finally {
			try {
				in2b = baos.toByteArray();
				if (baos != null) {
					baos.close();
					baos = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
			} catch (IOException e) {
				logger.error("Failed to close multipart stream.", e);
			}
		}

		return in2b;
	}
}
