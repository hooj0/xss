package com.masget.xss;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FileHttpRequestWrapper extends TextHttpRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(FileHttpRequestWrapper.class);
	
	private static final byte CR = 13;
	private static final byte LF = 10;
	
	private static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	private static final byte[] FIELD_SEPARATOR = { CR, LF };
	
	private String charset = "UTF-8";
	private Long maxFileSize = 1024L * 1024 * 50; //50M
	private String limitFileType = "application/octet-stream";
	private String allowedType = ".zip,.rar,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.gif,.jpeg,.jpg,.pdf,.png,.bmp,.mp3,.txt,.xml";
	
	private File tmpFile;
	private String url;
	
	public FileHttpRequestWrapper(HttpServletRequest request) {
		super(request);
		this.url = request.getRequestURI();
		
		try {
			this.maxFileSize = Long.valueOf(PropertyConfiguration.getInstance().getProp("file.max.size", String.valueOf(maxFileSize)));
			this.limitFileType = PropertyConfiguration.getInstance().getProp("file.limit.type", limitFileType);
			this.allowedType = PropertyConfiguration.getInstance().getProp("file.allowed.type", allowedType);;
			this.charset = PropertyConfiguration.getInstance().getProp("file.charset", charset);;

			DiskFileItemFactory dfif = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(dfif);

			List<FileItem> list = fileUpload.parseRequest(this);
			if ((list != null) && (list.size() > 0)) {
				
				boolean isRejector = true;
				for (FileItem fileItem : list) {
					if (fileItem.isFormField()) {
						String fieldName = fileItem.getFieldName();
						String fieldText = fileItem.getString(charset);
						
						String result = InjectedRejectorUtils.invokeAll(url, fieldName, fieldText);
						if (!(fieldText.equalsIgnoreCase(result))) {
							isRejector = false;
						}
					} else {

						String fileName = fileItem.getName();
						if (fileName != null) {
							int index = fileName.lastIndexOf(".");
							if (fileName.lastIndexOf(".") != -1) {
								String contentType = fileItem.getContentType();
								if (FilterConfiguration.getInstance().isCheckFileType(url) && (this.limitFileType.indexOf(contentType) > -1)) {
									String fileType = fileName.substring(index).toLowerCase();
									if (this.allowedType.toLowerCase().indexOf(fileType) < 0) {
										throw new RuntimeException("上传的文件格式不对");
									}
								}
							}
						}

						long size = fileItem.getSize();
						if (FilterConfiguration.getInstance().isCheckFileSize(url) && (this.maxFileSize != null) && size > this.maxFileSize.longValue()) {
							throw new RuntimeException("请确认上传的文件小于" + (maxFileSize.longValue() / 1024L / 1024L) + "M");
						}
					}
				}
				
				if (!isRejector) {
					saveTmpFile(list);
				}
			}
		} catch (IOException e) {
			logger.error("Failed to read multipart stream.", e);
		} catch (FileUploadException e) {
			logger.error("Failed to read multipart stream.", e);
		} catch (Exception e) {
			logger.error("Failed to read multipart stream.", e);
		}
	}

	private void saveTmpFile(List<FileItem> list) {
		byte[] boundary = getBoundary(getContentType());
		
		this.tmpFile = getTempFile();
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

			try {
				fos.close();
			} catch (IOException e1) {
				logger.error("Failed to read multipart stream.", e1);
				throw new RuntimeException("提交数据存在安全隐患");
			}
		} catch (Exception e) {
			logger.error("保存临时文件异常：", e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					logger.error("Failed to read multipart stream.", e1);
					throw new RuntimeException("提交数据存在安全隐患");
				}
			}
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e1) {
					logger.error("Failed to read multipart stream.", e1);
					throw new RuntimeException("提交数据存在安全隐患");
				}
		}
	}
	
	private void writeFile(FileItem fileItem, BufferedOutputStream bos, byte[] boundary) throws Exception {
		bos.write(FIELD_SEPARATOR);
		if (fileItem.isFormField()) {
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"".getBytes()).getBytes());
			bos.write(HEADER_SEPARATOR);
			bos.write(InjectedRejectorUtils.invokeAll(url, fileItem.getFieldName(), fileItem.getString(this.charset)).getBytes());
		} else {
			bos.write(("Content-Disposition: form-data; name=\"" + fileItem.getFieldName() + "\"; filename=\""+ fileItem.getName() + "\"").getBytes());
			bos.write(FIELD_SEPARATOR);
			bos.write(("Content-Type: " + fileItem.getContentType()).getBytes());
			bos.write(HEADER_SEPARATOR);
			bos.write(readInputStreamBytes(fileItem.getInputStream()));
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

	private File getTempFile() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String tempFileName = "upload_" + UUID.randomUUID().toString() + ".tmp";
		File tempFile = new File(tempDir, tempFileName);
		return tempFile;
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
