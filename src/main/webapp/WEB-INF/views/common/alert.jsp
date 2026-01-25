<%@ page contentType="text/html; charset=UTF-8" %>
<script>
  alert("${msg}");
  location.replace("${pageContext.request.contextPath}${url}");
</script>
