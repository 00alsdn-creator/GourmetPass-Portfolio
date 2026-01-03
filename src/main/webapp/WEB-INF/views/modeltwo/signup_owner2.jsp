<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>점주 회원가입 - 2단계</title>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<%-- 카카오 지도 API 및 주소 API 스크립트 --%>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakao.js.key}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
/* 상태 메시지 스타일 (join.jsp와 동일) */
.msg-ok { color: green; font-size: 12px; font-weight: bold; }
.msg-no { color: red; font-size: 12px; font-weight: bold; }
</style>
</head>
<body>
    <h2 align="center">점주 회원가입 - 2단계 (가게)</h2>
    
    <form action="${pageContext.request.contextPath}/join/ownerFinal.do" method="post" id="owner2Form">
        <%-- 가게 위치 좌표 --%>
        <input type="hidden" name="store_lat" id="store_lat" value="0.0">
        <input type="hidden" name="store_lon" id="store_lon" value="0.0">

        <table border="1" align="center" cellpadding="5">
            <tr>
                <td width="120">상호명</td>
                <td><input type="text" name="store_name" placeholder="가게 이름 입력" required></td>
            </tr>
            <tr>
                <td>카테고리</td>
                <td>
                    <select name="store_category" style="width: 100%;">
                        <option value="한식">한식</option>
                        <option value="일식">일식</option>
                        <option value="중식">중식</option>
                        <option value="양식">양식</option>
                        <option value="카페">카페·디저트</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>가게 전화번호</td>
                <td>
                    <input type="text" name="store_tel" required placeholder="숫자만 입력하세요" 
                           maxlength="13" oninput="autoHyphen(this)">
                </td>
            </tr>
            <tr>
                <td>가게 주소</td>
                <td>
                    <input type="text" name="store_zip" id="user_zip" placeholder="우편번호" readonly required>
                    <button type="button" onclick="execDaumPostcode()">주소검색</button><br>
                    <input type="text" name="store_addr1" id="user_addr1" size="40" placeholder="기본주소" readonly required><br>
                    <input type="text" name="store_addr2" id="user_addr2" placeholder="상세주소 입력">
                    
                    <div id="coordStatus" style="color: blue; font-size: 12px; margin-top: 5px;">
                        가게 주소를 검색하면 지도 좌표가 자동 등록됩니다.
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <input type="button" value="이전 단계" onclick="history.back()">
                    <input type="submit" value="가입 및 입점 완료">
                </td>
            </tr>
        </table>
    </form>

<script>
    // 1. 전화번호 자동 하이픈 로직
    const autoHyphen = (target) => {
        target.value = target.value
            .replace(/[^0-9]/g, '')
            .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, `$1-$2-$3`);
    }

    // 2. 주소 검색 및 좌표 추출 로직
    const geocoder = new kakao.maps.services.Geocoder();

    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById('user_zip').value = data.zonecode;
                document.getElementById('user_addr1').value = addr;

                // 주소로 좌표를 검색하여 히든 필드에 저장
                geocoder.addressSearch(addr, function(results, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var result = results[0];
                        document.getElementById('store_lat').value = result.y;
                        document.getElementById('store_lon').value = result.x;
                        
                        var msg = "📍 좌표 추출 완료!<br>" 
                                + "<small>(위도: " + result.y + ", 경도: " + result.x + ")</small>";
                        
                        $("#coordStatus").html("<span class='msg-ok'>" + msg + "</span>");
                    } else {
                        $("#coordStatus").html("<span class='msg-no'>❌ 좌표 추출 실패</span>");
                    }
                });
                document.getElementById('user_addr2').focus();
            }
        }).open();
    }

    // 3. 폼 전송 시 최종 검사 (좌표가 추출되었는지 확인)
    $("#owner2Form").submit(function() {
        if($("#store_lat").val() === "0.0") {
            alert("가게 주소를 검색하여 좌표를 등록해주세요.");
            return false;
        }
        return true;
    });
</script>

</body>
</html>