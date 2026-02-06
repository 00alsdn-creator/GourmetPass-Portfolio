<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/wait_status.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/review_list.css'/>">


<div class="edit-wrapper wait-status-wrapper" style="max-width: 1100px; margin: 40px auto;">
    <div class="edit-title">📜 전체 이용 내역</div>

    <%-- 웨이팅 내역 섹션 --%>
    <div class="dashboard-card status-history-card">
        <div class="card-header">
            <h3 class="card-title">🚶 웨이팅 내역</h3>
            <span class="badge-wire">총 ${waitPageInfo.total}건</span>
        </div>

        <div class="history-container">
            <c:choose>
                <c:when test="${not empty my_wait_list}">
                    <c:forEach var="w" items="${my_wait_list}">
                        <div class="history-item">
                            <div class="history-info">
                                <div class="history-meta">
                                    <span class="history-tag">[웨이팅]</span>
                                    <span class="history-date">
                                        <fmt:formatDate value="${w.wait_date}" pattern="yy.MM.dd HH:mm" />
                                    </span>
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
                                                data-return-url="/member/history?waitPage=${waitPageInfo.pageNum}&bookPage=${bookPageInfo.pageNum}">삭제</button>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${w.wait_status == 'CANCELLED'}">
                                    <span class="text-done text-done--danger">취소됨</span>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="status-empty">웨이팅 내역이 없습니다.</div>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- 웨이팅 페이징 --%>
        <c:if test="${waitPageInfo.pages > 1}">
            <div class="pagination-container">
                <c:if test="${waitPageInfo.hasPreviousPage}">
                    <a href="?waitPage=${waitPageInfo.pageNum - 1}&bookPage=${bookPageInfo.pageNum}" class="btn-wire">이전</a>
                </c:if>
                
                <%-- ★ 수정: fn:length() 사용 --%>
                <c:set var="navLength" value="${fn:length(waitPageInfo.navigatepageNums)}" />
                <c:forEach begin="0" end="${navLength - 1}" var="i">
                    <c:set var="num" value="${waitPageInfo.navigatepageNums[i]}" />
                    <a href="?waitPage=${num}&bookPage=${bookPageInfo.pageNum}" 
                       class="btn-wire ${num == waitPageInfo.pageNum ? 'active' : ''}">${num}</a>
                </c:forEach>
                
                <c:if test="${waitPageInfo.hasNextPage}">
                    <a href="?waitPage=${waitPageInfo.pageNum + 1}&bookPage=${bookPageInfo.pageNum}" class="btn-wire">다음</a>
                </c:if>
            </div>
        </c:if>
    </div>

    <%-- 예약 내역 섹션 --%>
    <div class="dashboard-card status-history-card" style="margin-top: 40px;">
        <div class="card-header">
            <h3 class="card-title">📅 예약 내역</h3>
            <span class="badge-wire">총 ${bookPageInfo.total}건</span>
        </div>

        <div class="history-container">
            <c:choose>
                <c:when test="${not empty my_book_list}">
                    <c:forEach var="b" items="${my_book_list}">
                        <div class="history-item">
                            <div class="history-info">
                                <div class="history-meta">
                                    <span class="history-tag">[예약]</span>
                                    <span class="history-date">
                                        <fmt:formatDate value="${b.book_date}" pattern="yy.MM.dd HH:mm" />
                                    </span>
                                </div>
                                <h4 class="history-store">${b.store_name}</h4>
                            </div>

                            <div class="history-actions">
                                <c:if test="${b.book_status == 'FINISH'}">
                                    <!-- <button class="btn-small btn-payment js-alert"
                                        data-message="결제 상세 정보를 확인합니다.">결제내역</button> -->
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
                                                data-return-url="/member/history?waitPage=${waitPageInfo.pageNum}&bookPage=${bookPageInfo.pageNum}">삭제</button>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${b.book_status == 'RESERVED'}">
                                    <div style="display: flex; align-items: center; gap: 8px;">
							            <span class="text-done text-done--success">방문예정</span>
							            
							            <%-- 취소 처리용 숨겨진 폼 --%>
							            <form action="<c:url value='/book/updateStatus'/>" method="post" class="userCancelForm">
							                <input type="hidden" name="book_id" value="${b.book_id}">
							                <input type="hidden" name="_csrf" value="${_csrf.token}" />
							                <button type="button" 
							                        class="btn-small btn-danger-outline history-cancel-btn"
							                        data-payid="${b.pay_id}"
							                        style="padding: 4px 8px; font-size: 11px;">예약취소</button>
							            </form>
							        </div>
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
                </c:when>
                <c:otherwise>
                    <div class="status-empty">예약 내역이 없습니다.</div>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- 예약 페이징 --%>
        <c:if test="${bookPageInfo.pages > 1}">
            <div class="pagination-container">
                <c:if test="${bookPageInfo.hasPreviousPage}">
                    <a href="?waitPage=${waitPageInfo.pageNum}&bookPage=${bookPageInfo.pageNum - 1}" class="btn-wire">이전</a>
                </c:if>
                
                <%-- ★ 수정: fn:length() 사용 --%>
                <c:set var="navLength" value="${fn:length(bookPageInfo.navigatepageNums)}" />
                <c:forEach begin="0" end="${navLength - 1}" var="i">
                    <c:set var="num" value="${bookPageInfo.navigatepageNums[i]}" />
                    <a href="?waitPage=${waitPageInfo.pageNum}&bookPage=${num}" 
                       class="btn-wire ${num == bookPageInfo.pageNum ? 'active' : ''}">${num}</a>
                </c:forEach>
                
                <c:if test="${bookPageInfo.hasNextPage}">
                    <a href="?waitPage=${waitPageInfo.pageNum}&bookPage=${bookPageInfo.pageNum + 1}" class="btn-wire">다음</a>
                </c:if>
            </div>
        </c:if>
    </div>

    <%-- 하단 네비게이션 버튼 --%>
    <div style="text-align: center; margin-top: 50px;">
        <button type="button" class="btn-wire" style="width: 200px; height: 55px;" onclick="location.href='<c:url value='/member/wait_status'/>'">이용 현황으로</button>
    </div>
</div>

<script src="<c:url value='/resources/js/mypage.js'/>"></script>
<script src="<c:url value='/resources/js/member_mypage.js'/>"></script>

<!-- <script>
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

// JS 공통 기능
$(".js-alert").on("click", function() {
    alert($(this).data("message"));
});

$(".js-review-link").on("click", function() {
    location.href = $(this).data("url");
});
</script> -->

<%-- JSP 하단 --%>
<script src="<c:url value='/resources/js/wait_history.js'/>"></script>

<script>
// 페이지 개별적으로 필요한 삭제 확인창 등만 남깁니다.
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