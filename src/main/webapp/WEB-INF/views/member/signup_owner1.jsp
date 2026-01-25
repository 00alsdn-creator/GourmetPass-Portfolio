<%-- WEB-INF/views/member/signup_owner1.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="../common/header.jsp" />
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">

<div class="edit-wrapper">
    <div class="edit-title">👨‍🍳 점주 가입 - 1단계 (계정)</div>
    
    <form action="${pageContext.request.contextPath}/member/signup/ownerStep1" method="post" id="joinForm">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <table class="edit-table">
            <tr>
                <th>아이디</th>
                <td>
                    <div class="input-row">
                        <input type="text" name="user_id" id="user_id" required placeholder="3글자 이상">
                        <button type="button" id="btnIdCheck" class="btn-wire">중복확인</button>
                    </div>
                    <div id="idCheckMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>비밀번호</th>
                <td><input type="password" name="user_pw" id="user_pw" required placeholder="비밀번호를 입력하세요"></td>
            </tr>
            <tr>
                <th>비밀번호 확인</th>
                <td>
                    <input type="password" id="user_pw_confirm" required placeholder="비밀번호를 다시 입력하세요">
                    <div id="pwCheckMsg" class="msg-box"></div>
                </td>
            </tr>
            <tr>
                <th>성명</th>
                <td><input type="text" name="user_nm" required placeholder="본인의 실명을 입력하세요"></td>
            </tr>
            <tr>
                <th>전화번호</th>
                <td><input type="text" name="user_tel" required oninput="autoHyphen(this)" maxlength="13" placeholder="숫자만 입력"></td>
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
                <th>거주지 주소</th>
                <td>
                    <div class="input-row mb-10">
                        <input type="text" name="user_zip" id="user_zip" style="width:120px; flex:none;" readonly placeholder="우편번호">
                        <button type="button" onclick="execDaumPostcode('user')" class="btn-wire">주소검색</button>
                    </div>
                    <input type="text" name="user_addr1" id="user_addr1" class="mb-10" readonly placeholder="기본주소">
                    <input type="text" name="user_addr2" id="user_addr2" placeholder="상세주소">
                    <div id="coordStatus" class="msg-box msg-ok">주소 검색 시 좌표가 자동 입력됩니다.</div>
                </td>
            </tr>
        </table>

        <div class="btn-group">
            <button type="submit" class="btn-submit">다음 단계로 (가게 정보 입력)</button>
            <a href="<c:url value='/member/signup/select'/>" class="btn-cancel">이전으로</a>
        </div>
    </form>
</div>

<%-- 외부 라이브러리 및 공통 스크립트 --%>
<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&libraries=services"></script>
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="<c:url value='/resources/js/address-api.js'/>"></script>
<script src="<c:url value='/resources/js/common.js'/>"></script>

<%-- [핵심] member.js 로드 제거: 통합된 member-signup.js와 충돌을 방지합니다 --%>

<script type="text/javascript">
    [cite_start]<%-- 전역 설정 객체: member-signup.js 실행 전에 선언되어야 함 [cite: 16] --%>
    var APP_CONFIG = APP_CONFIG || {
        contextPath: "${pageContext.request.contextPath}",
        csrfName: "${_csrf.parameterName}",
        csrfToken: "${_csrf.token}"
    };
</script>

<%-- 통합된 가입/검증 스크립트 --%>
<script src="<c:url value='/resources/js/member-signup.js'/>"></script>

<jsp:include page="../common/footer.jsp" />