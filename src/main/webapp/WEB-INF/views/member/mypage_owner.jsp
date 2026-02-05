<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="../common/header.jsp" />

<%-- [관심사 분리] 공용 마이페이지 스타일 및 통합 스크립트 연결 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<script src="<c:url value='/resources/js/mypage.js'/>"></script>

<div class="edit-wrapper" style="max-width: 1200px;">
    <div class="profile-card">
        <div class="profile-info">
            <span class="profile-label" style="color: #2f855a;">OWNER PROFILE</span>
            <h2 class="user-name">${member.user_nm} <small>점주님</small></h2>
            <p class="user-meta">ID: ${member.user_id} | TEL: ${member.user_tel}</p>
        </div>
        <div class="btn-group" style="margin: 0; width: auto;">
            <a href="<c:url value='/member/edit'/>" class="btn-wire" style="height: 45px; padding: 0 20px; font-size: 14px;">정보 수정</a>
            <form action="<c:url value='/logout'/>" method="post" style="display: inline;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <button type="submit" class="btn-wire btn-logout" style="height: 45px; padding: 0 20px; font-size: 14px; margin-left: 10px;">로그아웃</button>
            </form>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty store}">
            <a href="<c:url value='/book/manage?store_id=${store.store_id}'/>" class="status-btn-full">
                ⚙️ 실시간 예약 및 웨이팅 관리 센터
            </a>

            <div class="dashboard-grid">
                <aside class="dashboard-card">
                    <div class="card-header">
                        <h3 class="card-title">🏨 내 가게 정보</h3>
                        <button class="btn-wire" style="height: 32px; padding: 0 10px; font-size: 12px;" 
                                onclick="location.href='<c:url value='/store/update?store_id=${store.store_id}'/>'">수정</button>
                    </div>
                    <div style="text-align: center; margin-bottom: 25px;">
                        <div style="border: 2px solid #333; border-radius: 12px; overflow: hidden; height: 210px; background: #f9f9f9; margin-bottom: 15px;">
                            <c:choose>
                                <c:when test="${not empty store.store_img}">
                                    <img src="<c:url value='/upload/${store.store_img}'/>" style="width:100%; height:100%; object-fit:cover;">
                                </c:when>
                                <c:otherwise><div style="line-height:210px; color:#ccc; font-weight:900;">NO IMAGE</div></c:otherwise>
                            </c:choose>
                        </div>
                        <h4 style="font-size: 22px; font-weight: 900; margin-bottom: 8px;">${store.store_name}</h4>
                        <span class="badge-wire">${store.store_category}</span>
                    </div>
                    <table class="edit-table" style="font-size: 14px;">
                        <tr><th style="width: 40%;">영업시간</th><td>${store.open_time} ~ ${store.close_time}</td></tr>
                        <tr><th>예약단위</th><td>${store.res_unit}분</td></tr>
                        <tr><th>가게번호</th><td>${store.store_tel}</td></tr>
                    </table>
                </aside>

                <section class="dashboard-card">
                    <div class="card-header">
                        <h3 class="card-title">📋 메뉴 관리 (${menuList.size()})</h3>
                        <button class="btn-submit" style="width: auto; height: 38px; padding: 0 15px; font-size: 14px;" 
                                onclick="location.href='<c:url value='/store/menu/register?store_id=${store.store_id}'/>'">+ 메뉴 추가</button>
                    </div>
                    <table class="edit-table">
                        <thead>
                            <tr style="border-bottom: 2px solid #333;">
                                <th style="text-align: center; width: 80px;">이미지</th>
                                <th>메뉴명</th>
                                <th style="text-align: right; width: 140px;">가격</th>
                                <th style="text-align: center; width: 150px;">관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="menu" items="${menuList}">
                                <tr>
                                    <td align="center">
                                        <c:if test="${not empty menu.menu_img}">
                                            <img src="<c:url value='/upload/${menu.menu_img}'/>" class="item-img-thumb" style="width:60px; height:60px;">
                                        </c:if>
                                    </td>
                                    <td>
                                        <div style="font-weight: 800; font-size: 16px;">
                                            ${menu.menu_name}
                                            <c:if test="${menu.menu_sign == 'Y'}"><span class="badge-best" style="background:#ff3d00; color:#fff; padding:2px 5px; border-radius:4px; font-size:11px; margin-left:5px;">대표</span></c:if>
                                        </div>
                                    </td>
                                    <td align="right" style="font-weight: 900; color: #ff3d00;"><fmt:formatNumber value="${menu.menu_price}" pattern="#,###" />원</td>
                                    <td align="center">
                                        <div style="display: flex; gap: 6px; justify-content: center;">
                                            <button class="btn-wire" style="height: 32px; padding: 0 10px; font-size: 12px;" 
                                                    onclick="location.href='<c:url value='/store/menu/update?menu_id=${menu.menu_id}'/>'">수정</button>
                                            <button class="btn-wire" style="height: 32px; padding: 0 10px; font-size: 12px; color: #dc3545; border-color: #dc3545;" 
                                                    onclick="deleteMenu(${menu.menu_id})">삭제</button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </section>
            </div>

            <%-- [핵심 수정 섹션] 리뷰 요약 노출 및 전체보기 연동 --%>
            <div class="review-container" style="margin-top: 30px;">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3 class="section-title" style="margin: 0;">💬 우리 가게 리뷰 (${store_review_list.size()})</h3>
                    <%-- store_detail.jsp와 동일한 버튼 스타일 및 경로 적용 --%>
                    <a href="<c:url value='/review/list?store_id=${store.store_id}'/>" class="btn-wire-small">전체보기 ❯</a>
                </div>

                <div class="store-review-list">
                    <c:choose>
                        <c:when test="${not empty store_review_list}">
                            <%-- 최근 2개만 노출하기 위해 end="1" 설정 (0, 1 인덱스) --%>
                            <c:forEach var="review" items="${store_review_list}" end="1">
                                <div class="item-card">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 15px; border-bottom: 1px dashed #ddd; padding-bottom: 10px;">
                                        <div>
                                            <strong style="font-size: 16px;">${review.user_nm} 고객님</strong>
                                            <span style="color: #f1c40f; margin-left: 10px;">
                                                <c:forEach begin="1" end="${review.rating}">⭐</c:forEach>
                                            </span>
                                        </div>
                                        <span style="color: #999; font-size: 13px;">
                                            <fmt:formatDate value="${review.review_date}" pattern="yyyy.MM.dd" />
                                        </span>
                                    </div>
                                    <div style="display: flex; gap: 20px;">
                                        <c:if test="${not empty review.img_url}">
                                            <img src="<c:url value='/upload/${review.img_url}'/>" class="item-img-thumb" style="width:120px; height:120px; object-fit: cover; border-radius: 8px;">
                                        </c:if>
                                        <p style="line-height: 1.6; font-size: 15px; flex: 1;">${review.content}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="dashboard-card" style="text-align: center; padding: 50px 0; color: #999;">
                                아직 등록된 리뷰가 없습니다.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="dashboard-card" style="text-align: center; padding: 100px 0;">
                <h3 style="font-size: 26px; font-weight: 900;">연결된 매장 정보가 없습니다.</h3>
                <p style="color: #666; margin-top: 15px;">가게 정보를 등록하여 Gourmet Pass 서비스를 시작하세요!</p>
                <button class="btn-submit" style="width: 300px; height: 55px; margin-top: 30px;" 
                        onclick="location.href='<c:url value='/member/signup/owner2'/>'">지금 바로 등록하기</button>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />