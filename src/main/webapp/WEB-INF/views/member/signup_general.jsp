<%-- 
    [1] 페이지 설정 지시어
    - isELIgnored="false": ${kakaoJsKey} 같은 ${...} 표현식을 자바 코드로 해석하라는 뜻입니다.
      (이게 "true"면 그냥 글자로 화면에 나옵니다.)
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>일반 회원가입</title>

<!-- 
    [2] 필수 라이브러리 로딩
    1. jQuery: 복잡한 자바스크립트를 짧고 쉽게 쓰기 위해 가져옴 ($.ajax 등을 쓰기 위함)
    2. Kakao Map API: 주소를 좌표(위도/경도)로 바꾸기 위해 필요함
       - ${kakaoJsKey}: 컨트롤러(MemberController)에서 넘겨준 API 키값
       - &libraries=services: 좌표 변환 기능(Geocoder)을 쓰려면 이 옵션 필수!
    3. Daum Postcode: "우편번호 검색" 팝업창을 띄우는 기능
-->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
    /* 성공 메시지 (초록색) */
    .msg-ok { color: green; font-size: 12px; font-weight: bold; }
    /* 실패 메시지 (빨간색) */
    .msg-no { color: red; font-size: 12px; font-weight: bold; }
    
    /* 테이블 디자인 */
    table { margin-top: 20px; border-collapse: collapse; /* 테두리 겹침 방지 */ }
    td { padding: 10px; /* 칸 안쪽 여백 */ }
</style>
</head>
<body>
    <h2 align="center">일반 회원가입</h2>

	<%-- 
        [수정 포인트] 
        action 주소를 컨트롤러의 @PostMapping("/joinProcess")와 일치시킵니다.
        ${pageContext.request.contextPath}/member/joinProcess 로 변경 
    --%>
	<form action="${pageContext.request.contextPath}/member/joinProcess"
		method="post" id="joinForm">

		<%-- 
            [보안 핵심] CSRF 토큰
            - Spring Security를 쓰면 "POST 전송" 할 때 무조건 이 토큰을 같이 보내야 함.
            - 없으면 "403 Forbidden" 에러 뜨면서 가입 안 됨.
            - hidden: 화면엔 안 보이고 몰래 같이 보냄.
        --%>
		<input type="hidden" name="${_csrf.parameterName}"
			value="${_csrf.token}" />

		<!-- [4] 숨겨진 좌표 데이터 
             사용자는 주소만 검색하지만, 우리는 몰래 위도(lat)/경도(lon)를 계산해서 DB에 저장함 -->
		<input type="hidden" name="user_lat" id="user_lat" value="0.0">
		<input type="hidden" name="user_lon" id="user_lon" value="0.0">

		<table border="1" align="center">
			<tr>
				<td>아이디</td>
				<td><input type="text" name="user_id" id="user_id"
					placeholder="아이디" required> <!-- type="button"을 안 쓰면 엔터 칠 때 전송(submit)되어 버리니 주의! -->
					<button type="button" id="btnIdCheck">중복확인</button> <!-- 결과 메시지(사용 가능/불가능) 띄울 빈 공간 -->
					<div id="idCheckMsg"></div></td>
			</tr>
			<tr>
				<td>비밀번호</td>
				<td><input type="password" name="user_pw" id="user_pw"
					placeholder="비밀번호" required></td>
			</tr>
			<tr>
				<td>비밀번호 확인</td>
				<td><input type="password" id="user_pw_confirm"
					placeholder="비밀번호 재입력" required> <!-- 비번 일치 여부 메시지 공간 -->
					<div id="pwCheckMsg"></div></td>
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
					<!-- oninput="autoHyphen(this)": 키보드 칠 때마다 자동으로 하이픈(-) 넣어주는 함수 호출 -->
					<input type="text" name="user_tel" required placeholder="숫자만 입력하세요"
					maxlength="13" oninput="autoHyphen(this)">
				</td>
			</tr>
			<tr>
				<td>주소</td>
				<td>
					<!-- readonly: 사용자가 직접 타이핑 못 하게 막음 (오타 방지) --> <input type="text"
					name="user_zip" id="user_zip" placeholder="우편번호" readonly>
					<button type="button" onclick="execDaumPostcode()">주소검색</button> <br>

					<input type="text" name="user_addr1" id="user_addr1"
					placeholder="기본주소" size="40" readonly><br> <input
					type="text" name="user_addr2" id="user_addr2" placeholder="상세주소 입력">

					<!-- 좌표 변환 결과 보여줄 공간 -->
					<div id="coordStatus"
						style="color: blue; font-size: 12px; margin-top: 5px;">주소를
						검색하면 자동으로 위도/경도가 입력됩니다.</div>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="center"><input type="submit"
					value="가입하기"> <input type="button" value="취소"
					onclick="location.href='${pageContext.request.contextPath}/'">
				</td>
			</tr>
		</table>
	</form>

	<script>
    // [5] 전역 변수: 최종 제출 전에 "검사 통과했나?" 기록해두는 깃발
    let isIdChecked = false; // 중복확인 했는지?
    let isPwMatched = false; // 비번 두 개가 똑같은지?

    // --- [기능 1] 아이디 중복확인 (AJAX) ---
    $("#btnIdCheck").click(function() {
        const userId = $("#user_id").val();
        if(userId.length < 3) { alert("아이디는 3글자 이상 입력해주세요."); return; }

        // $.ajax: 페이지 이동 없이 서버랑 몰래 통신하기
        $.ajax({
            url: "${pageContext.request.contextPath}/member/idCheck", // 컨트롤러 주소
            type: "POST",
            data: { 
                user_id: userId,
                // [중요] AJAX도 POST로 보낼 땐 CSRF 토큰을 같이 실어 보내야 함!! (없으면 403 에러)
                "${_csrf.parameterName}": "${_csrf.token}" 
            },
            success: function(res) {
                // 컨트롤러가 "success"라고 답장하면 통과
                if(res === "success") { 
                    $("#idCheckMsg").html("<span class='msg-ok'>사용 가능한 아이디입니다.</span>"); 
                    isIdChecked = true; // 깃발 올림 (통과!)
                } else { 
                    $("#idCheckMsg").html("<span class='msg-no'>이미 사용 중인 아이디입니다.</span>");
                    isIdChecked = false; // 깃발 내림
                }
            },
            error: function() { alert("서버 통신 오류입니다."); }
        });
    });

    // 사용자가 아이디를 고치면? 다시 검사해야 하므로 깃발 내림
    $("#user_id").on("input", function() { isIdChecked = false; $("#idCheckMsg").text(""); });

    // --- [기능 2] 비밀번호 일치 확인 ---
    // keyup: 키보드 눌렀다 뗄 때마다 검사
    $("#user_pw, #user_pw_confirm").on("keyup", function() {
        const pw = $("#user_pw").val();
        const pwConfirm = $("#user_pw_confirm").val();
        
        if(pw === "" && pwConfirm === "") { $("#pwCheckMsg").text(""); return; }
        
        if(pw === pwConfirm) { 
            $("#pwCheckMsg").html("<span class='msg-ok'>비밀번호가 일치합니다.</span>"); 
            isPwMatched = true; // 통과!
        } else { 
            $("#pwCheckMsg").html("<span class='msg-no'>비밀번호가 일치하지 않습니다.</span>"); 
            isPwMatched = false; // 실패
        }
    });

    // --- [기능 3] 최종 제출 전 검사 (유효성 검사) ---
    $("#joinForm").submit(function() {
        if(!isIdChecked) { alert("아이디 중복확인을 해주세요."); $("#user_id").focus(); return false; } // 전송 막음
        if(!isPwMatched) { alert("비밀번호가 일치하지 않습니다."); $("#user_pw").focus(); return false; } // 전송 막음
        return true; // 전송 허용
    });

    // --- [기능 4] 주소 검색 및 좌표 변환 (핵심!) ---
    // Geocoder: 주소를 주면 좌표(위도, 경도)를 알려주는 카카오 도구
    const geocoder = new kakao.maps.services.Geocoder();

    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                // 1. 주소 넣기 (도로명 주소 우선)
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById('user_zip').value = data.zonecode;
                document.getElementById('user_addr1').value = addr;

                // 2. 주소 -> 좌표 변환 (Geocoder 사용)
                geocoder.addressSearch(addr, function(results, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var result = results[0]; // 첫 번째 검색 결과
                        
                        // [중요] 찾아낸 좌표를 숨겨진 input 태그(hidden)에 몰래 넣음
                        // 사용자는 모르지만, 가입 버튼 누르면 이 좌표도 같이 DB로 전송됨
                        document.getElementById('user_lat').value = result.y; // 위도
                        document.getElementById('user_lon').value = result.x; // 경도
                        
                        var msg = "📍 좌표 추출 완료! (위도: " + result.y + ", 경도: " + result.x + ")";
                        $("#coordStatus").html("<span class='msg-ok'>" + msg + "</span>");
                    } else {
                        $("#coordStatus").html("<span class='msg-no'>❌ 좌표 추출 실패</span>");
                    }
                });
                
                // 3. 상세주소 입력칸으로 포커스 이동
                document.getElementById('user_addr2').focus();
            }
        }).open();
    }

    // --- [기능 5] 전화번호 자동 하이픈 (-) ---
    // 숫자만 남기고 -> 010-1234-5678 형식으로 바꿔줌
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
