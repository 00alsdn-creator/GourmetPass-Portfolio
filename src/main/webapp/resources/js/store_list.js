/* /resources/js/store_list.js */

/**
 * [기능] 페이지 이동 함수
 * @param {number} pageNum - 이동할 페이지 번호
 * 설명: 페이징 버튼 클릭 시 호출되며, 기존 검색 조건을 유지한 채 페이지 값만 바꿔 제출합니다.
 */
function movePage(pageNum) {
    const pageInput = document.getElementById('pageNum');
    if (pageInput) {
        pageInput.value = pageNum;
        submitFilter();
    }
}

/**
 * [기능] 필터 변경 시 초기화 함수
 * 설명: 지역이나 검색 조건이 바뀔 경우, 기존 페이지 번호를 1로 리셋한 뒤 검색을 수행합니다.
 */
function resetPageAndSubmit() {
    const pageInput = document.getElementById('pageNum');
    if (pageInput) {
        pageInput.value = 1; // 필터 변경 시 첫 페이지부터 다시 조회
    }
    submitFilter();
}

/**
 * [기능] 카테고리 선택/해제 핸들러
 * @param {string} cat - 선택된 카테고리명
 * 설명: 카테고리 칩 클릭 시 호출됩니다. 이미 선택된 카테고리라면 선택을 해제(전체보기)합니다.
 */
function selectCategory(cat) {
    const hiddenInput = document.getElementById('selectedCategory');
    const pageInput = document.getElementById('pageNum');
    
    if (hiddenInput) {
        // 이미 선택된 카테고리를 다시 누르면 해제(전체보기), 아니면 새로 선택
        if (hiddenInput.value === cat) {
            hiddenInput.value = "";
        } else {
            hiddenInput.value = cat;
        }
        
        // 페이지 번호를 1로 리셋 (카테고리 변경 대응)
        if (pageInput) pageInput.value = 1;
        
        submitFilter();
    }
}

/**
 * [추가] 검색어 동기화 및 실행 함수
 * 설명: 상단 검색창의 값을 hidden 필드에 복사하고 검색을 실행합니다. (버튼 클릭/엔터 공용)
 */
function syncAndSubmit() {
    const searchInput = document.querySelector(".wire-input");
    const hiddenKeyword = document.querySelector("#filterForm input[name='keyword']");
    
    if (hiddenKeyword && searchInput) {
        hiddenKeyword.value = searchInput.value;
    }
    resetPageAndSubmit();
}

/**
 * [핵심] 폼 제출 함수
 * 설명: filterForm을 서버(StoreController)로 전송합니다. 
 * 모든 hidden 필드와 검색 조건이 Criteria DTO에 바인딩됩니다.
 */
function submitFilter() {
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.submit();
    }
}

/**
 * [이벤트] 페이지 로드 시 검색창 엔터키 및 초기화 설정
 */
document.addEventListener("DOMContentLoaded", function() {
    // .wire-input 클래스를 가진 검색창에서 엔터키 입력 시 검색 실행
    const searchInput = document.querySelector(".wire-input");
    
    if (searchInput) {
        searchInput.addEventListener("keypress", function(e) {
            if (e.key === 'Enter') {
                e.preventDefault(); // 기본 폼 제출 방지
                syncAndSubmit(); // 동기화 로직 호출
            }
        });
    }

    const favoriteButtons = document.querySelectorAll(".favorite-toggle");
    if (!favoriteButtons.length) return;

    const contextPath = (typeof APP_CONFIG !== "undefined" && APP_CONFIG.contextPath)
        ? APP_CONFIG.contextPath
        : "";

    function updateFavoriteButton(btn, isFavorite) {
        if (isFavorite) {
            btn.classList.add("active");
            btn.textContent = "❤️";
        } else {
            btn.classList.remove("active");
            btn.textContent = "🤍";
        }
    }

    function loadFavorites() {
        $.ajax({
            url: contextPath + "/favorite/list",
            type: "GET",
            dataType: "json"
        }).done(function(res) {
            const storeIds = new Set((res.storeIds || []).map(String));
            favoriteButtons.forEach(function(btn) {
                const storeId = btn.dataset.storeId;
                updateFavoriteButton(btn, storeIds.has(String(storeId)));
            });
        });
    }

    favoriteButtons.forEach(function(btn) {
        btn.addEventListener("click", function(e) {
            e.preventDefault();
            e.stopPropagation();

            $.ajax({
                url: contextPath + "/favorite/toggle",
                type: "POST",
                data: {store_id: btn.dataset.storeId},
                beforeSend: function(xhr) {
                    if (typeof APP_CONFIG !== "undefined") {
                        xhr.setRequestHeader("X-CSRF-TOKEN", APP_CONFIG.csrfToken);
                    }
                }
            }).done(function(res) {
                updateFavoriteButton(btn, !!res.favorite);
            }).fail(function(xhr) {
                if (xhr.status === 401) {
                    alert("로그인이 필요합니다");
                } else {
                    alert("즐겨찾기 처리 중 오류가 발생했습니다.");
                }
            });
        });
    });

    loadFavorites();
});
