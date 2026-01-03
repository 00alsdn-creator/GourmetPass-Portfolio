<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>점주 회원가입 - 1단계</title>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<%-- 카카오 지도 API --%>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakao.js.key}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
    /* 상태 메시지 스타일 */
    .msg-ok { color: green; font-size: 12px; font-weight: bold; }
    .msg-no { color: red; font-size: 12px; font-weight: bold; }
</style>
</head>
<body>
    <h2 align="center">점주 회원가입 - 1단계 (계정)</h2>
    
    <form action="${pageContext.request.contextPath}/join/ownerStep1.do" method="post" id="joinForm">
        
        <%-- 사장님 개인 위치 좌표 (MemberVO에 매핑됨) --%>
        <input type="hidden" name="user_lat" id="user_lat" value="0.0">
        <input type="hidden" name="user_lon" id="user_lon" value="0.0">

        <table border="1" align="center" cellpadding="5">
            <tr>
                <td width="120">아이디</td>
                <td>
                    <input type="text" name="user_id" id="user_id" placeholder="아이디" required>
                    <button type="button" id="btnIdCheck">중복확인</button>
                    <div id="idCheckMsg"></div>
                </td>
            </tr>
            <tr>
                <td>비밀번호</td>
                <td>
                    <input type="password" name="user_pw" id="user_pw" placeholder="비밀번호" required>
                </td>
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
                    <input type="text" name="user_tel" required placeholder="숫자만 입력하세요" maxlength="13" oninput="autoHyphen(this)">
                </td>
            </tr>
            <tr>
                <td>주소</td>
                <td>
                    <input type="text" name="user_zip" id="user_zip" placeholder="우편번호" readonly>
                    <button type="button" onclick="execDaumPostcode()">주소검색</button><br>
                    <input type="text" name="user_addr1" id="user_addr1" placeholder="기본주소" size="40" readonly><br>
                    <input type="text" name="user_addr2" id="user_addr2" placeholder="상세주소 입력">
                    <div id="coordStatus" style="color: blue; font-size: 12px; margin-top: 5px;">
                        주소를 검색하면 자동으로 위도/경도가 입력됩니다.
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <input type="submit" value="다음 단계로 (가게 정보)">
                    <input type="button" value="취소" onclick="location.href='${pageContext.request.contextPath}/'">
                </td>
            </tr>
        </table>
    </form>

<script>
    // === 1. 상태 플래그 ===
    let isIdChecked = false; // 아이디 중복 확인 여부
    let isPwMatched = false; // 비밀번호 일치 여부

    // === 2. 아이디 중복 체크 로직 (AJAX) ===
    $("#btnIdCheck").click(function() {
        const userId = $("#user_id").val();
        
        if(userId.length < 3) { 
            alert("아이디는 3글자 이상 입력해주세요."); 
            return; 
        }

        $.ajax({
            url: "${pageContext.request.contextPath}/idCheck.do",
            type: "POST",
            data: { user_id: userId },
            success: function(res) {
                if(res === "success") { 
                    $("#idCheckMsg").html("<span class='msg-ok'>사용 가능한 아이디입니다.</span>"); 
                    isIdChecked = true; 
                } else { 
                    $("#idCheckMsg").html("<span class='msg-no'>이미 사용 중인 아이디입니다.</span>"); 
                    isIdChecked = false; 
                }
            },
            error: function() {
                alert("서버 통신 오류입니다.");
            }
        });
    });

    // 아이디 입력창 변경 시 중복확인 초기화
    $("#user_id").on("input", function() { 
        isIdChecked = false; 
        $("#idCheckMsg").text(""); 
    });

    // === 3. 비밀번호 일치 확인 로직 ===
    $("#user_pw, #user_pw_confirm").on("keyup", function() {
        const pw = $("#user_pw").val();
        const pwConfirm = $("#user_pw_confirm").val();
        
        if(pw === "" && pwConfirm === "") { 
            $("#pwCheckMsg").text(""); 
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

    // === 4. 폼 전송 시 최종 검사 ===
    $("#joinForm").submit(function() {
        if(!isIdChecked) {
            alert("아이디 중복확인을 해주세요.");
            $("#user_id").focus();
            return false; // 전송 막기
        }
        if(!isPwMatched) {
            alert("비밀번호가 일치하지 않습니다.");
            $("#user_pw").focus();
            return false; // 전송 막기
        }
        return true; // 전송 허용
    });

    // === 5. 주소 API 및 좌표 출력 ===
    const geocoder = new kakao.maps.services.Geocoder();

    function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function(data) {
                var addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                document.getElementById('user_zip').value = data.zonecode;
                document.getElementById('user_addr1').value = addr;

                // 주소로 좌표를 검색하는 부분
                geocoder.addressSearch(addr, function(results, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var result = results[0];
                        
                        // 히든 필드에 값 넣기 (DB 전송용)
                        document.getElementById('user_lat').value = result.y;
                        document.getElementById('user_lon').value = result.x;
                        
                        // 화면에 좌표 표시
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

    // === 6. 전화번호 자동 하이픈 ===
    const autoHyphen = (target) => {
        target.value = target.value
            .replace(/[^0-9]/g, '') 
            .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, `$1-$2-$3`);
    }
</script>

</body>
</html>