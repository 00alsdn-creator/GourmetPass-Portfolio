<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<div style="padding: 20px;">
    <h1>🏠 ${store.store_name} <small style="font-size:15px; color:gray;">(${store.store_category})</small></h1>
    
    <table border="1" cellpadding="10" cellspacing="0" width="100%">
        <tr>
            <td width="300" align="center" bgcolor="#f0f0f0">
                <c:choose>
                    <c:when test="${not empty store.store_img}">
                        <img src="/upload/${store.store_img}" width="280">
                    </c:when>
                    <c:otherwise>이미지 준비중</c:otherwise>
                </c:choose>
            </td>
            <td valign="top">
                <p><b>📍 주소:</b> ${store.store_addr1} ${store.store_addr2}</p>
                <p><b>📞 전화:</b> ${store.store_tel}</p>
                <p><b>⏰ 영업시간:</b> ${store.store_time}</p>
                <p><b>📝 소개:</b> ${store.store_desc}</p>
                <p><b>👀 조회수:</b> ${store.store_cnt}</p>
            </td>
        </tr>
    </table>

    <hr>

    <h3>📋 대표 메뉴</h3>
    <ul>
        <c:forEach var="menu" items="${menuList}">
            <li>
                <b>${menu.menu_name}</b> 
                - <span style="color:red;"><fmt:formatNumber value="${menu.menu_price}" pattern="#,###"/>원</span>
            </li>
        </c:forEach>
        <c:if test="${empty menuList}">
            <li>등록된 메뉴가 없습니다.</li>
        </c:if>
    </ul>

    <hr>

    <h3>🗺️ 찾아오시는 길</h3>
    <div id="map" style="width:100%; height:300px; border:1px solid black;"></div>

    <hr>

    <div style="background-color: #fff8e1; padding: 20px; border: 2px dashed orange;">
        <h3>📅 예약하기 / 웨이팅</h3>
        
        <sec:authorize access="isAnonymous()">
            <p><b>⚠️ 예약하려면 로그인이 필요합니다.</b> <a href="${pageContext.request.contextPath}/member/login">[로그인하러 가기]</a></p>
        </sec:authorize>

        <sec:authorize access="isAuthenticated()">
            <form action="${pageContext.request.contextPath}/book/register" method="post">
                <input type="hidden" name="storeId" value="${store.store_id}">
                
                <sec:authentication property="principal.username" var="loginId"/>
                <input type="hidden" name="userId" value="${loginId}">

                <b>날짜:</b> <input type="datetime-local" name="bookDate" required>
                <b>인원:</b> 
                <select name="peopleCnt">
                    <option value="2">2명</option>
                    <option value="3">3명</option>
                    <option value="4">4명</option>
                </select>
                
                <button type="submit" style="padding: 5px 15px; font-weight: bold; background: red; color: white;">예약 신청</button>
            </form>
        </sec:authorize>
    </div>
    
    <br>
    <a href="list">[목록으로 돌아가기]</a>
</div>

<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=b907f9de332704eb4d28aab654997e4d"></script>
<script>
    window.onload = function() {
        if (${store.store_lat} && ${store.store_lon}) {
            var mapContainer = document.getElementById('map'), 
                mapOption = { center: new kakao.maps.LatLng(${store.store_lat}, ${store.store_lon}), level: 3 };
            var map = new kakao.maps.Map(mapContainer, mapOption);
            var marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(${store.store_lat}, ${store.store_lon}) });
            marker.setMap(map);
        }
    };
</script>

<jsp:include page="../common/footer.jsp" />