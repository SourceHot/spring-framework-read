<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="1.0">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<html>
			<head>
				<style>
					table.emp {
					border-collapse: collapse;
					}
					table.emp, table.emp th, table.emp td {
					border: 1px solid gray;
					}
				</style>
			</head>
			<body>
				<div align="center">
					<xsl:apply-templates/>
				</div>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="employees">
		<table class="emp" style="width:100%;">
			<tr bgcolor="#eee">
				<th>Id</th>
				<th>Name</th>
				<th>Department</th>
			</tr>
			<xsl:for-each select="employee">
				<tr>
					<td>
						<xsl:value-of select="id"/>
					</td>
					<td>
						<xsl:value-of select="name"/>
					</td>
					<td>
						<xsl:value-of select="dept"/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>