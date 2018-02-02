package com.cnblogs.hoojo.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cnblogs.hoojo.xss.log.ApplicationLogging;

/**
 * <b>function:</b> 文件上传、导入、下载控制器
 * 
 * @author hoojo
 * @createDate 2012-10-22 下午01:14:34
 * @file ResourceController.java
 * @project TradeSettlement
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
@Controller
@RequestMapping("/system/res")
public class ResourceController extends ApplicationLogging {

	private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
	protected String uploadPath = "/upload";

	@RequestMapping("/upload")
	public void upload(@RequestParam("file") MultipartFile file, HttpServletResponse response, HttpServletRequest request) throws IOException {

		try {
			String path = request.getSession().getServletContext().getRealPath(uploadPath);

			File attachmentFile = new File(path + File.separatorChar + file.getOriginalFilename());
			if (!attachmentFile.getParentFile().exists()) {
				attachmentFile.getParentFile().mkdirs();
			}
			file.transferTo(attachmentFile);

		} catch (Exception e) {
			logger.error("文件上传出现错误： ", e);
		}
		
		response.setCharacterEncoding("utf-8");
		response.getWriter().write("success");
	}
}
