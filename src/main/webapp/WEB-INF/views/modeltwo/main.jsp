<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String loginID = (String)session.getAttribute("loginID");
    if(loginID == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head><title>메인 페이지</title></head>
<body>
    <table border="1">
        <tr>
            <td align="center">
                <%= loginID %>님 환영합니다
            </td>
        </tr>
        <tr>
            <td align="center">
                <a href="../logout.do">로그아웃</a>
            </td>
        </tr>
    </table>
    <script>
    console.log("카카오키: " + "${kakao.js.key}");
</script>
</body>
</html>