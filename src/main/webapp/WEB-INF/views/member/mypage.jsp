<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- [관심사 분리] 공용 마이페이지 스타일 및 통합 스크립트 연결 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<script src="<c:url value='/resources/js/mypage.js'/>"></script>

<div class="mypage-wrapper">
	<div class="profile-card">
		<div class="profile-info">
			<span class="profile-label">MEMBER PROFILE</span>
			<h2 class="user-name">${member.user_nm}
				<small>님</small>
			</h2>
			<p class="user-meta">ID: ${member.user_id} | TEL:
				${member.user_tel}</p>
		</div>
		<div class="btn-group" style="margin: 0; width: auto;">
			<a href="<c:url value='/member/edit'/>" class="btn-wire"
				style="height: 45px; padding: 0 20px; font-size: 14px;">정보 수정</a>
			<form action="<c:url value='/logout'/>" method="post"
				style="display: inline;">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
				<button type="submit" class="btn-wire btn-logout"
					style="height: 45px; padding: 0 20px; font-size: 14px; margin-left: 10px;">로그아웃</button>
			</form>
		</div>
	</div>

	<%-- mypage.jsp 내부 핵심 버튼 링크 수정 --%>
	<div class="menu-container">
		<a href="<c:url value='/member/wait_status'/>" class="status-btn-full">
			📅 나 의 이 용 현 황 (예약 / 웨이팅) </a>
	</div>
	<hr class="section-divider">

	<div class="dashboard-card">
		<div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
			<%-- [수정] 컨트롤러에서 전달받은 전체 개수(total_review_cnt) 표시 --%>
			<h3 class="card-title">💬 최근 리뷰 기록 (${total_review_cnt})</h3>
			
			<%-- [추가] 전체보기 링크: 신규 생성할 전체 이력 페이지(/member/review/mine)로 연결 --%>
			<a href="<c:url value='/member/review/mine'/>" class="btn-wire" 
			   style="height: 32px; line-height: 30px; padding: 0 12px; font-size: 12px; text-decoration: none; color: #333;">전체보기 ❯</a>
		</div>

		<div class="review-list">
			<c:choose>
				<c:when test="${not empty my_review_list}">
					<c:forEach var="review" items="${my_review_list}">
						<%-- 컨트롤러에서 3개만 리스트에 담아 보내주므로 상위 3개만 자동 출력됨 --%>
						<div class="item-card">
							<div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 15px;">
								<div class="store-link-box">
									<a href="<c:url value='/store/detail?storeId=${review.store_id}'/>"
										style="font-size: 18px; font-weight: 900; color: #333; text-decoration: none;">
										🏨 ${review.store_name} <small style="font-weight: normal; color: #999;">❯</small>
									</a>
									<div style="margin-top: 5px; color: #f1c40f;">
										<c:forEach begin="1" end="${review.rating}">⭐</c:forEach>
									</div>
								</div>
								<button type="button" class="btn-wire"
									style="height: 32px; padding: 0 12px; font-size: 12px; color: #dc3545; border-color: #dc3545;"
									onclick="confirmDeleteReview('${review.review_id}', '${review.store_id}')">삭제</button>
							</div>
							<p style="line-height: 1.6; font-size: 15px; color: #444; margin-bottom: 15px;">${review.content}</p>
							<div style="font-size: 13px; color: #aaa; font-weight: 800;">
								<fmt:formatDate value="${review.review_date}"
									pattern="yyyy.MM.dd" />
							</div>
						</div>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<div style="text-align: center; padding: 60px 0; color: #ccc; font-weight: 900;">
						아직 작성된 리뷰 기록이 없습니다.
					</div>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</div>

<jsp:include page="../common/footer.jsp" />