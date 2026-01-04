<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="common/header.jsp" />

<div style="width: 80%; margin: 0 auto; text-align: center;">

    <div style="margin: 30px 0; padding: 20px; border: 1px solid #ccc; background-color: #f0f0f0;">
        <h3>오늘 뭐 먹지?</h3>
        <form action="${pageContext.request.contextPath}/store/list" method="get">
            <input type="text" name="keyword" placeholder="가게 이름 또는 메뉴 검색" style="width: 300px; padding: 5px;">
            <input type="submit" value="맛집 검색" style="padding: 5px 15px; background: #333; color: white; border: none;">
        </form>
    </div>

    <div style="margin-bottom: 30px;">
        <h4>카테고리별 모아보기</h4>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list?category=한식'" style="padding: 10px; cursor: pointer;">한식 🍚</button>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list?category=일식'" style="padding: 10px; cursor: pointer;">일식 🍣</button>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list?category=양식'" style="padding: 10px; cursor: pointer;">양식 🍝</button>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list?category=중식'" style="padding: 10px; cursor: pointer;">중식 🥡</button>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list?category=카페'" style="padding: 10px; cursor: pointer;">카페 ☕</button>
        <button onclick="location.href='${pageContext.request.contextPath}/store/list'" style="padding: 10px; cursor: pointer; background: #eee;">전체보기</button>
    </div>

    <hr>

    <div style="margin-top: 30px;">
        <h3 style="color: orange;">🔥 실시간 인기 맛집 (Top 6)</h3>
        
        <table border="1" cellpadding="10" cellspacing="0" style="width: 100%; border-collapse: collapse;">
            <thead style="background-color: #eee;">
                <tr>
                    <th width="15%">이미지</th>
                    <th width="10%">분류</th>
                    <th width="20%">가게이름</th>
                    <th width="40%">위치</th>
                    <th width="15%">조회수</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty storeList}">
                        <c:forEach var="store" items="${storeList}">
                            <tr onclick="location.href='${pageContext.request.contextPath}/store/detail?storeId=${store.store_id}'" 
                                style="cursor: pointer;" onmouseover="this.style.background='#f9f9f9'" onmouseout="this.style.background='white'">
                                
                                <td align="center">
                                    <c:if test="${not empty store.store_img}">
                                        <img src="${pageContext.request.contextPath}/upload/${store.store_img}" width="80" height="60" style="object-fit:cover;">
                                    </c:if>
                                    <c:if test="${empty store.store_img}">
                                        <span style="color: #ccc; font-size: 12px;">No Image</span>
                                    </c:if>
                                </td>
                                
                                <td align="center">[${store.store_category}]</td>
                                <td align="center" style="font-weight: bold; font-size: 16px;">
                                    ${store.store_name}
                                    <c:if test="${store.store_cnt >= 100}">
                                        <span style="color:red; font-size:10px;">HOT</span>
                                    </c:if>
                                </td>
                                <td align="left" style="padding-left: 10px;">${store.store_addr1}</td>
                                <td align="center" style="color: red; font-weight: bold;">${store.store_cnt}</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" style="padding: 30px; text-align: center; color: gray;">
                                현재 등록된 인기 맛집이 없습니다.<br>
                                (데이터베이스를 확인해 주세요)
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="common/footer.jsp" />