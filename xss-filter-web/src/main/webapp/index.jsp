<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0,user-scalable=no" name="viewport" />
	<meta name="renderer" content="webkit|ie-comp|ie-stand" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
</head>

<body>
	<a href="${pageContext.request.contextPath}/xss/example/form">过滤文本css/js/html/sql</a><br/>
	<a href="${pageContext.request.contextPath}/xss/example/whitelist">白名单，不过滤</a><br/>
	<a href="${pageContext.request.contextPath}/xss/example/upload">文件上传</a><br/>
</body>
</html>