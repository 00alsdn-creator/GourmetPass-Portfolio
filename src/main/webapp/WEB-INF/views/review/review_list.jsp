<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- 1. 스타일시트 분리 (외부 파일 호출) --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/store_list.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/review_list.css'/>">

<%-- 2. 컨테이너에 JS가 참조할 데이터 속성(data-) 주입 --%>
<div class="review-list-wrapper" 
     data-store-id="${store.store_id}" 
     data-context-path="${pageContext.request.contextPath}">
    
    <%-- 상단 요약 헤더: 인라인 스타일 제거 및 클래스화 --%>
    <div class="review-dashboard-card">
        <div class="review-header-flex">
            <div class="header-left">
                <span class="badge-wire">REVIEW BOARD</span>
                <h2 class="store-title">${store.store_name} <small>전체 리뷰</small></h2>
            </div>
            <div class="header-right">
                <div class="rating-display">
                    ⭐ ${store.avg_rating} <span class="total-count">(${pageMaker.total}건)</span>
                </div>
            </div>
        </div>
    </div>

    <%-- 리뷰 목록 섹션 --%>
    <div class="review-container">
        <c:choose>
            <c:when test="${not empty allReviews}">
                <c:forEach var="rev" items="${allReviews}">
                    <div class="item-card">
						<div class="item-header">
							<div class="user-meta">
								<strong class="user-name">${rev.user_nm} <small>고객님</small></strong>
								<span class="stars"> <c:forEach begin="1"
										end="${rev.rating}">⭐</c:forEach>
								</span>
							</div>
							<div class="action-meta">
								<span class="date"> <fmt:formatDate
										value="${rev.review_date}" pattern="yyyy.MM.dd" />
								</span>
								<sec:authorize access="isAuthenticated()">
									<c:if
										test="${rev.user_id == pageContext.request.userPrincipal.name}">
										<button type="button" class="btn-delete-review"
											data-review-id="${rev.review_id}"
											data-store-id="${rev.store_id}"
											data-return-url="/review/list?store_id=${rev.store_id}">삭제</button>
									</c:if>
								</sec:authorize>

							</div>
						</div>

						<div class="item-body">
                            <c:if test="${not empty rev.img_url}">
                                <div class="img-box">
                                    <img src="<c:url value='/upload/${rev.img_url}'/>">
                                </div>
                            </c:if>
                            <div class="content-box">
                                <p>${rev.content}</p>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="review-empty-status">아직 등록된 리뷰가 없습니다.</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 3. 하단 페이징 섹션: onclick 제거 및 data-page 속성 추가 --%>
    <div class="pagination-box">
        <ul class="pagination">
            <c:if test="${pageMaker.hasPreviousPage}">
                <li class="page-item">
                    <c:url var="prevUrl" value="/review/list">
                        <c:param name="store_id" value="${store.store_id}" />
                        <c:param name="pageNum" value="${pageMaker.prePage}" />
                        <c:param name="pageSize" value="${pageMaker.pageSize}" />
                    </c:url>
                    <a class="page-link" href="${prevUrl}" data-page="${pageMaker.prePage}">PREV</a>
                </li>
            </c:if>

            <c:forEach var="num" items="${pageMaker.navigatepageNums}">
                <li class="page-item ${pageMaker.pageNum == num ? 'active' : ''}">
                    <c:url var="pageUrl" value="/review/list">
                        <c:param name="store_id" value="${store.store_id}" />
                        <c:param name="pageNum" value="${num}" />
                        <c:param name="pageSize" value="${pageMaker.pageSize}" />
                    </c:url>
                    <a class="page-link" href="${pageUrl}" data-page="${num}">${num}</a>
                </li>
            </c:forEach>

            <c:if test="${pageMaker.hasNextPage}">
                <li class="page-item">
                    <c:url var="nextUrl" value="/review/list">
                        <c:param name="store_id" value="${store.store_id}" />
                        <c:param name="pageNum" value="${pageMaker.nextPage}" />
                        <c:param name="pageSize" value="${pageMaker.pageSize}" />
                    </c:url>
                    <a class="page-link" href="${nextUrl}" data-page="${pageMaker.nextPage}">NEXT</a>
                </li>
            </c:if>
        </ul>
    </div>

    <%-- 하단 네비게이션 버튼 --%>
    <div class="review-footer-nav">
        <button type="button" class="btn-wire-nav" id="btn-go-store">가게 상세로</button>
        <button type="button" class="btn-wire-nav" id="btn-go-back">이전 페이지로</button>
    </div>
</div>

<%-- 4. 스크립트 분리 (로직 제거 후 외부 파일 호출) --%>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>
<script src="<c:url value='/resources/js/review_list.js'/>"></script>

<jsp:include page="../common/footer.jsp" />
