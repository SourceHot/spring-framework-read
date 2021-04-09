<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<html>
<head>
    <link rel="stylesheet" href="<spring:theme code='stylesheet'/>" type="text/css" />
    <title>Spring MVC ThemeResolver Example</title>
</head>
<body>

<h3>Spring MVC ThemeResolver Example</h3>
theme: <a href="/theme?theme=bright">bright</a> | <a href="/theme?theme=dark">dark</a>

</body>
</html>