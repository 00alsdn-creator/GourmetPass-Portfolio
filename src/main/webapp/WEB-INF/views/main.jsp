<%-- WEB-INF/views/main.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="common/header.jsp" />

<%-- [원칙 1] 표준 스타일시트 연결: member.css(공통), main.css(전용) --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/main.css'/>">

<div class="main-wrapper">
    <%-- 1. 검색 섹션: Bold Wire 스타일 적용 --%>
    <div class="search-card">
        <h1 class="search-title">🍴 오늘 어떤 맛집을 예약할까요?</h1>
        <form action="${pageContext.request.contextPath}/store/list" method="get" class="search-form">
            <input type="text" name="keyword" class="search-input" placeholder="가게 이름 또는 메뉴 검색" required>
            <button type="submit" class="btn-search">맛집 검색</button>
        </form>
    </div>

    <%-- 2. 카테고리 섹션: store_list와 동일한 Chip 디자인 --%>
    <div class="category-section">
        <div class="chip-group">
            <c:set var="categories" value="한식,일식,양식,중식,카페" />
            <c:forEach var="cat" items="${fn:split(categories, ',')}">
                <%-- data-url을 활용하여 JS에서 일괄 처리 (관심사 분리) --%>
                <div class="cat-chip clickable" data-url="${pageContext.request.contextPath}/store/list?category=${cat}">${cat}</div>
            </c:forEach>
            <div class="cat-chip btn-all clickable" data-url="${pageContext.request.contextPath}/store/list">전체보기</div>
        </div>
    </div>

    <div class="main-divider"></div>

    <%-- 3. 실시간 인기 맛집 섹션 (TOP 6) --%>
    <div class="popular-section">
        <h2 class="section-title">🔥 실시간 인기 맛집</h2>

        <div class="store-grid">
            <c:choose>
                <c:when test="${not empty storeList}">
                    <c:forEach var="store" items="${storeList}">
                        <%-- 데이터 속성을 활용한 카드 인터랙션 --%>
                        <div class="store-card clickable" data-url="${pageContext.request.contextPath}/store/detail?storeId=${store.store_id}">
                            <div class="store-img-box">
                                <c:choose>
                                    <c:when test="${not empty store.store_img}">
                                        <img src="${pageContext.request.contextPath}/upload/${store.store_img}" alt="${store.store_name}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="no-img-placeholder">NO IMAGE</div>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" class="favorite-toggle" data-store-id="${store.store_id}">🤍</button>
                            </div>
                            <div class="store-info">
                                <div class="badge-cat">${store.store_category}</div>
                                <div class="store-name-row">
                                    <h3 class="store-name">${store.store_name}</h3>
                                    <%-- 조회수 100회 이상 매장에 HOT 배지 부여 --%>
                                    <c:if test="${store.store_cnt >= 100}">
                                        <span class="hot-badge">HOT</span>
                                    </c:if>
                                </div>
                                <div class="store-addr-text">${store.store_addr1}</div>
                                <div class="store-stats">
                                    <span class="stat-rating">⭐ ${store.avg_rating} <small>(${store.review_cnt})</small></span>
                                    <span class="stat-views">👀 조회수 ${store.store_cnt}</span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="empty-status-box">
                        현재 등록된 인기 맛집 정보가 없습니다.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%-- [원칙 2] 공통 인터랙션 스크립트 연결 --%>
<script src="<c:url value='/resources/js/main.js'/>"></script>
<jsp:include page="common/footer.jsp" />
