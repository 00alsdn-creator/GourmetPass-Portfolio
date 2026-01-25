<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- 외부 스타일 시트로 완전 분리 [manage.css] --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/manage.css'/>">

<div class="edit-wrapper" style="max-width: 1100px; margin: 40px auto;">
	<div class="edit-title">⚙️ 실시간 매장 관리 센터</div>

	<%-- 1. 실시간 웨이팅 섹션 --%>
	<div class="dashboard-card">
		<%-- [로직 교정] 실제 입장 대기 중인(WAITING, CALLED) 팀만 합산 --%>
		<c:set var="realWaitCount" value="0" />
		<c:forEach var="w" items="${store_wait_list}">
			<c:if
				test="${w.wait_status == 'WAITING' or w.wait_status == 'CALLED'}">
				<c:set var="realWaitCount" value="${realWaitCount + 1}" />
			</c:if>
		</c:forEach>

		<div class="card-header-flex">
			<h3 class="card-title-wait">🚶 실시간 웨이팅 현황</h3>
			<span class="badge-wire">입장 대기 <b>${realWaitCount}</b>팀
			</span>
		</div>

		<table class="manage-dashboard-table">
			<thead>
				<tr>
					<th class="col-num">번호</th>
					<th class="col-userid">고객ID</th>
					<th class="col-people">인원</th>
					<th class="col-status">상태</th>
					<th class="col-action">상태 제어</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="wait" items="${store_wait_list}">
					<%-- 관리 종료된 건은 제외하고 노출 --%>
					<c:if
						test="${wait.wait_status != 'FINISH' and wait.wait_status != 'CANCELLED'}">
						<tr>
							<td align="center"><b>${wait.wait_num}번</b></td>
							<td align="center">${wait.user_id}</td>
							<td align="center">${wait.people_cnt}명</td>
							<td align="center"><c:choose>
									<c:when test="${wait.wait_status == 'WAITING'}">
										<span class="badge-wire">대기중</span>
									</c:when>
									<c:when test="${wait.wait_status == 'CALLED'}">
										<span class="badge-wire badge-called">호출중</span>
									</c:when>
									<c:when test="${wait.wait_status == 'ING'}">
										<span class="badge-wire badge-ing">식사중</span>
									</c:when>
								</c:choose></td>
							<td align="center">
								<form action="<c:url value='/store/wait/updateStatus'/>"
									method="post" class="action-btn-group">
									<input type="hidden" name="wait_id" value="${wait.wait_id}">
									<input type="hidden" name="user_id" value="${wait.user_id}">
									<input type="hidden" name="${_csrf.parameterName}"
										value="${_csrf.token}" />

									<c:choose>
										<c:when test="${wait.wait_status == 'WAITING'}">
											<button type="submit" name="status" value="CALLED"
												class="btn-step btn-step-primary">지금 호출하기</button>
											<button type="submit" name="status" value="CANCELLED"
												class="btn-step btn-step-danger">취소</button>
										</c:when>
										<c:when test="${wait.wait_status == 'CALLED'}">
											<button type="submit" name="status" value="ING"
												class="btn-step btn-step-next">입장 확인</button>
											<button type="submit" name="status" value="CANCELLED"
												class="btn-step btn-step-danger">노쇼 처리</button>
										</c:when>
										<c:when test="${wait.wait_status == 'ING'}">
											<button type="submit" name="status" value="FINISH"
												class="btn-step btn-finish">식사 확인</button>
										</c:when>
									</c:choose>
								</form>
							</td>
						</tr>
					</c:if>
				</c:forEach>
			</tbody>
		</table>
	</div>

	<%-- 2. 오늘 예약 섹션 --%>
	<div class="dashboard-card">
		<div class="card-header-flex">
			<h3 class="card-title-book">📅 오늘 예약 현황</h3>
			<span class="badge-wire">총 ${store_book_list.size()}건</span>
		</div>

		<table class="manage-dashboard-table">
			<thead>
				<tr>
					<th class="col-num">시간</th>
					<th class="col-userid">고객ID</th>
					<th class="col-people">인원</th>
					<th class="col-status">상태</th>
					<th class="col-action">상태 제어</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="book" items="${store_book_list}">
					<tr>
						<td align="center"><b><fmt:formatDate
									value="${book.book_date}" pattern="HH:mm" /></b></td>
						<td align="center">${book.user_id}</td>
						<td align="center">${book.people_cnt}명</td>
						<td align="center"><span
							class="badge-wire ${book.book_status == 'ING' ? 'badge-ing' : ''}">
								${book.book_status == 'RESERVED' ? '예약확정' : (book.book_status == 'ING' ? '식사중' : book.book_status)}
						</span></td>
						<td align="center">
							<form action="<c:url value='/book/updateStatus'/>" method="post"
								class="action-btn-group">
								<input type="hidden" name="book_id" value="${book.book_id}">
								<input type="hidden" name="user_id" value="${book.user_id}">
								<input type="hidden" name="${_csrf.parameterName}"
									value="${_csrf.token}" />

								<c:choose>
									<c:when test="${book.book_status == 'RESERVED'}">
										<button type="submit" name="status" value="ING"
											class="btn-step btn-step-next">입장 확인</button>
										<!-- 노쇼 버튼에 pay_id를 넣어 환불기능을 하기 위해 javascript에 보냄 -->
										<button type="button" name="status" value="NOSHOW"
											class="btn-step btn-step-danger noshow-btn" data-payid="${book.pay_id}">노쇼 처리</button>
									</c:when>
									<c:when test="${book.book_status == 'ING'}">
										<button type="submit" name="status" value="FINISH"
											class="btn-step btn-finish confirm-btn" data-payid="${book.pay_id}">식사 확인</button>
									</c:when>
									<c:otherwise>
										<span class="management-end">관리 종료</span>
									</c:otherwise>
								</c:choose>
							</form>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<%-- 스크립트 및 WebSocket 설정 --%>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>
<script src="<c:url value='/resources/js/manage.js'/>"></script>

<jsp:include page="../common/footer.jsp" />