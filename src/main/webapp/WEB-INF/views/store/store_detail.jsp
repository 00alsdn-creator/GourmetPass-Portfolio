<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<jsp:include page="../common/header.jsp" />

<style>
    .time-btn {
        padding: 8px 15px; 
        margin: 5px; 
        border: 1px solid #ccc; 
        background-color: #f9f9f9; 
        cursor: pointer; 
        border-radius: 5px;
        transition: 0.2s;
        width: 85px; /* 버튼 크기 통일 */
    }
    .time-btn:hover:not(:disabled) { background-color: #e0e0e0; }
    .time-btn.active { 
        background-color: #ff3d00; 
        color: white; 
        border-color: #ff3d00; 
        font-weight: bold;
    }
    /* 예약 불가(지난 시간) 버튼 스타일 */
    .time-btn:disabled {
        background-color: #eee;
        color: #bbb;
        cursor: not-allowed;
        border-color: #ddd;
    }
    .step-title { font-weight: bold; margin-bottom: 10px; display: block; }
</style>

<div style="padding: 20px; max-width: 1000px; margin: auto;">
    <h1>🏠 ${store.store_name} <small style="font-size:15px; color:gray;">(${store.store_category})</small></h1>
    
    <table border="0" cellpadding="10" cellspacing="0" width="100%" style="border: 1px solid #ddd; border-radius: 10px; overflow: hidden;">
        <tr>
            <td width="350" align="center" bgcolor="#f9f9f9" style="border-right: 1px solid #ddd;">
                <c:choose>
                    <c:when test="${not empty store.store_img}">
                        <img src="/upload/${store.store_img}" width="320" style="border-radius: 10px; box-shadow: 2px 2px 10px rgba(0,0,0,0.1);">
                    </c:when>
                    <c:otherwise><div style="width:320px; height:200px; background:#eee; line-height:200px;">이미지 준비중</div></c:otherwise>
                </c:choose>
            </td>
            <td valign="top" style="padding: 20px;">
                <p><b>📍 주소:</b> ${store.store_addr1} ${store.store_addr2}</p>
                <p><b>📞 전화:</b> ${store.store_tel}</p>
                <p><b>⏰ 영업시간:</b> <span id="store-time-info">${store.open_time} ~ ${store.close_time}</span> (단위: ${store.res_unit}분)</p>
                <p><b>📝 소개:</b> ${store.store_desc}</p>
                <p><b>👀 조회수:</b> ${store.store_cnt}</p>
            </td>
        </tr>
    </table>

    <hr style="margin: 30px 0;">

    <h3>📋 대표 메뉴</h3>
    <ul style="list-style: none; padding: 0;">
        <c:forEach var="menu" items="${menuList}">
            <li style="padding: 10px; border-bottom: 1px solid #eee;">
                <b>${menu.menu_name}</b> 
                - <span style="color:#ff3d00;"><fmt:formatNumber value="${menu.menu_price}" pattern="#,###"/>원</span>
            </li>
        </c:forEach>
        <c:if test="${empty menuList}">
            <li style="color: gray;">등록된 메뉴가 없습니다.</li>
        </c:if>
    </ul>

    <hr style="margin: 30px 0;">

    <h3>🗺️ 찾아오시는 길</h3>
    <div id="map" style="width:100%; height:350px; border-radius: 10px; border:1px solid #ddd;"></div>

    <hr style="margin: 30px 0;">

    <div style="background-color: #fffaf0; padding: 30px; border: 2px solid #ffe0b2; border-radius: 15px;">
        <h3 style="color: #e65100;">📅 예약하기 (당일 예약 전용)</h3>
        
        <sec:authorize access="isAnonymous()">
            <div style="text-align: center; padding: 20px;">
                <p><b>⚠️ 예약하려면 로그인이 필요합니다.</b></p>
                <a href="${pageContext.request.contextPath}/member/login" style="display: inline-block; padding: 10px 20px; background: #ff3d00; color: white; border-radius: 5px; text-decoration: none;">로그인하러 가기</a>
            </div>
        </sec:authorize>

        <sec:authorize access="isAuthenticated()">
            <form action="${pageContext.request.contextPath}/book/register" method="post" onsubmit="return validateForm()">
                <input type="hidden" name="storeId" value="${store.store_id}">
                <sec:authentication property="principal.username" var="loginId"/>
                <input type="hidden" name="userId" value="${loginId}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                <div style="display: flex; gap: 30px; flex-wrap: wrap;">
                    <div style="flex: 1; min-width: 250px;">
                        <label class="step-title">Step 1. 날짜 확인</label>
                        <input type="text" name="bookDate" id="bookDate" readonly 
                               style="padding: 10px; width: 100%; border: 1px solid #ccc; border-radius: 5px; background: #eee; cursor: not-allowed;">
                        <p style="font-size: 12px; color: #f44336; margin-top: 5px;">* 온라인 예약은 당일 방문만 가능합니다.</p>
                        
                        <label class="step-title" style="margin-top: 25px;">Step 2. 인원 선택</label>
                        <select name="peopleCnt" style="padding: 10px; width: 100%; border: 1px solid #ccc; border-radius: 5px;">
                            <c:forEach var="i" begin="1" end="10">
                                <option value="${i}">${i}명</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div style="flex: 2; min-width: 300px; border-left: 1px dashed #ffccbc; padding-left: 30px;">
                        <label class="step-title">Step 3. 시간 선택</label>
                        <div id="timeSlotContainer" style="display: flex; flex-wrap: wrap; align-content: flex-start;">
                            </div>
                        <input type="hidden" name="bookTime" id="selectedTime" required>
                    </div>
                </div>

                <div style="text-align: center; margin-top: 40px;">
                    <button type="submit" style="padding: 15px 50px; font-size: 18px; font-weight: bold; background: #ff3d00; color: white; border: none; border-radius: 30px; cursor: pointer; box-shadow: 0 4px 10px rgba(255,61,0,0.3);">
                        🚀 예약 확정하기
                    </button>
                </div>
            </form>
        </sec:authorize>
    </div>
    
    <div style="margin-top: 20px; text-align: right;">
        <a href="list" style="color: gray; text-decoration: none;">← 목록으로 돌아가기</a>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}"></script>

<script>
    // 페이지 로드 시 실행
    $(document).ready(function() {
        // 1. 지도 생성
        if (${not empty store.store_lat} && ${not empty store.store_lon}) {
            var mapContainer = document.getElementById('map'), 
                mapOption = { center: new kakao.maps.LatLng(${store.store_lat}, ${store.store_lon}), level: 3 };
            var map = new kakao.maps.Map(mapContainer, mapOption);
            var marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(${store.store_lat}, ${store.store_lon}) });
            marker.setMap(map);
        }

        // 2. 당일 날짜 설정 (Step 1)
        var now = new Date();
        var yyyy = now.getFullYear();
        var mm = String(now.getMonth() + 1).padStart(2, '0');
        var dd = String(now.getDate()).padStart(2, '0');
        var todayStr = yyyy + "-" + mm + "-" + dd;
        $("#bookDate").val(todayStr);

        // 3. 타임테이블 생성 (Step 3)
        generateTimeSlots();
    });

    // 타임테이블 생성 함수
    function generateTimeSlots() {
        // 서버에서 받아온 영업 정보 (값이 없으면 기본값 설정)
        const openTime = "${store.open_time}" || "09:00";
        const closeTime = "${store.close_time}" || "22:00";
        const resUnit = parseInt("${store.res_unit}") || 30;

        const container = $("#timeSlotContainer");
        container.empty();

        // 시간 문자열 -> 분 단위 변환
        function toMin(t) {
            let parts = t.split(':');
            return parseInt(parts[0]) * 60 + parseInt(parts[1]);
        }

        // 분 단위 -> 시간 문자열 변환 (09:30 형식)
        function toStr(m) {
            let h = Math.floor(m / 60);
            let min = m % 60;
            return (h < 10 ? "0" + h : h) + ":" + (min < 10 ? "0" + min : min);
        }

        const startMin = toMin(openTime);
        const endMin = toMin(closeTime);
        
        // 현재 시간 (지난 시간 예약 방지용)
        const now = new Date();
        const currentTotalMin = (now.getHours() * 60) + now.getMinutes();

        let html = "";
        for (let m = startMin; m < endMin; m += resUnit) {
            let timeStr = toStr(m);
            // 현재 시간보다 10분 뒤부터 예약 가능하도록 설정
            let isDisabled = m < (currentTotalMin + 10) ? "disabled" : "";
            
            html += '<button type="button" class="time-btn" ' + isDisabled + ' onclick="selectTime(this, \'' + timeStr + '\')">' + timeStr + '</button>';
        }

        if (html === "") {
            html = "<p style='color:gray;'>현재 예약 가능한 시간이 없습니다.</p>";
        }
        container.append(html);
    }

    // 시간 선택 이벤트
    function selectTime(btn, time) {
        $(".time-btn").removeClass("active");
        $(btn).addClass("active");
        $("#selectedTime").val(time);
    }

    // 폼 검증
    function validateForm() {
        var time = $("#selectedTime").val();
        if(!time) {
            alert("방문하실 시간을 선택해주세요!");
            return false;
        }
        return confirm("오늘 " + time + " 시로 예약을 신청하시겠습니까?");
    }
</script>

<jsp:include page="../common/footer.jsp" />