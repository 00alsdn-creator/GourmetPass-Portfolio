<%-- WEB-INF/views/member/signup_general.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="../common/header.jsp" />
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">

<div class="edit-wrapper">
    <div class="edit-title">👤 일반 회원가입</div>

    <form action="${pageContext.request.contextPath}/member/joinProcess" method="post" id="joinForm">
        <%-- CSRF 보안 및 좌표 저장용 숨김 필드 --%>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input type="hidden" name="user_lat" id="user_lat" value="0.0">
        <input type="hidden" name="user_lon" id="user_lon" value="0.0">

        <table class="edit-table">
            <tr>
                <th>아이디</th>
                <td>
                    <div class="input-row">
                        <%-- 가입 페이지는 readonly가 아니므로 member-signup.js에서 중복확인을 강제함 [cite: 19] --%>
                        <input type="text" name="user_id" id="user_id" required placeholder="3글자 이상">
                        <button type="button" id="btnIdCheck" class="btn-wire">중복확인</button>
                    </div>
                    <div id="idCheckMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>비밀번호</th>
                <td><input type="password" name="user_pw" id="user_pw" required placeholder="비밀번호 입력"></td>
            </tr>
            <tr>
                <th>비밀번호 확인</th>
                <td>
                    <input type="password" id="user_pw_confirm" required placeholder="비밀번호 재입력">
                    <div id="pwCheckMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>이름</th>
                <td><input type="text" name="user_nm" required placeholder="성함을 입력하세요"></td>
            </tr>
            <tr>
                <th>전화번호</th>
                <td><input type="text" name="user_tel" required placeholder="숫자만 입력" maxlength="13" oninput="autoHyphen(this)"></td>
            </tr>
            
            <%-- 이메일 인증 섹션 --%>
            <tr>
                <th>이메일</th>
                <td>
                    <div class="input-row">
                        <input type="email" name="user_email" id="user_email" required placeholder="example@mail.com">
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
                        <input type="text" name="user_zip" id="user_zip" style="width: 120px; flex: none;" placeholder="우편번호" readonly>
                        <button type="button" onclick="execDaumPostcode('user')" class="btn-wire">주소검색</button>
                    </div>
                    <input type="text" name="user_addr1" id="user_addr1" class="mb-10" placeholder="기본주소" readonly>
                    <input type="text" name="user_addr2" id="user_addr2" placeholder="상세주소를 입력하세요">
                    <div id="coordStatus" class="msg-box msg-ok">주소 검색 시 좌표가 자동 설정됩니다.</div>
                </td>
            </tr>
        </table>

        <div class="btn-group">
            <button type="submit" class="btn-submit" id="btnSubmit">가입하기</button>
            <a href="${pageContext.request.contextPath}/" class="btn-cancel">취소</a>
        </div>
    </form>
</div>

<%-- 외부 API 및 공통 스크립트 --%>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="<c:url value='/resources/js/address-api.js'/>"></script>
<script src="<c:url value='/resources/js/common.js'/>"></script>

<script type="text/javascript">
    <%-- 전역 설정 객체 --%>
    var APP_CONFIG = APP_CONFIG || {
        contextPath: "${pageContext.request.contextPath}",
        csrfName: "${_csrf.parameterName}",
        csrfToken: "${_csrf.token}"
    };
</script>

<%-- 통합된 가입/검증 스크립트 (member.js 중복 호출 금지) --%>
<script src="<c:url value='/resources/js/member-signup.js'/>"></script>

<jsp:include page="../common/footer.jsp" />