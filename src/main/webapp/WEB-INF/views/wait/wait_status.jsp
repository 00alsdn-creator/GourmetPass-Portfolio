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

<style>
/* 이용 현황 전용 애니메이션 및 스타일 */
.dining-mode {
	border: 2px solid #2e7d32 !important;
	background-color: #f1f8e9 !important;
}

.dining-msg {
	font-size: 14px;
	color: #2e7d32;
	font-weight: 900;
	animation: pulse 2s infinite;
}

@
keyframes pulse { 0% {
	opacity: 1;
}

50
%
{
opacity
:
0.6;
}
100
%
{
opacity
:
1;
}
}
.status-card {
	margin-bottom: 25px;
	transition: 0.3s;
}

.status-card:hover {
	transform: translateY(-5px);
}

.badge-ing {
	background: #2e7d32;
	color: #fff;
}

.badge-call {
	background: #ff3d00;
	color: #fff;
	animation: shake 0.5s infinite;
}

@
keyframes shake { 0% {
	transform: rotate(0);
}

25
%
{
transform
:
rotate(
1deg
);
}
75
%
{
transform
:
rotate(
-1deg
);
}
100
%
{
transform
:
rotate(
0
);
}
}

/* 히스토리 전용 스타일 */
.history-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px;
	border-bottom: 1px solid #eee;
	transition: background 0.2s;
}

.history-item:hover {
	background-color: #fafafa;
}

.history-info {
	flex: 1;
}

.history-actions {
	display: flex;
	gap: 8px;
	align-items: center;
}

.btn-small {
	height: 32px;
	padding: 0 12px;
	font-size: 12px;
	font-weight: 800;
	border-radius: 4px;
	cursor: pointer;
}

.btn-review {
	background: #ff3d00;
	color: #fff;
	border: none;
}

.btn-payment {
	background: #fff;
	color: #333;
	border: 1px solid #ddd;
}

.text-done {
	color: #ccc;
	font-size: 12px;
	font-weight: 800;
}
</style>

<%-- 실시간 알림 라이브러리 --%>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<script>

//[수정] const 대신 var를 사용하거나, 기존 객체에 속성을 추가하는 방식
// header.jsp에서 이미 선언된 APP_CONFIG를 활용합니다.

APP_CONFIG.userId = "<sec:authentication property='principal.username'/>";
APP_CONFIG.activeStoreId = "${not empty activeWait ? activeWait.store_id : (not empty activeBook ? activeBook.store_id : '')}";
    function connectRealtime() {
        const socket = new SockJS(APP_CONFIG.contextPath + '/ws_waiting');
        const stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function (frame) {
            stompClient.subscribe('/topic/wait/' + APP_CONFIG.userId, function () { location.reload(); });
            if (APP_CONFIG.activeStoreId) {
                stompClient.subscribe('/topic/store/' + APP_CONFIG.activeStoreId + '/waitUpdate', function () { location.reload(); });
            }
        }, function() { setTimeout(connectRealtime, 5000); });
    }

    document.addEventListener("DOMContentLoaded", function() {
        if (APP_CONFIG.userId) connectRealtime();
    });
</script>

<div class="edit-wrapper" style="max-width: 900px;">
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
						<div
							style="display: flex; justify-content: space-between; align-items: center;">
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
								<h3 style="font-size: 22px; font-weight: 900; margin: 10px 0;">${activeWait.store_name}</h3>
								<p style="font-size: 15px; color: #555;">
									<c:choose>
										<c:when test="${activeWait.wait_status == 'ING'}">
											<span class="dining-msg">맛있는 식사 되세요!</span>
										</c:when>
										<c:otherwise>대기 번호: <b style="color: #ff3d00;">${activeWait.wait_num}번</b>
											<c:if test="${not empty aheadCount}">(내 앞 <b
													style="color: #ff3d00;">${aheadCount}</b>팀)</c:if> / ${activeWait.people_cnt}명</c:otherwise>
									</c:choose>
								</p>
							</div>
							<div class="history-actions">
								<c:if test="${activeWait.wait_status == 'WAITING'}">
									<button type="button" class="btn-wire"
										style="color: #dc3545; border-color: #dc3545;"
										onclick="cancelWait('${activeWait.wait_id}')">웨이팅 취소</button>
								</c:if>
								<c:if test="${activeWait.wait_status == 'ING'}">
									<button class="btn-small btn-payment"
										onclick="alert('결제 상세 기능 준비 중입니다.')">주문 확인</button>
								</c:if>
							</div>
						</div>
					</div>
				</c:if>

				<%-- 예약 카드 --%>
				<%-- 예약 카드 내부 --%>
				<c:if test="${not empty activeBook}">
					<div class="item-card status-card">
						<div
							style="display: flex; justify-content: space-between; align-items: center;">
							<div class="history-info">
								<span class="badge-wire">📅 예약 확정</span>
								<h3 style="font-size: 22px; font-weight: 900; margin: 10px 0;">${activeBook.store_name}</h3>
								<p style="font-size: 15px; color: #555;">
									방문 일시: <b><fmt:formatDate value="${activeBook.book_date}"
											pattern="MM월 dd일 HH:mm" /></b>
								</p>
							</div>

							<div class="history-actions" style="text-align: right;">
								<div
									style="font-weight: 900; font-size: 14px; color: #2e7d32; margin-bottom: 10px;">방문
									예정</div>

								<%-- 예약 취소 폼 --%>
								<form action="<c:url value='/book/updateStatus'/>" method="post"
									id="userCancelForm">
									<input type="hidden" name="book_id" value="${activeBook.book_id}"> 
									<input type="hidden" name="_csrf" value="${_csrf.token}" />
									
									<%-- 취소 버튼 --%>
									<button type="button"
										class="btn-step btn-step-danger user-cancel-btn"
										data-payid="${activeBook.pay_id}"
										style="padding: 8px 15px; border-radius: 5px; cursor: pointer;">
										예약 취소</button>
								</form>
							</div>
						</div>
					</div>
				</c:if>
			</c:when>
			<c:otherwise>
				<div
					style="text-align: center; padding: 50px 0; color: #999; font-weight: 800;">현재
					이용 중인 서비스가 없습니다.</div>
			</c:otherwise>
		</c:choose>
	</div>

	<%-- 2. 이용 히스토리 (결제 및 리뷰 통합) --%>
	<div class="dashboard-card" style="margin-top: 30px;">
		<div class="card-header">
			<h3 class="card-title">📜 전체 이용 내역</h3>
			<span class="badge-wire">최근 방문 순</span>
		</div>

		<div class="history-container">
			<%-- 웨이팅 히스토리 --%>
			<c:forEach var="w" items="${my_wait_list}">
				<div class="history-item">
					<div class="history-info">
						<div
							style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
							<span style="font-size: 12px; color: #999; font-weight: 700;">[웨이팅]</span>
							<span style="font-size: 13px; color: #666;"><fmt:formatDate
									value="${w.wait_date}" pattern="yy.MM.dd" /></span>
						</div>
						<h4 style="font-size: 17px; font-weight: 800; margin: 0;">${w.store_name}</h4>
					</div>

					<div class="history-actions">
						<%-- 리뷰 작성 버튼 (방문 완료 상태이고 리뷰가 없을 때만) --%>
						<c:if test="${w.wait_status == 'FINISH'}">
							<button class="btn-small btn-payment"
								onclick="alert('결제/영수증 상세 페이지로 이동합니다.')">결제내역</button>
							<c:choose>
								<c:when test="${empty w.review_id}">
									<button class="btn-small btn-review"
										onclick="location.href='<c:url value='/review/write?store_id=${w.store_id}&wait_id=${w.wait_id}'/>'">리뷰
										작성</button>
								</c:when>
								<c:otherwise>
									<span class="text-done">리뷰완료</span>
								</c:otherwise>
							</c:choose>
						</c:if>
						<c:if test="${w.wait_status == 'CANCELLED'}">
							<span class="text-done" style="color: #dc3545">취소됨</span>
						</c:if>
						
					</div>
				</div>
			</c:forEach>

			<%-- 예약 히스토리 --%>
			<c:forEach var="b" items="${my_book_list}">
				<div class="history-item">
					<div class="history-info">
						<div
							style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
							<span style="font-size: 12px; color: #999; font-weight: 700;">[예약]</span>
							<span style="font-size: 13px; color: #666;"><fmt:formatDate
									value="${b.book_date}" pattern="yy.MM.dd" /></span>
						</div>
						<h4 style="font-size: 17px; font-weight: 800; margin: 0;">${b.store_name}</h4>
					</div>

					<div class="history-actions">
						<c:if test="${b.book_status == 'FINISH'}">
							<button class="btn-small btn-payment"
								onclick="alert('결제 상세 정보를 확인합니다.')">결제내역</button>
							<c:choose>
								<c:when test="${empty b.review_id}">
									<button class="btn-small btn-review"
										onclick="location.href='<c:url value='/review/write?store_id=${b.store_id}&book_id=${b.book_id}'/>'">리뷰
										작성</button>
								</c:when>
								<c:otherwise>
									<span class="text-done">리뷰완료</span>
								</c:otherwise>
							</c:choose>
						</c:if>
						<c:if test="${b.book_status == 'RESERVED'}">
							<span class="text-done" style="color: #2e7d32">방문예정</span>
						</c:if>
						<c:if test="${b.book_status == 'CANCELED'}">
							<span class="text-done" style="color: #bd2222">예약취소</span>
						</c:if>
						<c:if test="${b.book_status == 'NOSHOW'}">
							<span class="text-done" style="color: #B22222 ">NO-SHOW</span>
						</c:if>
					</div>
				</div>
			</c:forEach>
		</div>
	</div>
</div>

<script src="<c:url value='/resources/js/mypage.js'/>"></script>
<script src="<c:url value='/resources/js/wait_status.js'/>"></script>
<jsp:include page="../common/footer.jsp" />