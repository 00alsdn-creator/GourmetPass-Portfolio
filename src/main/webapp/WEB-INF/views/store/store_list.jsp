<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="../common/header.jsp" />

<div style="padding: 20px;">
    <h2>🍽️ 맛집 목록 (Store List)</h2>
    
    <div style="margin-bottom: 20px; border: 1px solid #ccc; padding: 15px; background: #f9f9f9;">
        <form action="${pageContext.request.contextPath}/store/list" method="get">
            <b>지역 선택:</b>
            <select name="region">
                <option value="">전체 지역</option>
                <option value="서울" ${region == '서울' ? 'selected' : ''}>서울</option>
                <option value="경기" ${region == '경기' ? 'selected' : ''}>경기</option>
                <option value="부산" ${region == '부산' ? 'selected' : ''}>부산</option>
            </select>
            
            &nbsp;|&nbsp; 
            
            <b>카테고리:</b>
            <a href="list">전체</a> |
            <a href="list?category=한식">한식</a> |
            <a href="list?category=일식">일식</a> |
            <a href="list?category=중식">중식</a> |
            <a href="list?category=양식">양식</a>
            
            <button type="submit">검색 적용</button>
        </form>
    </div>

    <table border="1" cellpadding="10" cellspacing="0" width="100%">
        <tr style="background-color: #eee;">
            <th>사진</th>
            <th>카테고리</th>
            <th>가게명</th>
            <th>지역/주소</th>
            <th>조회수</th>
            <th>상세보기</th>
        </tr>
        
        <c:choose>
            <c:when test="${not empty storeList}">
                <c:forEach var="store" items="${storeList}">
                    <tr>
                        <td align="center" width="100">
                            <c:if test="${not empty store.store_img}">
                                <img src="/upload/${store.store_img}" width="80" height="60">
                            </c:if>
                            <c:if test="${empty store.store_img}">
                                <span style="color:gray; font-size:12px;">(이미지 없음)</span>
                            </c:if>
                        </td>
                        <td align="center">${store.store_category}</td>
                        <td>
                            <b>${store.store_name}</b>
                            <c:if test="${store.store_cnt > 100}">
                                <span style="color:red; font-size:10px;">(HOT)</span>
                            </c:if>
                        </td>
                        <td>${store.store_addr1}</td>
                        <td align="center">${store.store_cnt}</td>
                        <td align="center">
                            <button onclick="location.href='detail?storeId=${store.store_id}'">
                                방문하기
                            </button>
                        </td>
                    </tr>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <tr>
                    <td colspan="6" align="center" height="100">
                        등록된 맛집이 없습니다.
                    </td>
                </tr>
            </c:otherwise>
        </c:choose>
    </table>
</div>

<jsp:include page="../common/footer.jsp" />