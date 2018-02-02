package com.cnblogs.hoojo.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.cnblogs.hoojo.xss.FileHttpRequestWrapper;
import com.cnblogs.hoojo.xss.log.ApplicationLogging;
import com.cnblogs.hoojo.xss.wrapper.FileUploadRequestWrapper;
import com.cnblogs.hoojo.xss.wrapper.FileUploadRequestWrapper.FileItem;
import com.cnblogs.hoojo.xss.wrapper.FileUploadWrapper;

/**
 * examples xss
 * @author hoojo
 * @createDate 2018年1月24日 下午5:24:33
 * @file XssExampleController.java
 * @package com.cnblogs.hoojo.controller
 * @project xss-filter-web
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
@Controller
@RequestMapping("/xss/example")
public class XssExampleController extends ApplicationLogging {

	@Autowired
	private ResourceController res;
	
	@RequestMapping("/index")
	public String index() {
		
		return "redirect:index.jsp";
	}
	
	@RequestMapping("/form")
	public String _form() {
		return "form";
	}
	
	@RequestMapping("/form/submit")
	public String form(String name, String remark, Model model) {
		
		logger.info("name: {}, remark: {}", name, remark);
		model.addAttribute("name", name);
		model.addAttribute("remark", remark);
		return "form";
	}
	
	@RequestMapping("/whitelist")
	public String _whitelist() {
		return "whitelist";
	}
	
	@RequestMapping("/whitelist/submit")
	public String whitelist(String name, String remark, Model model) {
		
		logger.info("name: {}, remark: {}", name, remark);
		model.addAttribute("name", name);
		model.addAttribute("remark", remark);
		return "whitelist";
	}
	
	@RequestMapping("/upload")
	public String _upload() {
		return "upload";
	}
	
	@RequestMapping("/upload/submit")
	public void upload(@RequestParam("file") MultipartFile file, String name, String remark, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("name: {}, remark: {}", name, remark);
		
		try {
			res.upload(file, response, request);
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(String.format("name: %s, remark: %s, file: %s, orgname: %s", name, remark, file.getName(), file.getOriginalFilename()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/upload/submit1")
	public void uploadFileHttpRequestWrapper(String name, String remark, FileHttpRequestWrapper request, HttpServletResponse response) throws IOException {
		logger.info("name: {}, remark: {}", name, remark);

		try {
			String path = request.getSession().getServletContext().getRealPath(res.uploadPath);

			org.apache.commons.fileupload.FileItem file = request.getFileItem("file");
			File attachmentFile = new File(path + File.separatorChar + file.getName());
			if (!attachmentFile.getParentFile().exists()) {
				attachmentFile.getParentFile().mkdirs();
			}
			System.out.println(attachmentFile.getAbsolutePath());
			
			OutputStream output = new FileOutputStream(attachmentFile);
			IOUtils.copy(file.getInputStream(), output);
			IOUtils.closeQuietly(output);
			
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(String.format("name: %s, remark: %s, file: %s, orgname: %s", name, remark, file.getName(), file.getFieldName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/upload/submit1.1")
	public void uploadFileHttpRequestWrapper(String name, String remark, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("name: {}, remark: {}", name, remark);

		try {
			String path = request.getSession().getServletContext().getRealPath(res.uploadPath);

			FileHttpRequestWrapper req = (FileHttpRequestWrapper) request;
			org.apache.commons.fileupload.FileItem file = req.getFileItem("file");
			File attachmentFile = new File(path + File.separatorChar + file.getName());
			if (!attachmentFile.getParentFile().exists()) {
				attachmentFile.getParentFile().mkdirs();
			}
			System.out.println(attachmentFile.getAbsolutePath());
			
			OutputStream output = new FileOutputStream(attachmentFile);
			IOUtils.copy(file.getInputStream(), output);
			IOUtils.closeQuietly(output);
			
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(String.format("name: %s, remark: %s, file: %s, orgname: %s", name, remark, file.getName(), file.getFieldName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/upload/submit4")
	public void uploadFileUploadWrapper(String name, String remark, FileUploadWrapper request, HttpServletResponse response) throws IOException {
		logger.info("name: {}, remark: {}", name, remark);

		try {
			String path = request.getSession().getServletContext().getRealPath(res.uploadPath);
			String fileName = UUID.randomUUID().toString();

			FileUploadWrapper req = (FileUploadWrapper) request;
			org.apache.commons.fileupload.FileItem file = req.getFileItem("file");
			
			File attachmentFile = new File(path + File.separatorChar + fileName + "." + FilenameUtils.getExtension(file.getName()));
			if (!attachmentFile.getParentFile().exists()) {
				attachmentFile.getParentFile().mkdirs();
			}
			System.out.println(attachmentFile.getAbsolutePath());
			
			OutputStream output = new FileOutputStream(attachmentFile);
			IOUtils.copy(file.getInputStream(), output);
			output.close();
			
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(String.format("name: %s, remark: %s, file: %s, orgname: %s", name, remark, file.getName(), file.getFieldName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/upload/submit3")
	public void uploadFileUploadRequestWrapper(String name, String remark, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("name: {}, remark: {}", name, remark);

		try {
			
			String path = request.getSession().getServletContext().getRealPath(res.uploadPath);
			String fileName = UUID.randomUUID().toString();

			File attachmentFile = new File(path + File.separatorChar + fileName);
			if (!attachmentFile.getParentFile().exists()) {
				attachmentFile.getParentFile().mkdirs();
			}
			System.out.println(attachmentFile.getAbsolutePath());

			FileUploadRequestWrapper req = (FileUploadRequestWrapper) request;
			FileItem file = req.getFileItem("file");
			OutputStream output = new FileOutputStream(attachmentFile);
			IOUtils.write(file.getBytes(), output);
			output.close();
			
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(String.format("name: %s, remark: %s, file: %s, orgname: %s", name, remark, file.getFilename(), file.getFilename()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/upload/submit2")
	public String springUpload(HttpServletRequest request) throws IllegalStateException, IOException {
		long startTime = System.currentTimeMillis();
		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			Iterator iter = multiRequest.getFileNames();

			while (iter.hasNext()) {
				// 一次遍历所有文件
				MultipartFile file = multiRequest.getFile(iter.next().toString());
				if (file != null) {
					String path = "E:/springUpload" + file.getOriginalFilename();
					// 上传
					file.transferTo(new File(path));
				}

			}
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("方法三的运行时间：" + String.valueOf(endTime - startTime) + "ms");
		return "/success";
	}
}
