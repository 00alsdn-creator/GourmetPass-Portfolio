<%-- WEB-INF/views/common/header.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gourmet Pass</title>

    <meta name="app-context-path" content="${pageContext.request.contextPath}">
    <meta name="csrf-name" content="${_csrf.parameterName}">
    <meta name="csrf-token" content="${_csrf.token}">

    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="<c:url value='/resources/js/app-config.js'/>"></script>
</head>
<body>
    <nav class="wire-nav">
        <div class="nav-inner">
            <h2 class="logo-text">
                <a href="<c:url value='/'/>">GOURMET PASS</a>
            </h2>
            <div class="nav-links">
                <a href="<c:url value='/store/list'/>" class="nav-item">맛집 검색</a>
                
                <sec:authorize access="isAnonymous()">
                    <span class="divider">|</span>
                    <a href="<c:url value='/member/login'/>" class="nav-item">로그인</a>
                    <span class="divider">|</span>
                    <a href="<c:url value='/member/signup/select'/>" class="nav-item">회원가입</a>
                </sec:authorize>
                
                <sec:authorize access="isAuthenticated()">
                    <sec:authentication property="principal" var="user" />
                    <span class="divider">|</span>
                    <span class="user-welcome"><b>${user.username}</b>님</span>
                    
                    <sec:authorize access="hasRole('ROLE_OWNER')">
                        <a href="<c:url value='/member/mypage'/>" class="nav-item owner-link">[매장관리]</a>
                    </sec:authorize>
                    <sec:authorize access="hasRole('ROLE_USER')">
                        <a href="<c:url value='/member/mypage'/>" class="nav-item">[마이페이지]</a>
                    </sec:authorize>
                    
                    <form action="<c:url value='/logout'/>" method="post" class="logout-form-inline">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        <button type="submit" class="btn-logout-text">로그아웃</button>
                    </form>
                </sec:authorize>
            </div>
        </div>
    </nav>

    <div class="page-wrapper">
