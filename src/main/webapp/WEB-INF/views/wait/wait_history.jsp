<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<%-- 공용 스타일시트 연결 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/wait_status.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/review_list.css'/>">

<div class="edit-wrapper wait-status-wrapper" style="max-width: 1100px; margin: 40px auto;">
    <div class="edit-title">📜 전체 이용 내역</div>

    <%-- 전체 이용 히스토리 --%>
    <div class="dashboard-card status-history-card">
        <div class="card-header">
            <h3 class="card-title">📜 전체 이용 내역</h3>
            <span class="badge-wire">최근 방문 순</span>
        </div>

        <div class="history-container">
            <%-- 웨이팅 히스토리 --%>
            <c:forEach var="w" items="${my_wait_list}">
                <div class="history-item">
                    <div class="history-info">
                        <div class="history-meta">
                            <span class="history-tag">[웨이팅]</span>
                            <span class="history-date"><fmt:formatDate value="${w.wait_date}" pattern="yy.MM.dd" /></span>
                        </div>
                        <h4 class="history-store">${w.store_name}</h4>
                    </div>

                    <div class="history-actions">
                        <c:if test="${w.wait_status == 'FINISH'}">
                            <c:choose>
                                <c:when test="${empty w.review_id}">
                                    <button class="btn-small btn-review js-review-link"
                                        data-url="<c:url value='/review/write?store_id=${w.store_id}&wait_id=${w.wait_id}'/>">리뷰 작성</button>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-done">리뷰완료</span>
                                    <button type="button" class="btn-delete-review"
                                        data-review-id="${w.review_id}" 
                                        data-store-id="${w.store_id}"
                                        data-return-url="/member/history">삭제</button>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${w.wait_status == 'CANCELLED'}">
                            <span class="text-done text-done--danger">취소됨</span>
                        </c:if>
                    </div>
                </div>
            </c:forEach>

            <%-- 예약 히스토리 --%>
            <c:forEach var="b" items="${my_book_list}">
                <div class="history-item">
                    <div class="history-info">
                        <div class="history-meta">
                            <span class="history-tag">[예약]</span>
                            <span class="history-date"><fmt:formatDate value="${b.book_date}" pattern="yy.MM.dd" /></span>
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
                                        data-url="<c:url value='/review/write?store_id=${b.store_id}&book_id=${b.book_id}'/>">리뷰 작성</button>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-done">리뷰완료</span>
                                    <button type="button" class="btn-delete-review"
                                        data-review-id="${b.review_id}" 
                                        data-store-id="${b.store_id}"
                                        data-return-url="/member/history">삭제</button>
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
            </c:forEach>
        </div>
    </div>

    <%-- 하단 네비게이션 버튼 --%>
    <div style="text-align: center; margin-top: 50px;">
        <button type="button" class="btn-wire" style="width: 200px; height: 55px;" onclick="location.href='<c:url value='/member/wait_status'/>'">이용 현황으로</button>
    </div>
</div>

<script src="<c:url value='/resources/js/mypage.js'/>"></script>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>

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