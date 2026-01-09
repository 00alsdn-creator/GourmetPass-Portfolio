<%-- 
    [1] 페이지 설정 지시어
    - member 객체에는 컨트롤러에서 보낸 기존 회원 정보가 담겨 있어야 합니다.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 정보 수정</title>

<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
    /* signup_general과 통일된 스타일 */
    .msg-ok { color: green; font-size: 12px; font-weight: bold; }
    .msg-no { color: red; font-size: 12px; font-weight: bold; }
    table { margin-top: 20px; border-collapse: collapse; }
    td { padding: 10px; }
    input[readonly] { background-color: #eee; } /* 읽기 전용 필드 배경색 */
</style>
</head>
<body>
    <h2 align="center">회원 정보 수정</h2>

    <%-- [3] 전송 폼: action을 /member/edit으로 변경 --%>
    <form action="${pageContext.request.contextPath}/member/edit" method="post" id="editForm">

        <%-- CSRF 토큰 (보안 필수) --%>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <%-- 기존 좌표 데이터 유지 --%>
        <input type="hidden" name="user_lat" id="user_lat" value="${member.user_lat}">
        <input type="hidden" name="user_lon" id="user_lon" value="${member.user_lon}">

        <table border="1" align="center">
            <tr>
                <td>아이디</td>
                <td>
                    <%-- 아이디는 고유 값이므로 수정을 못하게 readonly 처리합니다. --%>
                    <input type="text" name="user_id" id="user_id" value="${member.user_id}" readonly>
                    <span style="color: gray; font-size: 12px;">(아이디는 수정할 수 없습니다.)</span>
                </td>
            </tr>
            <tr>
                <td>새 비밀번호</td>
                <td>
                    <input type="password" name="user_pw" id="user_pw" placeholder="수정 시에만 입력하세요">
                </td>
            </tr>
            <tr>
                <td>비밀번호 확인</td>
                <td>
                    <input type="password" id="user_pw_confirm" placeholder="비밀번호 재입력">
                    <div id="pwCheckMsg"></div>
                </td>
            </tr>
            <tr>
                <td>이름</td>
                <td><input type="text" name="user_nm" value="${member.user_nm}" required></td>
            </tr>
            <tr>
                <td>이메일</td>
                <td><input type="email" name="user_email" value="${member.user_email}"></td>
            </tr>
            <tr>
                <td>전화번호</td>
                <td>
                    <input type="text" name="user_tel" value="${member.user_tel}" required 
                           maxlength="13" oninput="autoHyphen(this)">
                </td>
            </tr>
            <tr>
                <td>주소</td>
                <td>
                    <input type="text" name="user_zip" id="user_zip" value="${member.user_zip}" placeholder="우편번호" readonly>
                    <button type="button" onclick="execDaumPostcode()">주소검색</button> <br>

                    <input type="text" name="user_addr1" id="user_addr1" value="${member.user_addr1}" 
                           placeholder="기본주소" size="40" readonly><br> 
                    <input type="text" name="user_addr2" id="user_addr2" value="${member.user_addr2}" placeholder="상세주소 입력">

                    <div id="coordStatus" style="color: blue; font-size: 12px; margin-top: 5px;">
                        주소를 변경하면 좌표가 자동으로 갱신됩니다.
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <input type="submit" value="수정완료"> 
                    <input type="button" value="취소" onclick="location.href='${pageContext.request.contextPath}/member/mypage'">
                </td>
            </tr>
        </table>
    </form>

    <script>
    // 수정 페이지이므로 아이디는 이미 체크된 상태나 다름없음
    let isPwMatched = true; 

    // --- [기능 1] 비밀번호 일치 확인 (수정 시에만 작동하도록 유연하게 처리) ---
    $("#user_pw, #user_pw_confirm").on("keyup", function() {
        const pw = $("#user_pw").val();
        const pwConfirm = $("#user_pw_confirm").val();
        
        if(pw === "" && pwConfirm === "") { 
            $("#pwCheckMsg").text(""); 
            isPwMatched = true; 
            return; 
        }
        
        if(pw === pwConfirm) { 
            $("#pwCheckMsg").html("<span class='msg-ok'>비밀번호가 일치합니다.</span>"); 
            isPwMatched = true; 
        } else { 
            $("#pwCheckMsg").html("<span class='msg-no'>비밀번호가 일치하지 않습니다.</span>"); 
            isPwMatched = false; 
        }
    });

    // --- [기능 2] 최종 제출 전 검사 ---
    $("#editForm").submit(function() {
        if(!isPwMatched) { 
            alert("비밀번호 확인을 다시 해주세요."); 
            $("#user_pw_confirm").focus(); 
            return false; 
        }
        return confirm("회원 정보를 수정하시겠습니까?");
    });

    // --- [기능 3] 주소 검색 및 좌표 변환 (signup_general과 동일) ---
    const geocoder = new kakao.maps.services.Geocoder();

    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById('user_zip').value = data.zonecode;
                document.getElementById('user_addr1').value = addr;

                geocoder.addressSearch(addr, function(results, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var result = results[0]; 
                        document.getElementById('user_lat').value = result.y; 
                        document.getElementById('user_lon').value = result.x; 
                        
                        var msg = "📍 좌표 갱신 완료!";
                        $("#coordStatus").html("<span class='msg-ok'>" + msg + "</span>");
                    }
                });
                document.getElementById('user_addr2').focus();
            }
        }).open();
    }

    // --- [기능 4] 전화번호 자동 하이픈 ---
    const autoHyphen = (target) => {
    // 1. 숫자 이외의 문자 제거
    let val = target.value.replace(/[^0-9]/g, "");
    let str = "";

    // 2. 서울 지역번호(02)인 경우
    if (val.startsWith("02")) {
        if (val.length < 3) {
            str = val;
        } else if (val.length < 6) {
            // 02-123
            str = val.substr(0, 2) + "-" + val.substr(2);
        } else if (val.length < 10) {
            // 02-123-4567 (9자리)
            str = val.substr(0, 2) + "-" + val.substr(2, 3) + "-" + val.substr(5);
        } else {
            // 02-1234-5678 (10자리)
            str = val.substr(0, 2) + "-" + val.substr(2, 4) + "-" + val.substr(6);
        }
    } 
    // 3. 그 외 번호 (010, 031, 051 등)
    else {
        if (val.length < 4) {
            str = val;
        } else if (val.length < 7) {
            // 010-123
            str = val.substr(0, 3) + "-" + val.substr(3);
        } else if (val.length < 11) {
            // 010-123-4567 (10자리)
            str = val.substr(0, 3) + "-" + val.substr(3, 3) + "-" + val.substr(6);
        } else {
            // 010-1234-5678 (11자리)
            str = val.substr(0, 3) + "-" + val.substr(3, 4) + "-" + val.substr(7);
        }
    }
    
    // 최종 결과물 반영
    target.value = str;
};
    </script>
</body>
</html>