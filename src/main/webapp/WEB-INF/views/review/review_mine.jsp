<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- 기존 스타일 시트 재사용 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/css/store_list.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/css/review_list.css'/>">

<%-- 컨테이너: JS가 이동 경로를 파악할 수 있도록 데이터 주입 --%>
<div class="review-mine-wrapper"
	data-context-path="${pageContext.request.contextPath}">

	<%-- 상단 요약 헤더: 나의 활동 중심 --%>
	<div class="review-dashboard-card">
		<div class="review-header-flex">
			<div class="header-left">
				<span class="badge-wire">MY ACTIVITY</span>
				<h2 class="store-title">
					나의 리뷰 이력 <small>총 ${pageMaker.total}건</small>
				</h2>
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
								<%-- 가게 상세페이지로 바로가는 링크 추가 --%>
								<a href="<c:url value='/store/detail?storeId=${rev.store_id}'/>"
									class="user-name"
									style="text-decoration: none; color: inherit;"> 🏨
									${rev.store_name} <small style="color: #999;">❯</small>
								</a> <span class="stars"> <c:forEach begin="1"
										end="${rev.rating}">⭐</c:forEach>
								</span>
							</div>
							<div class="action-meta">
								<span class="date"> <fmt:formatDate
										value="${rev.review_date}" pattern="yyyy.MM.dd" />
								</span>
								<%-- 삭제 로직 유지 --%>
								<button type="button" class="btn-delete-review"
									data-review-id="${rev.review_id}"
									data-store-id="${rev.store_id}"
									data-return-url="/member/review/mine">삭제</button>
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
				<div class="review-empty-status">아직 작성하신 리뷰가 없습니다.</div>
			</c:otherwise>
		</c:choose>
	</div>

	<%-- 하단 페이징 섹션 --%>
	<div class="pagination-box">
		<ul class="pagination">
			<c:if test="${pageMaker.hasPreviousPage}">
				<li class="page-item"><a class="page-link"
					href="<c:url value='/member/review/mine?pageNum=${pageMaker.prePage}'/>"
					data-page="${pageMaker.prePage}">PREV</a></li>
			</c:if>

			<c:forEach var="num" items="${pageMaker.navigatepageNums}">
				<li class="page-item ${pageMaker.pageNum == num ? 'active' : ''}">
					<a class="page-link"
					href="<c:url value='/member/review/mine?pageNum=${num}'/>"
					data-page="${num}">${num}</a>
				</li>
			</c:forEach>

			<c:if test="${pageMaker.hasNextPage}">
				<li class="page-item"><a class="page-link"
					href="<c:url value='/member/review/mine?pageNum=${pageMaker.nextPage}'/>"
					data-page="${pageMaker.nextPage}">NEXT</a></li>
			</c:if>
		</ul>
	</div>

	<%-- 하단 네비게이션 버튼 --%>
	<div class="review-footer-nav">
		<button type="button" class="btn-wire-nav"
			onclick="location.href='<c:url value='/member/mypage'/>'">마이페이지로</button>
	</div>
</div>

<%-- 스크립트 분리 --%>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>
<script src="<c:url value='/resources/js/review_mine.js'/>"></script>

<jsp:include page="../common/footer.jsp" />
