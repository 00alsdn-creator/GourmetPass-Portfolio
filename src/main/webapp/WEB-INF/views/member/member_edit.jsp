<%-- WEB-INF/views/member/member_edit.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../common/header.jsp" />

<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">

<div class="edit-wrapper">
    <div class="edit-title">⚙️ 회원 정보 수정</div>

    <form action="<c:url value='/member/edit'/>" method="post" id="joinForm">
        <%-- CSRF 토큰 및 위치 정보 --%>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input type="hidden" name="user_lat" id="user_lat" value="${member.user_lat}">
        <input type="hidden" name="user_lon" id="user_lon" value="${member.user_lon}">

        <table class="edit-table">
            <tr>
                <th>아이디</th>
                <td>
                    <%-- readonly 속성으로 인해 member-signup.js에서 중복확인을 자동 통과함 --%>
                    <input type="text" name="user_id" id="user_id" value="${member.user_id}" readonly>
                </td>
            </tr>
            <tr>
                <th>새 비밀번호</th>
                <td><input type="password" name="user_pw" id="user_pw" placeholder="변경 시에만 입력하세요"></td>
            </tr>
            <tr>
                <th>비밀번호 확인</th>
                <td>
                    <input type="password" id="user_pw_confirm" placeholder="비밀번호를 한 번 더 입력하세요">
                    <div id="pwCheckMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>성명</th>
                <td><input type="text" name="user_nm" value="${member.user_nm}" required placeholder="성함을 입력하세요"></td>
            </tr>
            <tr>
                <th>전화번호</th>
                <td>
                    <input type="text" name="user_tel" value="${member.user_tel}" required 
                           oninput="autoHyphen(this)" maxlength="13" placeholder="숫자만 입력">
                </td>
            </tr>

            <%-- 이메일 인증 섹션 --%>
            <tr>
                <th>이메일</th>
                <td>
                    <div class="input-row">
                        <input type="email" name="user_email" id="user_email" value="${member.user_email}" required placeholder="example@mail.com">
                        <button type="button" id="btnEmailAuth" class="btn-wire">인증코드 발송</button>
                    </div>
                    <div id="emailMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>인증코드</th>
                <td>
                    <div class="input-row">
                        <input type="text" id="auth_code" disabled placeholder="인증코드 6자리" maxlength="6">
                        <span id="timer" style="color:red; margin-left:10px; font-weight:bold;"></span>
                    </div>
                    <div id="authMsg" class="msg-box"></div>
                </td>
            </tr>

            <tr>
                <th>주소</th>
                <td>
                    <div class="input-row mb-10">
                        <input type="text" name="user_zip" id="user_zip" value="${member.user_zip}" 
                               style="width: 120px; flex: none;" readonly placeholder="우편번호">
                        <button type="button" onclick="execDaumPostcode('user')" class="btn-wire">주소검색</button>
                    </div>
                    <input type="text" name="user_addr1" id="user_addr1" value="${member.user_addr1}" 
                           class="mb-10" readonly placeholder="기본주소">
                    <input type="text" name="user_addr2" id="user_addr2" value="${member.user_addr2}" 
                           placeholder="상세 주소를 입력하세요">
                    <div id="coordStatus" class="msg-box msg-ok">주소 변경 시 위치 정보가 자동으로 갱신됩니다.</div>
                </td>
            </tr>
        </table>

        <div class="btn-group">
            <button type="submit" class="btn-submit">정보 수정 완료</button>
            <a href="<c:url value='/member/mypage'/>" class="btn-cancel">취소</a>
        </div>
    </form>

    <div class="withdraw-section">
        <%-- dropUser 함수는 member-signup.js에 통합됨 --%>
        <button type="button" class="btn-link-withdraw" onclick="dropUser('${member.user_id}')">
            회원 탈퇴하기
        </button>
    </div>
</div>

<%-- 외부 API 및 공통 스크립트 --%>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="<c:url value='/resources/js/address-api.js'/>"></script>
<script src="<c:url value='/resources/js/common.js'/>"></script>

<%-- [주의] member.js는 member-signup.js와 충돌하므로 로드하지 않음 --%>

<script type="text/javascript">
    [cite_start]<%-- 전역 설정 객체 [cite: 16] --%>
    var APP_CONFIG = APP_CONFIG || {
        contextPath: "${pageContext.request.contextPath}",
        csrfName: "${_csrf.parameterName}",
        csrfToken: "${_csrf.token}"
    };
</script>

<%-- 모든 검증 및 이관된 탈퇴 로직을 포함하는 통합 스크립트 --%>
<script src="<c:url value='/resources/js/member-signup.js'/>"></script>

<jsp:include page="../common/footer.jsp" />