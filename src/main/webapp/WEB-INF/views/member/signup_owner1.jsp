<%-- 
    [1] 페이지 설정 지시어
    - isELIgnored="false": ${kakaoJsKey} 같은 EL 표현식을 자바 코드로 해석하라는 명령
    - taglib: JSTL(자바 표준 태그 라이브러리)을 사용하겠다는 선언 (c:if 등을 쓸 수 있음)
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>점주 회원가입 - 1단계</title>

<!-- [2] 필수 라이브러리 (jQuery, Kakao Map, Daum Postcode) -->
<!-- 일반 회원가입과 동일하게 주소 찾기 및 좌표 변환 기능이 필요해서 가져옴 -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
    /* 성공/실패 메시지 스타일 */
    .msg-ok { color: green; font-size: 12px; font-weight: bold; }
    .msg-no { color: red; font-size: 12px; font-weight: bold; }
    
    /* 표 디자인 */
    table { margin-top: 20px; border-collapse: collapse; }
    td { padding: 10px; }
</style>
</head>
<body>
    <h2 align="center">점주 회원가입 - 1단계 (계정 정보)</h2>
    
    <%-- 
        [3] 전송 폼 (핵심 차이점!)
        - action: "/member/signup/ownerStep1" 
        - 일반 회원가입은 "/joinProcess"(DB저장)로 가지만,
          점주는 아직 가게 정보를 입력 안 했으므로 "임시 저장(Session)"하는 컨트롤러로 보냅니다.
    --%>
    <form action="<c:url value='/member/signup/ownerStep1'/>" method="post" id="joinForm">
        
        <%-- CSRF 토큰: POST 전송 시 필수 보안 토큰 --%>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <!-- 좌표 정보 (숨김): 사장님 집 주소 좌표도 저장하고 싶으면 사용 (선택사항) -->
        <input type="hidden" name="user_lat" id="user_lat" value="0.0">
        <input type="hidden" name="user_lon" id="user_lon" value="0.0">

        <table border="1" align="center">
            <tr>
                <td width="120">아이디</td>
                <td>
                    <!-- name="user_id": MemberVO의 필드명과 똑같아야 컨트롤러가 알아서 받음 -->
                    <input type="text" name="user_id" id="user_id" placeholder="아이디" required>
                    <button type="button" id="btnIdCheck">중복확인</button>
                    <!-- 중복확인 결과 메시지 공간 -->
                    <div id="idCheckMsg"></div>
                </td>
            </tr>
            <tr>
                <td>비밀번호</td>
                <td><input type="password" name="user_pw" id="user_pw" placeholder="비밀번호" required></td>
            </tr>
            <tr>
                <td>비밀번호 확인</td>
                <td>
                    <input type="password" id="user_pw_confirm" placeholder="비밀번호 재입력" required>
                    <div id="pwCheckMsg"></div>
                </td>
            </tr>
            <tr>
                <td>이름</td>
                <td><input type="text" name="user_nm" required></td>
            </tr>
            <tr>
                <td>이메일</td>
                <td><input type="email" name="user_email"></td>
            </tr>
            <tr>
                <td>전화번호</td>
                <td>
                   <!-- autoHyphen 함수 적용 (010-xxxx-xxxx) -->
                   <input type="text" name="user_tel" required placeholder="숫자만 입력하세요" 
                           maxlength="13" oninput="autoHyphen(this)">
                </td>
            </tr>
            <tr>
                <td>거주지 주소</td>
                <td>
                    <input type="text" name="user_zip" id="user_zip" placeholder="우편번호" readonly>
                    <button type="button" onclick="execDaumPostcode()">주소검색</button><br>
                    
                    <input type="text" name="user_addr1" id="user_addr1" placeholder="기본주소" size="40" readonly><br>
                    <input type="text" name="user_addr2" id="user_addr2" placeholder="상세주소 입력">
                    
                    <!-- 좌표 변환 결과 -->
                    <div id="coordStatus" style="color: blue; font-size: 12px; margin-top: 5px;">
                        주소를 검색하면 자동으로 좌표가 입력됩니다.
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <!-- 버튼 텍스트가 '가입하기'가 아니라 '다음 단계로'입니다 -->
                    <input type="submit" value="다음 단계로 (가게 정보 입력)">
                    <input type="button" value="취소" onclick="location.href='${pageContext.request.contextPath}/'">
                </td>
            </tr>
        </table>
    </form>

<script>
    // [4] 유효성 검사 변수 (중복체크 했는지? 비번 맞는지?)
    let isIdChecked = false;
    let isPwMatched = false;

    // --- [기능 1] 아이디 중복확인 (AJAX) ---
    $("#btnIdCheck").click(function() {
        const userId = $("#user_id").val();
        
        // 유효성 검사: 너무 짧으면 서버로 보내지도 않음
        if(userId.length < 3) { alert("아이디는 3글자 이상 입력해주세요."); return; }
        
        $.ajax({
        	url: "<c:url value='/member/idCheck'/>", // 컨트롤러 주소
            type: "POST",
            data: { 
                user_id: userId,
                "${_csrf.parameterName}": "${_csrf.token}" // 보안 토큰 필수
            },
            success: function(res) {
                if(res === "success") { 
                    $("#idCheckMsg").html("<span class='msg-ok'>사용 가능한 아이디입니다.</span>");
                    isIdChecked = true; // 통과
                } else { 
                    $("#idCheckMsg").html("<span class='msg-no'>이미 사용 중인 아이디입니다.</span>");
                    isIdChecked = false; // 실패
                }
            }
        });
    });

    // 아이디 수정 시 다시 검사하도록 깃발 내림
    $("#user_id").on("input", function() { isIdChecked = false; $("#idCheckMsg").text(""); });

    // --- [기능 2] 비밀번호 일치 확인 ---
    $("#user_pw, #user_pw_confirm").on("keyup", function() {
        const pw = $("#user_pw").val();
        const pwConfirm = $("#user_pw_confirm").val();
        
        if(pw === pwConfirm && pw !== "") { 
            $("#pwCheckMsg").html("<span class='msg-ok'>비밀번호가 일치합니다.</span>"); 
            isPwMatched = true; 
        } else { 
            $("#pwCheckMsg").html("<span class='msg-no'>비밀번호가 일치하지 않습니다.</span>"); 
            isPwMatched = false; 
        }
    });

    // --- [기능 3] 전송 전 최종 검사 ---
    $("#joinForm").submit(function() {
        if(!isIdChecked) { alert("아이디 중복확인을 해주세요."); return false; }
        if(!isPwMatched) { alert("비밀번호가 일치하지 않습니다."); return false; }
        
        // 여기까지 오면 폼 내용이 컨트롤러(/member/signup/ownerStep1)로 날아감
        return true;
    });

    // --- [기능 4] 주소 검색 및 좌표 변환 ---
    const geocoder = new kakao.maps.services.Geocoder();
    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                // 주소 입력
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById('user_zip').value = data.zonecode;
                document.getElementById('user_addr1').value = addr;
                
                // 좌표 변환
                geocoder.addressSearch(addr, function(results, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var result = results[0];
                        document.getElementById('user_lat').value = result.y;
                        document.getElementById('user_lon').value = result.x;
                        var msg = "📍 좌표 추출 완료! (위도: " + result.y + ", 경도: " + result.x + ")";
                        $("#coordStatus").html("<span class='msg-ok'>" + msg + "</span>");
                    } else {
                        $("#coordStatus").html("<span class='msg-no'>❌ 좌표 추출 실패</span>");
                    }
                });
                document.getElementById('user_addr2').focus();
            }
        }).open();
    }

    // --- [기능 5] 전화번호 자동 하이픈 ---
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
