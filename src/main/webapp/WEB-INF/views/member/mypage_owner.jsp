<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../common/header.jsp"/>

<%-- [관심사 분리] 공용 마이페이지 스타일 및 통합 스크립트 연결 --%>
<link rel="stylesheet" href="<c:url value='/resources/css/mypage.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/member.css'/>">
<script src="<c:url value='/resources/js/mypage.js'/>"></script>

<div class="edit-wrapper" style="max-width: 1200px;">
    <div class="profile-card">
        <div class="profile-info">
            <span class="profile-label" style="color: #2f855a;">OWNER PROFILE</span>
            <h2 class="user-name">${member.user_nm} <small>점주님</small></h2>
            <p class="user-meta">ID: ${member.user_id} | TEL: ${member.user_tel}</p>
        </div>
        <div class="btn-group" style="margin: 0; width: auto;">
            <a href="<c:url value='/member/edit'/>" class="btn-wire"
               style="height: 45px; padding: 0 20px; font-size: 14px;">정보 수정</a>
            <form action="<c:url value='/logout'/>" method="post" style="display: inline;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit" class="btn-wire btn-logout"
                        style="height: 45px; padding: 0 20px; font-size: 14px; margin-left: 10px;">로그아웃
                </button>
            </form>
        </div>
    </div>

    <c:set var="hasThumbnail" value="false"/>
    <c:forEach var="photo" items="${photo_list}">
        <c:if test="${photo.is_thumbnail == 'Y' && photo.is_active == 'Y'}">
            <c:set var="hasThumbnail" value="true"/>
        </c:if>
    </c:forEach>

    <c:choose>
        <c:when test="${not empty store}">
            <a href="<c:url value='/book/manage?store_id=${store.store_id}'/>" class="status-btn-full">
                ⚙️ 실시간 예약 및 웨이팅 관리 센터
            </a>

            <div class="dashboard-grid">
                <aside class="dashboard-card store-info-card">
                    <div class="card-header">
                        <h3 class="card-title">🏨 내 가게 정보</h3>
                        <button class="btn-wire btn-mini" style="margin-left: auto;"
                                onclick="location.href='<c:url value='/store/update?store_id=${store.store_id}'/>'">수정
                        </button>
                    </div>
                    <div style="text-align: center; margin-bottom: 25px;">
                        <div style="border: 2px solid #333; border-radius: 12px; overflow: hidden; height: 210px; background: #f9f9f9; margin-bottom: 15px;">
                            <c:choose>
                                <c:when test="${not empty store.store_img}">
                                    <img src="<c:url value='/upload/${store.store_img}'/>"
                                         style="width:100%; height:100%; object-fit:cover;">
                                </c:when>
                                <c:otherwise>
                                    <div style="line-height:210px; color:#ccc; font-weight:900;">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <h4 style="font-size: 22px; font-weight: 900; margin-bottom: 8px;">${store.store_name}</h4>
                        <span class="badge-wire">${store.store_category}</span>
                    </div>
                    <table class="edit-table" style="font-size: 14px;">
                        <tr>
                            <th style="width: 40%;">영업시간</th>
                            <td>${store.open_time} ~ ${store.close_time}</td>
                        </tr>
                        <tr>
                            <th>카테고리</th>
                            <td>${store.store_category}</td>
                        </tr>
                        <tr>
                            <th>예약단위</th>
                            <td>${store.res_unit}분</td>
                        </tr>
                        <tr>
                            <th>가게번호</th>
                            <td>${store.store_tel}</td>
                        </tr>
                        <tr>
                            <th>최대 수용 인원</th>
                            <td>${store.max_capacity}</td>
                        </tr>

                    </table>
                </aside>

                <section class="dashboard-card photo-manage-card">
                    <div class="card-header">
                        <h3 class="card-title">📷 매장 사진 관리</h3>
                    </div>
                    <form action="<c:url value='/photo/upload'/>" method="post" enctype="multipart/form-data" style="margin-bottom: 20px;" id="photoUploadForm">
                        <input type="hidden" name="store_id" value="${store.store_id}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div style="display: flex; gap: 10px; align-items: center;">
                            <button type="button" class="btn-submit" id="photoUploadBtn" style="width: auto; height: 38px; padding: 0 15px; font-size: 14px;">사진 업로드</button>
                            <div id="photoUploadControls" class="is-hidden" style="display: none; gap: 10px; align-items: center;">
                                <input type="file" name="files" id="photoFiles" multiple required class="is-hidden">
                                <button type="button" class="btn-wire is-hidden" id="photoPreviewClear" style="display: none; height: 38px; padding: 0 15px; font-size: 14px;">선택 취소</button>
                            </div>
                        </div>
                    </form>
                    <div class="photo-preview is-hidden" id="photoPreviewWrap" style="margin-bottom: 20px;">
                        <div class="photo-section-title">선택된 사진 미리보기</div>
                        <div id="photoPreview" class="favorite-grid"></div>
                    </div>
                    <c:if test="${!hasThumbnail}">
                        <div class="empty-status-box" style="margin-bottom: 20px; text-align: center; padding: 12px 0; color: #999; font-weight: 900;">
                            * 현재 등록된 썸네일 사진이 없습니다.
                        </div>
                    </c:if>
                    <div class="photo-section-title">등록된 사진</div>
                    <div class="favorite-grid">
                        <c:choose>
                            <c:when test="${not empty photo_list}">
                                <c:forEach var="photo" items="${photo_list}">
                                    <div class="favorite-card">
                                        <c:if test="${photo.is_thumbnail == 'Y'}">
                                            <span class="badge-wire" style="position:absolute; top:10px; left:10px;">썸네일</span>
                                        </c:if>
                                        <c:if test="${photo.is_active != 'Y'}">
                                            <span class="badge-wire" style="position:absolute; top:10px; right:10px;">비공개</span>
                                        </c:if>
                                        <div class="favorite-thumb">
                                            <img src="<c:url value='/upload/${photo.file_path}'/>" alt="${photo.original_name}">
                                        </div>
                                        <div class="favorite-info">
                                            <div style="display:flex; gap:6px; justify-content: space-between; align-items:center;">
                                                <form action="<c:url value='/photo/thumbnail'/>" method="post">
                                                    <input type="hidden" name="store_id" value="${store.store_id}">
                                                    <input type="hidden" name="photo_id" value="${photo.photo_id}">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                    <button type="submit" class="btn-wire" style="height: 30px; padding: 0 8px; font-size: 12px;" ${photo.is_thumbnail == 'Y' ? 'disabled' : ''}>썸네일 지정</button>
                                                </form>
                                                <div class="photo-actions">
                                                    <c:choose>
                                                        <c:when test="${photo.is_active == 'Y'}">
                                                            <form action="<c:url value='/photo/delete'/>" method="post">
                                                                <input type="hidden" name="photo_id" value="${photo.photo_id}">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn-wire photo-deactivate"
                                                                        data-is-thumbnail="${photo.is_thumbnail == 'Y'}"
                                                                        style="height: 30px; padding: 0 8px; font-size: 12px; color:#dc3545; border-color:#dc3545;">내리기</button>
                                                            </form>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <form action="<c:url value='/photo/activate'/>" method="post">
                                                                <input type="hidden" name="photo_id" value="${photo.photo_id}">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn-wire" style="height: 30px; padding: 0 8px; font-size: 12px;">올리기</button>
                                                            </form>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <form action="<c:url value='/photo/remove'/>" method="post">
                                                        <input type="hidden" name="photo_id" value="${photo.photo_id}">
                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                        <button type="submit" class="btn-wire photo-delete"
                                                                data-is-thumbnail="${photo.is_thumbnail == 'Y'}"
                                                                style="height: 30px; padding: 0 8px; font-size: 12px; color:#dc3545; border-color:#dc3545;">삭제</button>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-status-box" style="grid-column: 1/-1; text-align: center; padding: 40px 0; color: #ccc; font-weight: 900;">
                                    등록된 매장 사진이 없습니다.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <section class="dashboard-card menu-manage-card">
                    <div class="card-header">
                        <h3 class="card-title">📋 메뉴 관리 (${menuList.size()})</h3>
                        <button class="btn-submit" style="width: auto; height: 38px; padding: 0 15px; font-size: 14px;"
                                onclick="location.href='<c:url value='/store/menu/register?store_id=${store.store_id}'/>'">+ 메뉴 추가
                        </button>
                    </div>
                    <div class="menu-grid">
                        <c:forEach var="menu" items="${menuList}">
                            <div class="menu-card">
                                <div class="menu-thumb">
                                    <c:if test="${not empty menu.menu_img}">
                                        <img src="<c:url value='/upload/${menu.menu_img}'/>" alt="${menu.menu_name}">
                                    </c:if>
                                </div>
                                <div class="menu-info">
                                    <div class="menu-title">
                                        ${menu.menu_name}
                                        <c:if test="${menu.menu_sign == 'Y'}"><span class="badge-best"
                                                                                    style="background:#ff3d00; color:#fff; padding:2px 5px; border-radius:4px; font-size:11px; margin-left:5px;">대표</span></c:if>
                                    </div>
                                    <div class="menu-price"><fmt:formatNumber value="${menu.menu_price}" pattern="#,###"/>원</div>
                                    <div class="menu-actions">
                                        <button class="btn-wire" style="height: 32px; padding: 0 10px; font-size: 12px;"
                                                onclick="location.href='<c:url value='/store/menu/update?menu_id=${menu.menu_id}'/>'">수정
                                        </button>
                                        <button class="btn-wire"
                                                style="height: 32px; padding: 0 10px; font-size: 12px; color: #dc3545; border-color: #dc3545;"
                                                onclick="deleteMenu(${menu.menu_id})">삭제
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </section>
            </div>

            <%-- [핵심 수정 섹션] 리뷰 요약 노출 및 전체보기 연동 --%>
            <div class="review-container" style="margin-top: 30px;">
                <div class="card-header"
                     style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3 class="section-title" style="margin: 0;">💬 우리 가게 리뷰 (${store_review_list.size()})</h3>
                        <%-- store_detail.jsp와 동일한 버튼 스타일 및 경로 적용 --%>
                    <a href="<c:url value='/review/list?store_id=${store.store_id}'/>" class="btn-wire-small">전체보기 ❯</a>
                </div>

                <div class="store-review-list">
                    <c:choose>
                        <c:when test="${not empty store_review_list}">
                            <%-- 최근 2개만 노출하기 위해 end="1" 설정 (0, 1 인덱스) --%>
                            <c:forEach var="review" items="${store_review_list}" end="1">
                                <div class="item-card">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 15px; border-bottom: 1px dashed #ddd; padding-bottom: 10px;">
                                        <div>
                                            <strong style="font-size: 16px;">${review.user_nm} 고객님</strong>
                                            <span style="color: #f1c40f; margin-left: 10px;">
                                                <c:forEach begin="1" end="${review.rating}">⭐</c:forEach>
                                            </span>
                                        </div>
                                        <span style="color: #999; font-size: 13px;">
                                            <fmt:formatDate value="${review.review_date}" pattern="yyyy.MM.dd"/>
                                        </span>
                                    </div>
                                    <div style="display: flex; gap: 20px;">
                                        <c:if test="${not empty review.img_url}">
                                            <img src="<c:url value='/upload/${review.img_url}'/>" class="item-img-thumb"
                                                 style="width:120px; height:120px; object-fit: cover; border-radius: 8px;">
                                        </c:if>
                                        <p style="line-height: 1.6; font-size: 15px; flex: 1;">${review.content}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="dashboard-card" style="text-align: center; padding: 50px 0; color: #999;">
                                아직 등록된 리뷰가 없습니다.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="dashboard-card" style="text-align: center; padding: 100px 0;">
                <h3 style="font-size: 26px; font-weight: 900;">연결된 매장 정보가 없습니다.</h3>
                <p style="color: #666; margin-top: 15px;">가게 정보를 등록하여 Gourmet Pass 서비스를 시작하세요!</p>
                <button class="btn-submit" style="width: 300px; height: 55px; margin-top: 30px;"
                        onclick="location.href='<c:url value='/member/signup/owner2'/>'">지금 바로 등록하기
                </button>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    (function () {
        var form = document.getElementById("photoUploadForm");
        var uploadBtn = document.getElementById("photoUploadBtn");
        var controls = document.getElementById("photoUploadControls");
        var input = document.getElementById("photoFiles");
        var preview = document.getElementById("photoPreview");
        var previewWrap = document.getElementById("photoPreviewWrap");
        if (!uploadBtn || !controls || !input || !preview || !form) return;
        var clearBtn = document.getElementById("photoPreviewClear");
        if (clearBtn) {
            clearBtn.classList.add("is-hidden");
            clearBtn.style.display = "none";
        }

        input.addEventListener("change", function () {
            preview.innerHTML = "";
            var files = Array.from(input.files || []);
            if (files.length > 5) {
                alert("사진은 최대 5장까지만 선택할 수 있습니다.");
                input.value = "";
                previewWrap.classList.add("is-hidden");
                if (clearBtn) {
                    clearBtn.classList.add("is-hidden");
                    clearBtn.style.display = "none";
                }
                return;
            }
            if (files.length) {
                previewWrap.classList.remove("is-hidden");
                if (clearBtn) {
                    clearBtn.classList.remove("is-hidden");
                    clearBtn.style.display = "inline-block";
                }
            } else {
                previewWrap.classList.add("is-hidden");
                if (clearBtn) {
                    clearBtn.classList.add("is-hidden");
                    clearBtn.style.display = "none";
                }
            }
            files.forEach(function (file) {
                if (!file.type || !file.type.startsWith("image/")) {
                    return;
                }
                var reader = new FileReader();
                reader.onload = function (e) {
                    var card = document.createElement("div");
                    card.className = "favorite-card";
                    var thumb = document.createElement("div");
                    thumb.className = "favorite-thumb";
                    var img = document.createElement("img");
                    img.src = e.target.result;
                    img.alt = file.name;
                    thumb.appendChild(img);
                    card.appendChild(thumb);
                    preview.appendChild(card);
                };
                reader.readAsDataURL(file);
            });
        });

        uploadBtn.addEventListener("click", function () {
            if (controls.classList.contains("is-hidden")) {
                controls.classList.remove("is-hidden");
                controls.style.display = "flex";
                if (clearBtn) {
                    clearBtn.classList.add("is-hidden");
                    clearBtn.style.display = "none";
                }
                input.click();
                return;
            }
            if (input.files && input.files.length > 0) {
                form.submit();
                return;
            }
            input.click();
        });
    })();

    (function () {
        var input = document.getElementById("photoFiles");
        var preview = document.getElementById("photoPreview");
        var clearBtn = document.getElementById("photoPreviewClear");
        var controls = document.getElementById("photoUploadControls");
        var previewWrap = document.getElementById("photoPreviewWrap");
        if (clearBtn && input && preview && controls && previewWrap) {
            clearBtn.addEventListener("click", function () {
                input.value = "";
                preview.innerHTML = "";
                previewWrap.classList.add("is-hidden");
                controls.classList.add("is-hidden");
                controls.style.display = "none";
                clearBtn.classList.add("is-hidden");
                clearBtn.style.display = "none";
            });
        }

        document.addEventListener("click", function (e) {
            if (!e.target.classList.contains("photo-deactivate")) return;
            var isThumbnail = e.target.dataset.isThumbnail === "true";
            if (!isThumbnail) return;
            var ok = confirm("메인 페이지에 보이는 사진입니다. 정말 내리시겠습니까?");
            if (!ok) {
                e.preventDefault();
                alert("삭제가 취소되었습니다.");
            }
        });

        document.addEventListener("click", function (e) {
            if (!e.target.classList.contains("photo-delete")) return;
            var isThumbnail = e.target.dataset.isThumbnail === "true";
            if (!isThumbnail) return;
            var ok = confirm("메인 페이지에 보이는 사진입니다. 정말 삭제하시겠습니까?");
            if (!ok) {
                e.preventDefault();
                alert("삭제가 취소되었습니다.");
            }
        });
    })();
</script>

<jsp:include page="../common/footer.jsp"/>
