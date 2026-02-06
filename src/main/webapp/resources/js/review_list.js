/* resources/js/review_list.js */

document.addEventListener("DOMContentLoaded", function() {
    // 1. 컨텍스트 및 데이터 추출
    const wrapper = document.querySelector('.review-list-wrapper');
    if (!wrapper) return;

    const storeId = wrapper.dataset.storeId;
    const contextPath = wrapper.dataset.contextPath;

    /**
     * 2. 페이징 클릭 이벤트 (이벤트 위임 방식)
     * .pagination 내부의 어떤 .page-link를 클릭해도 이벤트를 가로챕니다.
     */
    const pagination = document.querySelector('.pagination');
    if (pagination) {
        pagination.addEventListener('click', function(e) {
            const link = e.target.closest('.page-link');
            if (link) {
                e.preventDefault();
                const pageNum = link.dataset.page;
                if (pageNum) {
                    const href = link.getAttribute('href');
                    if (href && href !== '#') {
                        location.href = href;
                        return;
                    }
                    location.href = contextPath + "/review/list?store_id=" + storeId + "&pageNum=" + pageNum;
                }
            }
        });
    }

    /**
     * 3. 리뷰 삭제 버튼 이벤트
     * member_mypage.js의 confirmDeleteReview 함수를 호출합니다.
     */
    wrapper.addEventListener('click', function(e) {
    if (e.target.classList.contains('btn-delete-review')) {
        const reviewId = e.target.dataset.reviewId;
        const sId = e.target.dataset.storeId;
        const returnUrl = e.target.dataset.returnUrl; // 추가
        
        if (typeof confirmDeleteReview === 'function') {
            confirmDeleteReview(reviewId, sId, returnUrl); // returnUrl 전달
        }
    }
});

    /**
     * 4. 하단 네비게이션 버튼 이벤트
     */
    const btnGoStore = document.getElementById('btn-go-store');
    if (btnGoStore) {
        btnGoStore.addEventListener('click', function() {
            location.href = contextPath + "/store/detail?storeId=" + storeId;
        });
    }

    const btnGoBack = document.getElementById('btn-go-back');
    if (btnGoBack) {
        btnGoBack.addEventListener('click', function() {
            history.back();
        });
    }
});
