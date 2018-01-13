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
	<form action="${pageContext.request.contextPath}/xss/example/upload/submit" enctype="multipart/form-data" method="post">
		<input type="text" name="name" value="${param.name }"/>
		<textarea name="remark">${remark }</textarea>
		<input type="file" name="file"/>
		
		<input type="submit"/>
	</form>
</body>
</html>