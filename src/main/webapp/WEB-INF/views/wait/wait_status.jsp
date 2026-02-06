<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- 공용 스타일시트 연결 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/css/wait_status.css'/>">
<link rel="stylesheet"
	href="<c:url value='/resources/css/review_list.css'/>">

<%-- 실시간 알림 라이브러리 --%>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<div class="edit-wrapper wait-status-wrapper" style="max-width: 1100px; margin: 40px auto;"
	data-user-id="<sec:authentication property='principal.username'/>"
	data-active-store-id="${not empty activeWait ? activeWait.store_id : (not empty activeBook ? activeBook.store_id : '')}">
	<div class="edit-title">📅 나의 실시간 이용 현황</div>

	<%-- 1. 진행 중인 서비스 (실시간 카드) --%>
	<div class="dashboard-card">
		<div class="card-header">
			<h3 class="card-title">🔥 진행 중인 서비스</h3>
			<span class="badge-wire">현재 활동 중</span>
		</div>

		<c:choose>
			<c:when test="${not empty activeWait or not empty activeBook}">
				<%-- 웨이팅 카드 --%>
				<c:if test="${not empty activeWait}">
					<div
						class="item-card status-card ${activeWait.wait_status == 'ING' ? 'dining-mode' : ''}">
						<div class="status-card-row">
							<div class="history-info">
								<c:choose>
									<c:when test="${activeWait.wait_status == 'ING'}">
										<span class="badge-wire badge-ing">🍽️ 식사 중</span>
									</c:when>
									<c:when test="${activeWait.wait_status == 'CALLED'}">
										<span class="badge-wire badge-call">📢 입장 호출!</span>
									</c:when>

									<c:when
										test="${activeWait.wait_status == 'WAITING' and aheadCount == 0}">
										<span class="badge-wire badge-call">🚀 곧 입장!</span>
									</c:when>


									<c:otherwise>
										<span class="badge-wire">🚶 웨이팅 중</span>
									</c:otherwise>
								</c:choose>
								<h3 class="status-store-name">${activeWait.store_name}</h3>
								<p class="status-subtext">
									<c:choose>
										<c:when test="${activeWait.wait_status == 'ING'}">
											<span class="dining-msg">맛있는 식사 되세요!</span>
										</c:when>
										<c:otherwise>대기 번호: <b class="status-highlight">${activeWait.wait_num}번</b>
											<c:if test="${not empty aheadCount}">(내 앞 <b
													class="status-highlight">${aheadCount}</b>팀)</c:if> / ${activeWait.people_cnt}명</c:otherwise>
									</c:choose>
								</p>
							</div>
							<div class="history-actions">
								<c:if test="${activeWait.wait_status == 'WAITING'}">
									<button type="button"
										class="btn-wire btn-danger-outline wait-cancel-btn"
										data-wait-id="${activeWait.wait_id}">웨이팅 취소</button>
								</c:if>
								<c:if test="${activeWait.wait_status == 'ING'}">
									<button class="btn-small btn-payment js-alert"
										data-message="결제 상세 기능 준비 중입니다.">주문 확인</button>
								</c:if>
							</div>
						</div>
					</div>
				</c:if>

				<%-- 예약 카드 --%>
				<%-- 예약 카드 내부 --%>
				<c:if test="${not empty activeBook}">
					<div class="item-card status-card">
						<div class="status-card-row">
							<div class="history-info">
								<span class="badge-wire">📅 예약 확정</span>
								<h3 class="status-store-name">${activeBook.store_name}</h3>
								<p class="status-subtext">
									방문 일시: <b><fmt:formatDate value="${activeBook.book_date}"
											pattern="MM월 dd일 HH:mm" /></b>
								</p>
							</div>

							<div class="history-actions history-actions-right">
								<div class="status-visit-label">방문 예정</div>

								<%-- 예약 취소 폼 --%>
								<form action="<c:url value='/book/updateStatus'/>" method="post"
									id="userCancelForm">
									<input type="hidden" name="book_id"
										value="${activeBook.book_id}"> <input type="hidden"
										name="_csrf" value="${_csrf.token}" />

									<%-- 취소 버튼 --%>
									<button type="button"
										class="btn-step btn-step-danger user-cancel-btn btn-cancel"
										data-payid="${activeBook.pay_id}">예약 취소</button>
								</form>
							</div>
						</div>
					</div>
				</c:if>
			</c:when>
			<c:otherwise>
				<div class="status-empty">현재 이용 중인 서비스가 없습니다.</div>
			</c:otherwise>
		</c:choose>
	</div>

	<%-- 2. 이용 히스토리 (결제 및 리뷰 통합) --%>
	<div class="dashboard-card status-history-card">
		<div class="card-header"
			style="display: flex; justify-content: space-between; align-items: center;">
			<h3 class="card-title">📜 최근 이용 내역</h3>&nbsp;&nbsp;
			<a href="<c:url value='/member/history'/>" class="btn-wire"
				style="height: 32px; line-height: 30px; padding: 0 12px; font-size: 12px; text-decoration: none; color: #333;">
				전체보기 ❯ </a>
		</div>

		<div class="history-container">
			<c:set var="displayCount" value="0" />

			<%-- 웨이팅 히스토리 --%>
			<c:forEach var="w" items="${my_wait_list}">
				<c:if test="${displayCount < 3}">
					<div class="history-item">
						<div class="history-info">
							<div class="history-meta">
								<span class="history-tag">[웨이팅]</span> <span
									class="history-date"><fmt:formatDate
										value="${w.wait_date}" pattern="yy.MM.dd" /></span>
							</div>
							<h4 class="history-store">${w.store_name}</h4>
						</div>

						<div class="history-actions">
							<%-- 리뷰 작성 버튼 (방문 완료 상태이고 리뷰가 없을 때만) --%>
							<c:if test="${w.wait_status == 'FINISH'}">
								<!-- 							<button class="btn-small btn-payment"
								onclick="alert('결제/영수증 상세 페이지로 이동합니다.')">결제내역</button>
 -->
								<c:choose>
									<c:when test="${empty w.review_id}">
										<button class="btn-small btn-review js-review-link"
											data-url="<c:url value='/review/write?store_id=${w.store_id}&wait_id=${w.wait_id}'/>">리뷰
											작성</button>
									</c:when>
									<c:otherwise>
										<span class="text-done">리뷰완료</span>
										<button type="button" class="btn-delete-review"
											data-review-id="${w.review_id}" data-store-id="${w.store_id}"
											data-return-url="/member/wait_status">삭제</button>
									</c:otherwise>

								</c:choose>
							</c:if>
							<c:if test="${w.wait_status == 'CANCELLED'}">
								<span class="text-done text-done--danger">취소됨</span>
							</c:if>

						</div>
					</div>
					<c:set var="displayCount" value="${displayCount + 1}" />
				</c:if>
			</c:forEach>

			<%-- 예약 히스토리 --%>
			<c:forEach var="b" items="${my_book_list}">
				<c:if test="${displayCount < 3}">
					<div class="history-item">
						<div class="history-info">
							<div class="history-meta">
								<span class="history-tag">[예약]</span> <span class="history-date"><fmt:formatDate
										value="${b.book_date}" pattern="yy.MM.dd" /></span>
							</div>
							<h4 class="history-store">${b.store_name}</h4>
						</div>

						<div class="history-actions">
							<c:if test="${b.book_status == 'FINISH'}">
								<button class="btn-small btn-payment js-alert"
									data-message="결제 상세 정보를 확인합니다.">결제내역</button>
								<c:choose>
									<c:when test="${empty b.review_id}">
										<button class="btn-small btn-review js-review-link"
											data-url="<c:url value='/review/write?store_id=${b.store_id}&book_id=${b.book_id}'/>">리뷰
											작성</button>
									</c:when>
									<c:otherwise>
										<span class="text-done">리뷰완료</span>
										<!-- ✨ 삭제 버튼 추가 -->
										<button type="button" class="btn-delete-review"
											data-review-id="${b.review_id}" data-store-id="${b.store_id}"
											data-return-url="/member/wait_status">삭제</button>
									</c:otherwise>
								</c:choose>
							</c:if>
							<c:if test="${b.book_status == 'RESERVED'}">
								<span class="text-done text-done--success">방문예정</span>
							</c:if>
							<c:if test="${b.book_status == 'CANCELED'}">
								<span class="text-done text-done--cancel">예약취소</span>
							</c:if>
							<c:if test="${b.book_status == 'NOSHOW'}">
								<span class="text-done text-done--noshow">NO-SHOW</span>
							</c:if>
						</div>
					</div>
					<c:set var="displayCount" value="${displayCount + 1}" />
				</c:if>
			</c:forEach>
		</div>
	</div>
</div>

<script src="<c:url value='/resources/js/mypage.js'/>"></script>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>
<script src="<c:url value='/resources/js/wait_status.js'/>"></script>

<script>
	// 리뷰 삭제 버튼 이벤트 처리
	document.addEventListener('click', function(e) {
		if (e.target.classList.contains('btn-delete-review')) {
			const reviewId = e.target.dataset.reviewId;
			const storeId = e.target.dataset.storeId;
			const returnUrl = e.target.dataset.returnUrl;

			if (typeof confirmDeleteReview === 'function') {
				confirmDeleteReview(reviewId, storeId, returnUrl);
			}
		}
	});
</script>

<jsp:include page="../common/footer.jsp" />
